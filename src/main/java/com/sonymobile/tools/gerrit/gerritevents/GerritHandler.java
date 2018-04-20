/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.CommentAdded;
import com.sonymobile.tools.gerrit.gerritevents.workers.Coordinator;
import com.sonymobile.tools.gerrit.gerritevents.workers.GerritEventWork;
import com.sonymobile.tools.gerrit.gerritevents.workers.JSONEventWork;
import com.sonymobile.tools.gerrit.gerritevents.workers.StreamEventsStringWork;
import com.sonymobile.tools.gerrit.gerritevents.workers.Work;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//CS IGNORE LineLength FOR NEXT 2 LINES. REASON: static import.
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_NR_OF_RECEIVING_WORKER_THREADS;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_RECEIVE_THREAD_KEEP_ALIVE_TIME;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.MIN_RECEIVE_THREAD_KEEP_ALIVE_TIME;



/**
 * Main class for this module. Contains the main loop for connecting and reading streamed events from Gerrit.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class GerritHandler implements Coordinator, Handler {

    /**
     * Time to wait between connection attempts.
     */
    private static final Logger logger = LoggerFactory.getLogger(GerritHandler.class);
    private int numberOfWorkerThreads;
    private final Set<GerritEventListener> gerritEventListeners = new CopyOnWriteArraySet<GerritEventListener>();
    private Map<String, String> ignoreEMails = new ConcurrentHashMap<String, String>();
    private ThreadPoolExecutor executor = null;
    private int threadKeepAliveTime = DEFAULT_RECEIVE_THREAD_KEEP_ALIVE_TIME;
    private static final String THREAD_PREFIX = "Gerrit Worker EventThread_";
    private static final int WAIT_FOR_JOBS_SHUTDOWN_TIMEOUT = 30;

    /**
     * The minimum size of the job-queue before monitors should begin to warn the administrator(s).
     */
    private static final int WORK_QUEUE_SIZE_WARNING_THRESHOLD =
            Integer.getInteger("gerritevents.GerritSendCommandQueue.WORK_QUEUE_SIZE_WARNING_THRESHOLD", 40);

    /**
     * Creates a GerritHandler with all the default values set.
     *
     * @see GerritDefaultValues#DEFAULT_NR_OF_RECEIVING_WORKER_THREADS
     * @see GerritDefaultValues#DEFAULT_RECEIVE_THREAD_KEEP_ALIVE_TIME
     */
    public GerritHandler() {
        this(DEFAULT_NR_OF_RECEIVING_WORKER_THREADS, DEFAULT_RECEIVE_THREAD_KEEP_ALIVE_TIME);
    }

    /**
     * Create handler with the given number of maximum worker threads
     *
     * @param numberOfWorkerThreads the number of event threads.
     */
    public GerritHandler(int numberOfWorkerThreads) {
        this(numberOfWorkerThreads, DEFAULT_RECEIVE_THREAD_KEEP_ALIVE_TIME);
    }

    /**
     * Create handler with the given number of maximum worker threads and given thread keep alive
     * time (not allowing the thread keep alive to become less than 
     * @see GerritDefaultValues#MIN_RECEIVE_THREAD_KEEP_ALIVE_TIME)
     *
     * @param numberOfWorkerThreads the number of event threads.
     * @param threadKeepAliveTime the number of seconds threads will stay alive. 
     */
    public GerritHandler(int numberOfWorkerThreads, int threadKeepAliveTime) {
        this.numberOfWorkerThreads = numberOfWorkerThreads;
        if (threadKeepAliveTime < MIN_RECEIVE_THREAD_KEEP_ALIVE_TIME)
        {
            threadKeepAliveTime = MIN_RECEIVE_THREAD_KEEP_ALIVE_TIME;
        }
        this.threadKeepAliveTime = threadKeepAliveTime;

        startQueue();
    }


    /**
     * Returns a factory for creating worker threads for the receiving queue.
     * Overwrite when you have special requirements, e.g. security constraints
     *
     * @return ThreadFactory
     */
    protected ThreadFactory getThreadFactory() {
        return new ThreadFactory() {
            private final ThreadFactory parent = Executors.defaultThreadFactory();
            private final AtomicInteger tid = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable task) {
              final Thread t = parent.newThread(task);
              t.setName(THREAD_PREFIX + tid.getAndIncrement());
              return t;
            }
          };
    }

    /**
     * Starts the executor if it hasn't started yet, or updates the thread-pool size if it is started.
     */
    protected void startQueue() {
      if (executor == null) {
          logger.debug("Starting the receiving thread pool.");
          executor = new ThreadPoolExecutor(
                  numberOfWorkerThreads,
                  numberOfWorkerThreads,
                  threadKeepAliveTime, TimeUnit.SECONDS,
                  new LinkedBlockingQueue<Runnable>(),
                  getThreadFactory());
          executor.allowCoreThreadTimeOut(true);
          //Start with one thread, and build it up gradually as it needs.
          executor.prestartCoreThread();
          logger.info("ReceiveQueue started! Current pool size: {}", executor.getPoolSize());
      } else {
          if (executor.getCorePoolSize() < numberOfWorkerThreads) {
              //If the number has increased we need to set the max first, or we'll get an IllegalArgumentException
              executor.setMaximumPoolSize(numberOfWorkerThreads);
              executor.setCorePoolSize(numberOfWorkerThreads);
          } else if (executor.getCorePoolSize() > numberOfWorkerThreads) {
              //If the number has decreased we need to set the core first.
              executor.setCorePoolSize(numberOfWorkerThreads);
              executor.setMaximumPoolSize(numberOfWorkerThreads);
          }
          logger.info("ReceiveQueue running. Current pool size: {}. Current Queue size: {}",
                  executor.getPoolSize(), getQueueSize());
          logger.info("Nr of active pool-threads: {}", executor.getActiveCount());
      }
    }

    /**
     * Returns the largest number of threads that have ever simultaneously been in the pool.
     * Package visiblity for testing purposes only.
     *
     * @return number of threads
     */
    int getLargestPoolSize() {
        return executor.getLargestPoolSize();
    }

    /**
     * Standard getter for the ignoreEMail.
     *
     * @param serverName the server name.
     * @return the e-mail address to ignore CommentAdded events from.
     */
    public String getIgnoreEMail(String serverName) {
        if (serverName != null) {
            return ignoreEMails.get(serverName);
        } else {
            return null;
        }
    }

    /**
     * Standard setter for the ignoreEMail.
     *
     * @param serverName the server name.
     * @param ignoreEMail the e-mail address to ignore CommentAdded events from.
     * If you want to remove, please set null.
     */
    public void setIgnoreEMail(String serverName, String ignoreEMail) {
        if (serverName != null) {
            if (ignoreEMail != null) {
                ignoreEMails.put(serverName, ignoreEMail);
            } else {
                ignoreEMails.remove(serverName);
            }
        }
    }

    @Override
    public void post(String data) {
        post(data, null);
    }

    @Override
    public void post(JSONObject json) {
        post(json, null);
    }

    @Override
    public void post(String data, Provider provider) {
        logger.debug("Trigger event string: {}", data);
        post(new StreamEventsStringWork(data, provider));
    }

    @Override
    public void post(JSONObject json, Provider provider) {
        logger.debug("Trigger event json object: {}", json);
        post(new JSONEventWork(json, provider));
    }

    @Override
    public void post(GerritEvent event) {
        logger.debug("Internally trigger event: {}", event);
        post(new GerritEventWork(event));
    }

    /**
     * Returns the current queue size if gerrit events.
     * @return number of events
     */
    public int getQueueSize() {
      if (executor != null) {
          return executor.getQueue().size();
      } else {
          return 0;
      }
  }

    /**
     * Post work object to work queue.
     *
     * @param work the work object.
     */
    private void post(Work work) {
        logger.trace("putting work on queue.");
        queueWork(work);
        checkQueueSize();
    }

    /**
     * Helper that will trigger to perform the work
     *
     */
    private static class EventWorker implements Runnable {

        Work work;
        Coordinator coordinator;

        /**
         * creates a new new EventWorker
         * @param work the work to do
         * @param coordinator the coordinator
         */
        public EventWorker(Work work, Coordinator coordinator) {
            this.work = work;
            this.coordinator = coordinator;
        }

        @Override
        public void run() {
            work.perform(coordinator);
        }
    }

    /**
     * puts work in the queue
     * @param work the work to do
     */
    private void queueWork(Work work) {
        try {
            logger.debug("Queueing work {}", work);
            executor.submit(new EventWorker(work, this));
        } catch (RejectedExecutionException e) {
            logger.error("Unable to queue a received event! ", e);
        }
        checkQueueSize();
    }

    /**
     * Checks queue size.
     */
    private void checkQueueSize() {
        int queueSize = getQueueSize();
        if (WORK_QUEUE_SIZE_WARNING_THRESHOLD > 0 && queueSize >= WORK_QUEUE_SIZE_WARNING_THRESHOLD) {
            logger.warn("The Gerrit incoming events queue contains {} items!"
                        + " Something might be stuck, or your system can't process the commands fast enough."
                        + " Try to increase the number of receiving worker threads."
                        + " Current thread-pool size: {}",
                    queueSize, executor.getPoolSize());
        }
    }

    @Override
    public void addListener(GerritEventListener listener) {
        synchronized (this) {
            if (!gerritEventListeners.add(listener)) {
                logger.warn("The listener was doubly-added: {}", listener);
            }
        }
    }

    /**
     * Adds all the provided listeners to the internal list of listeners.
     *
     * @param listeners the listeners to add.
     */
    @Deprecated
    public void addEventListeners(Map<Integer, GerritEventListener> listeners) {
        addEventListeners(listeners.values());
    }

    /**
     * Adds all the provided listeners to the internal list of listeners.
     *
     * @param listeners the listeners to add.
     */
    public void addEventListeners(Collection<? extends GerritEventListener> listeners) {
        synchronized (this) {
            gerritEventListeners.addAll(listeners);
        }
    }

    @Override
    public void removeListener(GerritEventListener listener) {
        synchronized (this) {
            gerritEventListeners.remove(listener);
        }
    }

    /**
     * Removes all event listeners and returns those that where removed.
     *
     * @return the former list of listeners.
     */
    public Collection<GerritEventListener> removeAllEventListeners() {
        synchronized (this) {
            HashSet<GerritEventListener> listeners = new HashSet<GerritEventListener>(gerritEventListeners);
            gerritEventListeners.clear();
            return listeners;
        }
    }

    /**
     * The number of added e{@link GerritEventListener}s.
     * @return the size.
     */
    public int getEventListenersCount() {
        return gerritEventListeners.size();
    }

    /**
     * Returns an unmodifiable view of the set of {@link GerritEventListener}s.
     *
     * @return a list of the registered event listeners.
     * @see Collections#unmodifiableSet(Set)
     */
    public Set<GerritEventListener> getGerritEventListenersView() {
        return Collections.unmodifiableSet(gerritEventListeners);
    }

    /**
     * Gets the number of event worker threads.
     *
     * @return the number of threads.
     */
    public int getNumberOfWorkerThreads() {
        return numberOfWorkerThreads;
    }

    /**
     * Sets the number of worker event threads.
     *
     * @param numberOfWorkerThreads the number of threads
     */
    public void setNumberOfWorkerThreads(int numberOfWorkerThreads) {
        this.numberOfWorkerThreads = numberOfWorkerThreads;
        startQueue();
    }

    /**
     * Gets the number of seconds threads are kept alive.
     * @return number of seconds
     */

    public int getThreadKeepAliveTime() {
        return threadKeepAliveTime;
    }

    /**
     * Sets the number of seconds threads well be kept alive.
     *
     * @param threadKeepAliveTime number of seconds
     */
    public void setThreadKeepAliveTime(int threadKeepAliveTime) {
        this.threadKeepAliveTime = threadKeepAliveTime;
        executor.setKeepAliveTime(threadKeepAliveTime, TimeUnit.SECONDS);
    }

    /**
     * Notifies all listeners of a Gerrit event. This method is meant to be called by one of the Worker Threads {@link
     * com.sonymobile.tools.gerrit.gerritevents.workers.EventThread} and not on this Thread which would
     * defeat the purpose of having workers.
     *
     * @param event the event.
     */
    @Override
    public void notifyListeners(GerritEvent event) {
        if (event instanceof CommentAdded) {
            if (ignoreEvent((CommentAdded)event)) {
                logger.trace("CommentAdded ignored");
                return;
            }
        }
        for (GerritEventListener listener : gerritEventListeners) {
            try {
                notifyListener(listener, event);
            } catch (Exception ex) {
                logger.error("When notifying listener: {} about event: {}", listener, event);
                logger.error("Notify-error: ", ex);
            }
        }
    }

    /**
     * Sub method of {@link #notifyListeners(com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent) }.
     * This is where most of the reflection magic in the event notification is done.
     *
     * @param listener the listener to notify
     * @param event    the event.
     */
    private void notifyListener(GerritEventListener listener, GerritEvent event) {
        logger.trace("Notifying listener {} of event {}", listener, event);
        try {
            logger.trace("Reflecting closest method");
            Method method = listener.getClass().getMethod("gerritEvent", event.getClass());
            method.invoke(listener, event);
        } catch (IllegalAccessException ex) {
            logger.debug("Not allowed to invoke the reflected method. Calling default.", ex);
            listener.gerritEvent(event);
        } catch (IllegalArgumentException ex) {
            logger.debug("Not allowed to invoke the reflected method with specified parameter (REFLECTION BUG). "
               + "Calling default.", ex);
            listener.gerritEvent(event);
        } catch (InvocationTargetException ex) {
            logger.error("When notifying listener: {} about event: {}", listener, event);
            logger.error("Exception thrown during event handling.", ex);
        } catch (NoSuchMethodException ex) {
            logger.debug("No apropriate method found during reflection. Calling default.", ex);
            listener.gerritEvent(event);
        } catch (SecurityException ex) {
            logger.debug("Not allowed to reflect/invoke a method on this listener (DESIGN BUG). Calling default", ex);
            listener.gerritEvent(event);
        }
    }

    /**
     * Checks if the event should be ignored.
     * @param event the event to check.
     * @return true if it should be ignored, false if not.
     */
    private boolean ignoreEvent(CommentAdded event) {
        Account account = event.getAccount();
        if (account == null) {
            return false;
        }

        String accountEmail = account.getEmail();
        if (StringUtils.isEmpty(accountEmail)) {
            return false;
        }

        Provider provider = event.getProvider();
        if (provider != null) {
            String ignoreEMail = ignoreEMails.get(provider.getName());
            if (StringUtils.isNotEmpty(ignoreEMail) && accountEmail.endsWith(ignoreEMail)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Closes the handler.
     *
     * @param join if the method should wait for the thread to finish before returning.
     */
    public void shutdown(boolean join) {
        if (executor != null) {
            ThreadPoolExecutor pool = executor;
            executor = null;
            pool.shutdown(); // Disable new tasks from being submitted
            if (join) {
                try {
                    // Wait a while for existing tasks to terminate
                    if (!pool.awaitTermination(WAIT_FOR_JOBS_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                        pool.shutdownNow(); // Cancel currently executing tasks
                        // Wait a while for tasks to respond to being cancelled
                        if (!pool.awaitTermination(WAIT_FOR_JOBS_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                            logger.error("Pool did not terminate");
                        }
                    }
                } catch (InterruptedException ie) {
                    // (Re-)Cancel if current thread also interrupted
                    pool.shutdownNow();
                    // Preserve interrupt status
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * "Triggers" an event by adding it to the internal queue and be taken by one of the worker threads. This way it
     * will be put into the normal flow of events as if it was coming from the stream-events command.
     *
     * @param event the event to trigger.
     */
    @Deprecated
    public void triggerEvent(GerritEvent event) {
        post(event);
    }
}
