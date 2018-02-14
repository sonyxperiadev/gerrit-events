/*
 * The MIT License
 *
 * Copyright 2011 Sony Mobile Communications Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeAbandoned;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeMerged;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeRestored;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.CommentAdded;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.DraftPublished;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.MergeFailed;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.PatchsetCreated;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.RefUpdated;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ReviewerAdded;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.TopicChanged;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ProjectCreated;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.PrivateStateChanged;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.WipStateChanged;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.only;

//CS IGNORE MagicNumber FOR NEXT 600 LINES. REASON: Test data.

/**
 * Tests for {@link com.sonymobile.tools.gerrit.gerritevents.GerritHandler}.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
@RunWith(PowerMockRunner.class)
public class GerritHandlerTest {

    private GerritHandler handler;

    /**
     * Creates a GerritHandler.
     */
    @Before
    public void setup() {
        handler = new GerritHandler();
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
     * Tests {@link GerritHandler#addListener(GerritEventListener)}.
     *
     * @throws Exception if so.
     */
    @Test
    public void testAddListener() throws Exception {
        GerritEventListener listenerMock = mock(GerritEventListener.class);
        handler.addListener(listenerMock);
        Collection<GerritEventListener> gerritEventListeners =
                Whitebox.getInternalState(handler, "gerritEventListeners");
        assertThat(listenerMock, isIn(gerritEventListeners));
        assertEquals(1, gerritEventListeners.size());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.GerritHandler#addListener(GerritEventListener)}.
     * With 10000 listeners added by 10 threads at the same time.
     *
     * @throws Exception if so.
     */
    @Test
    public void testAddListenerManyAtTheSameTime() throws Exception {
        final int nrOfListeners = 100000;
        BlockingQueue<Runnable> listeners = new LinkedBlockingQueue<Runnable>(nrOfListeners);
        System.out.print("Creating Listeners");
        for (int i = 0; i < nrOfListeners; i++) {
            listeners.add(new Runnable() {
                GerritEventListener listener = new ListenerMock();
                @Override
                public void run() {
                    handler.addListener(listener);
                }
            });
            if (i % 1000 == 0) {
                System.out.print(".");
            }
        }
        System.out.println(".Done!");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 1, TimeUnit.MINUTES, listeners);
        executor.prestartAllCoreThreads();
        executor.shutdown();
        do {
            System.out.printf("Waiting for listeners to be added...Running#: %5d  Left#: %5d  Count#: %5d\n",
                    executor.getActiveCount(), listeners.size(), handler.getEventListenersCount());
        } while (!executor.awaitTermination(1, TimeUnit.SECONDS));
        System.out.printf("              Listeners are added...Running#: %5d  Left#: %5d  Count#: %5d\n",
                    executor.getActiveCount(), listeners.size(), handler.getEventListenersCount());
        assertEquals(nrOfListeners, handler.getEventListenersCount());
    }

    /**
     * Tests {@link GerritHandler#addEventListeners(java.util.Map)}.
     *
     * @throws Exception if so.
     */
    @Test
    public void testAddEventListeners() throws Exception {
        Collection<GerritEventListener> listeners = new HashSet<GerritEventListener>();
        GerritEventListener listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        handler.addEventListeners(listeners);
        Collection<GerritEventListener> gerritEventListeners =
                Whitebox.getInternalState(handler, "gerritEventListeners");
        assertThat(listenerMock, isIn(gerritEventListeners));
        assertEquals(5, gerritEventListeners.size());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.GerritHandler#removeListener(GerritEventListener)}.
     *
     * @throws Exception if so.
     */
    @Test
    public void testRemoveListener() throws Exception {
        GerritEventListener listenerMock = mock(GerritEventListener.class);
        handler.addListener(listenerMock);
        handler.removeListener(listenerMock);
        Collection<GerritEventListener> gerritEventListeners =
                Whitebox.getInternalState(handler, "gerritEventListeners");
        assertTrue(gerritEventListeners.isEmpty());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.GerritHandler#removeAllEventListeners()}.
     *
     * @throws Exception if so.
     */
    @Test
    public void testRemoveAllEventListeners() throws Exception {
        Collection<GerritEventListener> listeners = new HashSet<GerritEventListener>();
        GerritEventListener listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        listenerMock = mock(GerritEventListener.class);
        listeners.add(listenerMock);
        handler.addEventListeners(listeners);
        listeners = handler.removeAllEventListeners();
        assertThat(listenerMock, isIn(listeners));
        assertEquals(5, listeners.size());
        listeners = Whitebox.getInternalState(handler, "gerritEventListeners");
        assertTrue(listeners.isEmpty());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.GerritHandler#removeAllEventListeners()} when
     * one listener's hashCode has changed mid air.
     *
     * @throws Exception if so.
     */
    @Test
    public void testRemoveAllEventListenersOneChanged() throws Exception {
        Collection<GerritEventListener> listeners = new HashSet<GerritEventListener>();
        ListenerMock listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        handler.addEventListeners(listeners);
        listenerMock.code = (10000);
        listeners = handler.removeAllEventListeners();
        assertThat(listenerMock, isIn(listeners));
        assertEquals(5, listeners.size());
        listeners = Whitebox.getInternalState(handler, "gerritEventListeners");
        assertTrue(listeners.isEmpty());
    }

    /**
     * Tests to remove all eventlisteners and then re add them.
     *
     * @throws Exception if so.
     */
    @Test
    public void testReAddAllEventListenersOneChanged() throws Exception {
        Collection<GerritEventListener> listeners = new HashSet<GerritEventListener>();
        ListenerMock listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        listenerMock = new ListenerMock();
        listeners.add(listenerMock);
        handler.addEventListeners(listeners);
        listenerMock.code = (10000);
        listeners = handler.removeAllEventListeners();
        assertThat(listenerMock, isIn(listeners));
        assertEquals(5, listeners.size());
        Collection<GerritEventListener> gerritEventListeners =
                Whitebox.getInternalState(handler, "gerritEventListeners");
        assertTrue(gerritEventListeners.isEmpty());
        handler.addEventListeners(listeners);
        gerritEventListeners = Whitebox.getInternalState(handler, "gerritEventListeners");
        assertThat(listenerMock, isIn(gerritEventListeners));
        assertEquals(5, gerritEventListeners.size());
    }

    /**
     * Tests that ignoreEMails is handled correctly.
     * @throws Exception if so.
     */
    @Test
    public void testIgnoreEMails() throws Exception {
        String email = "e@mail.com";
        String server = "testserver";
        handler.setIgnoreEMail(server, email);
        assertEquals(email, handler.getIgnoreEMail(server));
        handler.setIgnoreEMail(server, null);
        assertEquals(null, handler.getIgnoreEMail(server));
    }

    /**
     * Tests that CommentAdded events are ignored correctly.
     * @throws Exception if so.
     */
    @Test
    public void testIgnoreCommentAdded() throws Exception {
        String server = "testserver";
        handler.setIgnoreEMail(server, "ignore-mail.com");
        ListenerMock listenerMock = mock(ListenerMock.class);
        handler.addListener(listenerMock);
        Account account = new Account("name", "e@ignore-mail.com");
        Provider provider = new Provider();
        provider.setName(server);
        CommentAdded ca = new CommentAdded();
        ca.setAccount(account);
        ca.setProvider(provider);

        handler.notifyListeners(ca);

        verifyNoMoreInteractions(listenerMock);
    }

    /**
     * Tests that CommentAdded events are ignored correctly.
     * @throws Exception if so.
     */
    @Test
    public void testIgnoreCommentAddedWithNullMail() throws Exception {
        String server = "testserver";
        handler.setIgnoreEMail(server, "ignore-mail.com");
        ListenerMock listenerMock = mock(ListenerMock.class);
        handler.addListener(listenerMock);
        Account account = new Account("name", null);
        Provider provider = new Provider();
        provider.setName(server);
        CommentAdded ca = new CommentAdded();
        ca.setAccount(account);
        ca.setProvider(provider);

        handler.notifyListeners(ca);

        Mockito.verify(listenerMock, only()).gerritEvent(ca);
    }

    /**
     * Tests that CommentAdded events are ignored correctly.
     * @throws Exception if so.
     */
    @Test
    public void testCommentAddedWithEmptyIgnoreMail() throws Exception {
        String server = "testserver";
        handler.setIgnoreEMail(server, "");
        ListenerMock listenerMock = mock(ListenerMock.class);
        handler.addListener(listenerMock);
        Account account = new Account("name", "some@mail.com");
        Provider provider = new Provider();
        provider.setName(server);
        CommentAdded ca = new CommentAdded();
        ca.setAccount(account);
        ca.setProvider(provider);

        handler.notifyListeners(ca);

        Mockito.verify(listenerMock, only()).gerritEvent(ca);
    }

    /**
     * A GerritListener mock that can change it's hashCode
     */
    static class ListenerMock implements GerritEventListener {
        private static int count = 0;
        int code;

        /**
         * Default constructor.
         */
        ListenerMock() {
            code = 54 + count++;
        }

        @Override
        public void gerritEvent(GerritEvent event) {

        }

        @Override
        public int hashCode() {
            return code;
        }
    }

    /**
     * Tests that event notification using the default method.
     */
    @Test
    public void testEventNotificationWithDefaultListenerImplemention() {
        GerritEventListener listenerMock = mock(GerritEventListener.class);
        handler.addListener(listenerMock);

        ChangeAbandoned changeAbandoned = new ChangeAbandoned();
        handler.notifyListeners(changeAbandoned);
        verify(listenerMock, times(1)).gerritEvent(changeAbandoned);

        ChangeMerged changeMerged = new ChangeMerged();
        handler.notifyListeners(changeMerged);
        verify(listenerMock, times(1)).gerritEvent(changeMerged);

        ChangeRestored changeRestored = new ChangeRestored();
        handler.notifyListeners(changeRestored);
        verify(listenerMock, times(1)).gerritEvent(changeRestored);

        CommentAdded commentAdded = new CommentAdded();
        handler.notifyListeners(commentAdded);
        verify(listenerMock, times(1)).gerritEvent(commentAdded);

        DraftPublished draftPublished = new DraftPublished();
        handler.notifyListeners(draftPublished);
        verify(listenerMock, times(1)).gerritEvent(draftPublished);

        PatchsetCreated patchsetCreated = new PatchsetCreated();
        handler.notifyListeners(patchsetCreated);
        verify(listenerMock, times(1)).gerritEvent(patchsetCreated);

        RefUpdated refUpdated = new RefUpdated();
        handler.notifyListeners(refUpdated);
        verify(listenerMock, times(1)).gerritEvent(refUpdated);

        ProjectCreated projectCreated = new ProjectCreated();
        handler.notifyListeners(projectCreated);
        verify(listenerMock, times(1)).gerritEvent(projectCreated);

        TopicChanged topicChanged = new TopicChanged();
        handler.notifyListeners(topicChanged);
        verify(listenerMock, times(1)).gerritEvent(topicChanged);

        ReviewerAdded reviewerAdded = new ReviewerAdded();
        handler.notifyListeners(reviewerAdded);
        verify(listenerMock, times(1)).gerritEvent(reviewerAdded);

        MergeFailed mergeFailed = new MergeFailed();
        handler.notifyListeners(mergeFailed);
        verify(listenerMock, times(1)).gerritEvent(mergeFailed);

        PrivateStateChanged privateStateChanged = new PrivateStateChanged();
        handler.notifyListeners(privateStateChanged);
        verify(listenerMock, times(1).gerritEvent(privateStateChanged));

        WipStateChanged wipStateChanged = new WipStateChanged();
        handler.notifyListeners(wipStateChanged);
        verify(listenerMock, times(1).gerritEvent(wipStateChanged));
    }

    /**
     * Tests that ChangeAbandoned event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerChangeAbandonedMethodSignature() {
        SpecificEventListener changeAbandonedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(ChangeAbandoned event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(changeAbandonedListener, new ChangeAbandoned());
    }

    /**
     * Tests that ChangeMerged event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerChangeMergedMethodSignature() {
        SpecificEventListener changeMergedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(ChangeMerged event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(changeMergedListener, new ChangeMerged());
    }

    /**
     * Tests that ChangeRestored event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerChangeRestoredMethodSignature() {
        SpecificEventListener changeRestoredListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(ChangeRestored event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(changeRestoredListener, new ChangeRestored());
    }

    /**
     * Tests that CommentAdded event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerCommentAddedMethodSignature() {
        SpecificEventListener commentAddedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(CommentAdded event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(commentAddedListener, new CommentAdded());
    }

    /**
     * Tests that DraftPublished event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerDraftPublishedMethodSignature() {
        SpecificEventListener draftPublishedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(DraftPublished event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(draftPublishedListener, new DraftPublished());
    }

    /**
     * Tests that PatchsetCreated event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerPatchsetCreatedMethodSignature() {
        SpecificEventListener patchsetCreatedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(PatchsetCreated event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(patchsetCreatedListener, new PatchsetCreated());
    }

    /**
     * Tests that RefUpdated event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerRefUpdatedMethodSignature() {
        SpecificEventListener refUpdatedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(RefUpdated event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(refUpdatedListener, new RefUpdated());
    }

    /**
     * Tests that ProjectCreated event are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerProjectCreatedMethodSignature() {
        SpecificEventListener projectCreatedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(ProjectCreated event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(projectCreatedListener, new ProjectCreated());
    }

    /**
     * Tests that TopicChanged events are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerTopicChangedMethodSignature() {
        SpecificEventListener topicChangedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(TopicChanged event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(topicChangedListener, new TopicChanged());
    }

    /**
     * Tests that ReviewerAdded events are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerReviewerAddedMethodSignature() {
        SpecificEventListener reviewerAddedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(ReviewerAdded event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(reviewerAddedListener, new ReviewerAdded());
    }

    /**
     * Tests that MergeFailed events are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerMergeFailedMethodSignature() {
        SpecificEventListener mergeFailedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(MergeFailed event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(mergeFailedListener, new MergeFailed());
    }

    /**
     * Tests that PrivateStateChanged events are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerPrivateStateChangedMethodSignature() {
        SpecificEventListener stateChangedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(PrivateStateChanged event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(stateChangedListener, new PrivateStateChanged());
    }

    /**
     * Tests that WipStateChanged events are going in the method with
     * that type as parameter and that other type of events are going
     * in the default method.
     */
    @Test
    public void testEventNotificationWithListenerWipStateChangedMethodSignature() {
        SpecificEventListener stateChangedListener = new SpecificEventListener() {
            @SuppressWarnings("unused") //method is called by reflection
            public void gerritEvent(WipStateChanged event) {
                specificMethodCalled = true;
            }
        };
        testListenerWithSpecificSignature(stateChangedListener, new WipStateChanged());
    }

    /**
     * Base test listener implementation.
     */
    private abstract static class SpecificEventListener implements GerritEventListener {
        boolean defautMethodCalled = false;
        boolean specificMethodCalled = false;
        @Override
        public void gerritEvent(GerritEvent event) {
            defautMethodCalled = true;
        }
        /**
         * Reset the listener.
         */
        public void reset() {
            defautMethodCalled = false;
            specificMethodCalled = false;
        }
    }

    /**
     * Test that the specific method of a listener is called for the specific
     * type and that any the default method of the listener is called for any
     * other type of events.
     * @param listener the specific listener
     * @param specificEvent the specific event
     */
    private void testListenerWithSpecificSignature(SpecificEventListener listener, GerritEvent specificEvent) {
        GerritEvent[] allEvents =  new GerritEvent[]{new ChangeAbandoned(),
                                                     new ChangeMerged(),
                                                     new ChangeRestored(),
                                                     new ChangeAbandoned(),
                                                     new DraftPublished(),
                                                     new PatchsetCreated(),
                                                     new RefUpdated(),
                                                     new ProjectCreated(),
                                                     new PrivateStateChanged(),
                                                     new WipStateChanged(), };
        handler.addListener(listener);

        // Validate that event was sent to the specific method
        handler.notifyListeners(specificEvent);
        assertFalse(listener.defautMethodCalled);
        assertTrue(listener.specificMethodCalled);
        listener.reset();

        // Validate that other events are going in the default method
        for (GerritEvent gerritEvent : allEvents) {
            if (gerritEvent.getEventType() != specificEvent.getEventType()) {
                handler.notifyListeners(gerritEvent);
                assertTrue(listener.defautMethodCalled);
                assertFalse(listener.specificMethodCalled);
                listener.reset();
            }
        }
    }

}
