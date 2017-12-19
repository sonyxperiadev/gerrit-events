/*
 * The MIT License
 *
 * Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
 * Copyright 2014 Sony Mobile Communications AB. All rights reserved.s
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
package com.sonymobile.tools.gerrit.gerritevents.ssh;

import com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues;

import java.io.IOException;

/**
 * Factory class for {@link SshConnection}s.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public abstract class SshConnectionFactory {

    /**
     * Private constructor to hinder instantiation.
     */
    private SshConnectionFactory() {
        throw new UnsupportedOperationException("Cannot instantiate util classes.");
    }

    /**
     * Creates a {@link SshConnection}.
     *
     * @param host           the host name
     * @param port           the port
     * @param authentication the credentials
     * @return a new connection.
     *
     * @throws IOException if so.
     * @see SshConnection
     * @see SshConnectionImpl
     */
    public static SshConnection getConnection(String host, int port,
                                              Authentication authentication) throws IOException {
        return getConnection(host, port, GerritDefaultValues.DEFAULT_GERRIT_PROXY, authentication);
    }

    /**
     * Creates a {@link SshConnection}.
     *
     * @param host           the host name
     * @param port           the port
     * @param proxy          the proxy url
     * @param authentication the credentials
     * @return a new connection.
     *
     * @throws IOException if so.
     * @see SshConnection
     * @see SshConnectionImpl
     */
    public static SshConnection getConnection(String host, int port, String proxy,
                                              Authentication authentication) throws IOException {
        return getConnection(host, port, proxy, authentication, null, GerritDefaultValues.DEFAULT_GERRIT_SSH_CONNECTION_TIMEOUT);
    }

    /**
     * Creates a {@link SshConnection}.
     *
     * @param host           the host name
     * @param port           the port
     * @param proxy          the proxy url
     * @param authentication the credentials
     * @param connectionTimeout the connection timeout
     * @return a new connection.
     *
     * @throws IOException if so.
     * @see SshConnection
     * @see SshConnectionImpl
     */
    public static SshConnection getConnection(String host, int port, String proxy,
                                              Authentication authentication, int connectionTimeout) throws IOException {
        return getConnection(host, port, proxy, authentication, null, connectionTimeout);
    }

    /**
     * Creates a {@link SshConnection}.
     *
     * @param host           the host name
     * @param port           the port
     * @param proxy          the proxy url
     * @param authentication the credentials
     * @param updater        the updater.
     * @return a new connection.
     *
     * @throws IOException if so.
     * @see SshConnection
     * @see SshConnectionImpl
     */
    public static SshConnection getConnection(String host, int port, String proxy,
                                              Authentication authentication,
                                              AuthenticationUpdater updater) throws IOException {
        return getConnection(host, port, proxy, authentication, updater, GerritDefaultValues.DEFAULT_GERRIT_SSH_CONNECTION_TIMEOUT);
    }

    /**
     * Creates a {@link SshConnection}.
     *
     * @param host           the host name
     * @param port           the port
     * @param proxy          the proxy url
     * @param authentication the credentials
     * @param updater        the updater.
     * @param connectionTimeout the connection timeout.
     * @return a new connection.
     *
     * @throws IOException if so.
     * @see SshConnection
     * @see SshConnectionImpl
     */
    public static SshConnection getConnection(String host, int port, String proxy,
                                              Authentication authentication,
                                              AuthenticationUpdater updater, int connectionTimeout) throws IOException {
        SshConnection connection = new SshConnectionImpl(host, port, proxy, authentication, updater, connectionTimeout);
        connection.connect();
        return connection;
    }
}
