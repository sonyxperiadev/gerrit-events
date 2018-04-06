package com.sonymobile.tools.gerrit.gerritevents;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.RefUpdated;

//CS IGNORE MagicNumber FOR NEXT 600 LINES. REASON: Test data.

/**
 * Tests the GerritHandler queueing.
 */
public class GerritHandlerQueueTest {

    private GerritHandler handler;
    SlowEventListener listener;

    /**
     * Initialize.
     */
    @Before
    public void setup() {
        listener = new SlowEventListener();
    }

    /**
     * Shuts down the GerritHandler.
     */
    @After
    public void shutDown() {
        if (handler != null) {
            handler.shutdown(true);
        }
        handler = null;
    }

    /**
     * Put given number of events in the queue.
     *
     * @param number
     *            number of events
     */
    private void postEventsToQueue(int number) {
        for (int i = 0; i < number; i++) {
            handler.post(new RefUpdated());
        }

    }

    /**
     * Default number of executors is used.
     *
     * @throws Exception if occurred
     */
    @Test
    public void testDefaultNumberofParallelWorkers() throws Exception {
        handler = new GerritHandler();

        listener = new SlowEventListener();
        handler.addListener(listener);
        postEventsToQueue(5);
        waitForEventsProcessed();
        assertThat(handler.getLargestPoolSize(), equalTo(3));
    }

    /**
     * More of executors are used.
     *
     * @throws Exception if occurred
     */
    @Test
    public void testHigherNumberofParallelWorkers() throws Exception {
        handler = new GerritHandler(5);
        handler.addListener(listener);
        postEventsToQueue(8);
        waitForEventsProcessed();
        assertThat(handler.getLargestPoolSize(), equalTo(5));
    }

    /**
     * Number of executors is increased and used.
     *
     * @throws InterruptedException if occurred
     */
    @Test
    public void testWorkersAreIncreased() throws InterruptedException {
        handler = new GerritHandler(1);
        handler.addListener(listener);
        postEventsToQueue(9);
        handler.setNumberOfWorkerThreads(5);
        waitForEventsProcessed();
        assertThat(handler.getLargestPoolSize(), equalTo(5));
    }

    /**
     * High number of executors is used after increasing.
     *
     * @throws Exception if occurred
     */
    @Test
    public void testWorkersAreIncreasedWithmaynEventsAndWorkers() throws Exception {
        handler = new GerritHandler(1);
        handler.addListener(listener);
        postEventsToQueue(100);
        handler.setNumberOfWorkerThreads(50);
        waitForEventsProcessed();
        assertThat(handler.getLargestPoolSize(), equalTo(50));
    }

    /**
     * Number of executors are decreasd.
     *
     * @throws Exception if occurred
     */
    @Test
    public void testWorkersAreDecreased() throws Exception {
        handler = new GerritHandler(5, 1);
        handler.addListener(listener);
        postEventsToQueue(10);
        waitForEventsProcessed();
        assertThat(handler.getLargestPoolSize(), equalTo(5));
        listener.maxParallel = 0;
        handler.setNumberOfWorkerThreads(3);
        Thread.sleep(1000);
        postEventsToQueue(5);
        waitForEventsProcessed();
        assertThat(listener.maxParallel, equalTo(3));
    }

    /**
     * Wait that all events have been processed.
     *
     * @throws InterruptedException if occurred
     */
    private void waitForEventsProcessed() throws InterruptedException {
        while (handler.getQueueSize() > 0 || listener.counter.get() > 0) {
            Thread.sleep(100);
        }
    }

    /**
     * A slow event listener so the queue is not processed too fast and all
     * threads in the pool are used.
     */
    class SlowEventListener implements GerritEventListener {

        private int maxParallel = 0;
        private AtomicInteger counter = new AtomicInteger();

        @Override
        public void gerritEvent(GerritEvent event) {
            try {
                int c = counter.incrementAndGet();
                synchronized (counter) {

                    if (c > maxParallel) {
                        maxParallel = c;
                    }
                }
                Thread.sleep(200);
                c = counter.decrementAndGet();
            } catch (InterruptedException e) {
                System.out.println("Ignore it");
            }
        }
    }

}
