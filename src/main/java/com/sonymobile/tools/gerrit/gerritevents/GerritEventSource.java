/*
 *  The MIT License
 *
 *  Copyright 2026 Amarula Solutions All rights reserved.
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

/**
 * Interface for a source of Gerrit events. Implementations are responsible for
 * connecting to a Gerrit server and feeding events to a {@link GerritHandler}.
 *
 * <p>This abstraction allows different transport mechanisms (SSH stream-events,
 * HTTPS polling, etc.) to be used interchangeably by the event consumer.</p>
 *
 * @author Michael Trimarchi
 */
public interface GerritEventSource {

    /**
     * Sets the Gerrit handler that will process received events.
     *
     * @param handler the handler
     */
    void setHandler(GerritHandler handler);

    /**
     * Gets the Gerrit handler.
     *
     * @return the handler
     */
    GerritHandler getHandler();

    /**
     * Adds a connection listener.
     *
     * @param listener the listener
     */
    void addListener(ConnectionListener listener);

    /**
     * Removes a connection listener.
     *
     * @param listener the listener
     */
    void removeListener(ConnectionListener listener);

    /**
     * Returns whether this event source is currently connected and receiving events.
     *
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Returns the version of the connected Gerrit server.
     *
     * @return the Gerrit version string, or null if not connected
     */
    String getGerritVersion();

    /**
     * Shuts down this event source.
     *
     * @param join if true, waits for the underlying thread to finish before returning
     */
    void shutdown(boolean join);

    /**
     * Starts this event source. Typically implemented by delegating to the
     * underlying thread's start method.
     */
    void start();
}
