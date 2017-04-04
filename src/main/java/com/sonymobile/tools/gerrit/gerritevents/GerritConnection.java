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


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.jcraft.jsch.ChannelExec;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.AuthenticationUpdater;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshAuthenticationException;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectException;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import com.sonymobile.tools.gerrit.gerritevents.watchdog.StreamWatchdog;
import com.sonymobile.tools.gerrit.gerritevents.watchdog.WatchTimeExceptionData;


//CS IGNORE LineLength FOR NEXT 7 LINES. REASON: static import.


/**
 * Main class for connection. Contains the main loop for connecting to Gerrit.
 *
 * @author rinrinne &lt;rinrin.ne@gmail.com&gt;
 */
public class GerritConnection extends Thread implements Connector {

    /**
     * Time to wait between connection attempts.
     */
    public static final int CONNECT_SLEEP = 2000;
    /**
     * Command to open gerrit event stream.
     */
    public static final String CMD_STREAM_EVENTS = "gerrit stream-events";
    private static final String GERRIT_VERSION_PREFIX = "gerrit version ";
    private static final int SSH_RX_BUFFER_SIZE = 16384;
    private static final int SSH_RX_SLEEP_MILLIS = 100;
    /**
     * The standard scheme used for stream-events.
     */
    public static final String GERRIT_PROTOCOL_SCHEME_NAME = "ssh";
    private static final Logger logger = LoggerFactory.getLogger(GerritConnection.class);
    private String gerritName;
    private String gerritHostName;
    private int gerritSshPort;
    private String gerritProxy;
    private Authentication authentication;
    private String gerritFrontEndUrl;
    private SshConnection sshConnection;
    private volatile boolean shutdownInProgress = false;
    private volatile boolean connected = false;
    private String gerritVersion = null;
    private int watchdogTimeoutSeconds;
    private WatchTimeExceptionData exceptionData;
    private StreamWatchdog watchdog;
    private int reconnectCallCount = 0;
    private GerritHandler handler;
    private AuthenticationUpdater authenticationUpdater = null;
    private final Set<ConnectionListener> listeners = new CopyOnWriteArraySet<ConnectionListener>();
    private int sshRxBufferSize = SSH_RX_BUFFER_SIZE;

    /**
     * Creates a GerritHandler with all the default values set.
     *
     * @see GerritDefaultValues#DEFAULT_GERRIT_NAME
     * @see GerritDefaultValues#DEFAULT_GERRIT_HOSTNAME
     * @see GerritDefaultValues#DEFAULT_GERRIT_SSH_PORT
     * @see GerritDefaultValues#DEFAULT_GERRIT_PROXY
     * @see GerritDefaultValues#DEFAULT_GERRIT_USERNAME
     * @see GerritDefaultValues#DEFAULT_GERRIT_AUTH_KEY_FILE
     * @see GerritDefaultValues#DEFAULT_GERRIT_AUTH_KEY_FILE_PASSWORD
     */
    public GerritConnection() {
        this(GerritDefaultValues.DEFAULT_GERRIT_NAME,
                GerritDefaultValues.DEFAULT_GERRIT_HOSTNAME,
                GerritDefaultValues.DEFAULT_GERRIT_SSH_PORT,
                GerritDefaultValues.DEFAULT_GERRIT_PROXY,
                GerritDefaultValues.DEFAULT_GERRIT_FRONT_END_URL,
                new Authentication(GerritDefaultValues.DEFAULT_GERRIT_AUTH_KEY_FILE,
                        GerritDefaultValues.DEFAULT_GERRIT_USERNAME,
                        GerritDefaultValues.DEFAULT_GERRIT_AUTH_KEY_FILE_PASSWORD));
    }

    /**
     * Creates a GerritHandler with the specified values.
     *
     * @param gerritName the name of the gerrit server.
     * @param gerritHostName the hostName
     * @param gerritSshPort  the ssh port that the gerrit server listens to.
     * @param authentication the authentication credentials.
     */
    public GerritConnection(String gerritName,
                         String gerritHostName,
                         int gerritSshPort,
                         Authentication authentication) {
        this(gerritName,
                gerritHostName,
                gerritSshPort,
                GerritDefaultValues.DEFAULT_GERRIT_PROXY,
                GerritDefaultValues.DEFAULT_GERRIT_FRONT_END_URL,
                authentication);
    }

    /**
     * Standard Constructor.
     *
     * @param gerritName the name of the gerrit server.
     * @param config the configuration containing the connection values.
     */
    public GerritConnection(String gerritName, GerritConnectionConfig config) {
        this(gerritName, config, GerritDefaultValues.DEFAULT_GERRIT_PROXY, 0, null);
    }

    /**
     * Standard Constructor.
     *
     * @param gerritName the name of the gerrit server.
     * @param config the configuration containing the connection values.
     */
    public GerritConnection(String gerritName, GerritConnectionConfig2 config) {
        this(gerritName, config, config.getGerritProxy(), config.getWatchdogTimeoutSeconds(), config.getExceptionData());
    }

    /**
     * Creates a GerritHandler with the specified values.
     *
     * @param gerritName             the name of the gerrit server.
     * @param config                 the configuration containing the connection values.
     * @param gerritProxy            the URL of gerrit proxy.
     * @param watchdogTimeoutSeconds number of seconds before the connection watch dog restarts the connection set to 0
     *                               or less to disable it.
     * @param exceptionData          time info for when the watch dog's timeout should not be in effect. set to null to
     *                               disable the watch dog.
     */
    public GerritConnection(String gerritName,
                         GerritConnectionConfig config,
                         String gerritProxy,
                         int watchdogTimeoutSeconds,
                         WatchTimeExceptionData exceptionData) {
        this(gerritName,
                config.getGerritHostName(),
                config.getGerritSshPort(),
                gerritProxy,
                config.getGerritFrontEndUrl(),
                config.getGerritAuthentication(),
                watchdogTimeoutSeconds, exceptionData);
    }

    /**
     * Standard Constructor.
     *
     * @param gerritName            the name of the gerrit server.
     * @param gerritHostName        the hostName for gerrit.
     * @param gerritSshPort         the ssh port that the gerrit server listens to.
     * @param gerritProxy           the proxy url socks5|http://host:port.
     * @param gerritFrontEndUrl       the gerrit front end url.
     * @param authentication        the authentication credentials.
     */
    public GerritConnection(String gerritName,
                         String gerritHostName,
                         int gerritSshPort,
                         String gerritProxy,
                         String gerritFrontEndUrl,
                         Authentication authentication) {
        this(gerritName, gerritHostName, gerritSshPort, gerritProxy, gerritFrontEndUrl, authentication, 0, null);
    }

    /**
     * Standard Constructor.
     *
     * @param gerritName              the name of the gerrit server.
     * @param gerritHostName          the hostName for gerrit.
     * @param gerritSshPort             the ssh port that the gerrit server listens to.
     * @param gerritProxy              the proxy url socks5|http://host:port.
     * @param gerritFrontEndUrl       the gerrit front end url.
     * @param authentication            the authentication credentials.
     * @param watchdogTimeoutSeconds number of seconds before the connection watch dog restarts the connection
     *                               set to 0 or less to disable it.
     * @param exceptionData           time info for when the watch dog's timeout should not be in effect.
     *                                  set to null to disable the watch dog.
     */
    public GerritConnection(String gerritName,
                         String gerritHostName,
                         int gerritSshPort,
                         String gerritProxy,
                         String gerritFrontEndUrl,
                         Authentication authentication,
                         int watchdogTimeoutSeconds,
                         WatchTimeExceptionData exceptionData) {
        this.gerritName = gerritName;
        this.gerritHostName = gerritHostName;
        this.gerritSshPort = gerritSshPort;
        this.gerritProxy = gerritProxy;
        this.gerritFrontEndUrl = gerritFrontEndUrl;
        this.authentication = authentication;
        this.watchdogTimeoutSeconds = watchdogTimeoutSeconds;
        this.exceptionData = exceptionData;
    }

    /**
     * Sets Buffer size for receiving SSH stream.
     *
     * @param size buffer size.
     * @return The previous size.
     */
    public int setSshRxBufferSize(int size) {
        int prev = sshRxBufferSize;
        sshRxBufferSize = size;
        return prev;
    }

    /**
     * Sets gerrit handler.
     *
     * @param handler the handler.
     */
    public void setHandler(GerritHandler handler) {
        this.handler = handler;
    }

    /**
     * Gets gerrit handler.
     *
     * @return the handler.
     */
    public GerritHandler getHandler() {
        return handler;
    }

    /**
     * The gerrit version we are connected to.
     *
     * @return the gerrit version.
     */
    public String getGerritVersion() {
        return gerritVersion;
    }

    /**
     * Add listener for GerrirtConnectionEvent.
     *
     * @param listener the listener.
     */
    public void addListener(ConnectionListener listener) {
        if (!listeners.add(listener)) {
            logger.warn("The connection listener was doubly-added: {}", listener);
        }
    }

    /**
     * Remove listener for GerrirtConnectionEvent.
     *
     * @param listener the listener.
     */
    public void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all connection listeners.
     */
    public void removeListeners() {
        listeners.clear();
    }

    /**
     * Returns an unmodifiable view of the set of {@link ConnectionListener}s.
     *
     * @return the set of connection listeners.
     * @see Collections#unmodifiableSet(Set)
     */
    public Set<ConnectionListener> getListenersView() {
        return Collections.unmodifiableSet(listeners);
    }

    /**
     * Sets {@link AuthenticationUpdater}.
     * @param authenticationUpdater The {@link AuthenticationUpdater}.
     */
    public void setAuthenticationUpdater(AuthenticationUpdater authenticationUpdater) {
        this.authenticationUpdater = authenticationUpdater;
    }

    /**
     * If watchdog field is not null, shut it down and put it to null.
     */
    private void nullifyWatchdog() {
        if (watchdog != null) {
            watchdog.shutdown();
            watchdog = null;
        }
    }

    /**
     * Offers lines in buffer to queue.
     *
     * @param cb a buffer to have received text data.
     * @return the line string. null if no EOL, otherwise buffer is compacted.
     */
    private String getLine(CharBuffer cb) {
        String line = null;
        int pos = cb.position();
        cb.flip();
        for (int i = 0; i < cb.length(); i++) {
            if (cb.charAt(i) == '\n') {
                line = getSubSequence(cb, 0, i).toString().trim();
                cb.position(i + 1);
                break;
            }
        }
        if (line != null) {
            cb.compact();
        } else {
            cb.clear().position(pos);
        }
        return line;
    }

    /**
     *  Get sub sequence of buffer.
     *
     *  This method avoids error in java-api-check.
     *  animal-sniffer is confused by the signature of CharBuffer.subSequence()
     *  due to declaration of this method has been changed since Java7.
     *  (abstract -> non-abstract)
     *
     * @param cb a buffer
     * @param start start of sub sequence
     * @param end end of sub sequence
     * @return sub sequence.
     */
    @IgnoreJRERequirement
    private CharSequence getSubSequence(CharBuffer cb, int start, int end) {
        return cb.subSequence(start, end);
    }

    /**
     * Main loop for connecting and reading Gerrit JSON Events and dispatching them to Workers.
     */
    @Override
    public void run() {
        logger.info("Starting Up " + gerritName);
        do {
            sshConnection = connect();
            if (sshConnection == null) {
                return;
            }
            if (watchdogTimeoutSeconds > 0 && exceptionData != null) {
                nullifyWatchdog();
                watchdog = new StreamWatchdog(this, watchdogTimeoutSeconds, exceptionData);
            }

            ChannelExec channel = null;
            try {
                logger.trace("Executing stream-events command.");
                channel = sshConnection.executeCommandChannel(CMD_STREAM_EVENTS);
                if (channel == null) {
                    throw new IOException();
                }
                Reader reader = new InputStreamReader(channel.getInputStream(), "utf-8");
                CharBuffer cb = CharBuffer.allocate(sshRxBufferSize);
                notifyConnectionEstablished();
                Provider provider = new Provider(
                        gerritName,
                        gerritHostName,
                        String.valueOf(gerritSshPort),
                        GERRIT_PROTOCOL_SCHEME_NAME,
                        gerritFrontEndUrl,
                        getGerritVersionString());
                logger.info("Ready to receive data from Gerrit: " + gerritName);
                String line;
                while (reader.read(cb) != -1) {
                    while ((line = getLine(cb)) != null) {
                        logger.debug("Data-line from Gerrit: {}", line);
                        if (handler != null) {
                            handler.post(line, provider);
                        }
                    }
                    if (shutdownInProgress || interrupted()) {
                        throw new InterruptedException("shutdown requested: " + shutdownInProgress);
                    }
                    if (watchdog != null) {
                        watchdog.signal();
                    }
                    sleep(SSH_RX_SLEEP_MILLIS);
                }
            } catch (IOException ex) {
                logger.error("Stream events command error. ", ex);
            } catch (IllegalStateException ex) {
                logger.error("Unexpected disconnection occurred after initial moment of connection. ", ex);
            } catch (InterruptedException ex) {
                logger.error("Interrupted.", ex);
            } finally {
                nullifyWatchdog();
                if (channel != null && !channel.isClosed()) {
                    logger.trace("Close channel.");
                    try {
                        channel.disconnect();
                    } catch (Exception ex) {
                        logger.warn("Error when disconnecting SSH command channel.", ex);
                    }
                }
                if (!sshConnection.isConnected()) {
                    sshConnection = null;
                }
                notifyConnectionDown();
            }
        } while (!shutdownInProgress);
        handler = null;
        logger.debug("End of GerritConnection Thread.");
    }

    /**
     * Connects to the Gerrit server and authenticates as the specified user.
     *
     * @return not null if everything is well, null if connect and reconnect failed.
     */
    private SshConnection connect() {
        if (sshConnection != null && sshConnection.isConnected()) {
            return sshConnection;
        }
        while (!shutdownInProgress) {
            SshConnection ssh = null;
            try {
                logger.debug("Connecting...");
                ssh = SshConnectionFactory.getConnection(gerritHostName, gerritSshPort, gerritProxy,
                        authentication, authenticationUpdater);
                gerritVersion  = formatVersion(ssh.executeCommand("gerrit version"));
                logger.debug("connection seems ok, returning it.");
                return ssh;
            } catch (SshConnectException sshConEx) {
                logger.error("Could not connect to Gerrit server! "
                        + "Host: {} Port: {}", gerritHostName, gerritSshPort);
                logger.error(" Proxy: {}", gerritProxy);
                logger.error(" User: {} KeyFile: {}", authentication.getUsername(), authentication.getPrivateKeyFile());
                logger.error("ConnectionException: ", sshConEx);
            } catch (SshAuthenticationException sshAuthEx) {
                logger.error("Could not authenticate to Gerrit server!"
                        + "\n\tUsername: {}\n\tKeyFile: {}\n\tPassword: {}",
                        new Object[]{authentication.getUsername(),
                                authentication.getPrivateKeyFile(),
                                authentication.getPrivateKeyFilePassword(), });
                logger.error("AuthenticationException: ", sshAuthEx);
            } catch (IOException ex) {
                logger.error("Could not connect to Gerrit server! "
                        + "Host: {} Port: {}", gerritHostName, gerritSshPort);
                logger.error(" Proxy: {}", gerritProxy);
                logger.error(" User: {} KeyFile: {}", authentication.getUsername(), authentication.getPrivateKeyFile());
                logger.error("IOException: ", ex);
            }

            if (ssh != null) {
                logger.trace("Disconnecting bad connection.");
                try {
                    //The ssh lib used is starting at least one thread for each connection.
                    //The thread isn't shutdown properly when the connection goes down,
                    //so we need to close it "manually"
                    ssh.disconnect();
                } catch (Exception ex) {
                    logger.warn("Error when disconnecting bad connection.", ex);
                } finally {
                    ssh = null;
                }
            }

            if (!shutdownInProgress) {
                //If we end up here, sleep for a while and then go back up in the loop.
                logger.trace("Sleeping for a bit.");
                try {
                    Thread.sleep(CONNECT_SLEEP);
                } catch (InterruptedException ex) {
                    logger.warn("Got interrupted while sleeping.", ex);
                }
            }
        }
        return null;
    }

    /**
     * Removes the "gerrit version " from the start of the response from gerrit.
     * @param version the response from gerrit.
     * @return the input string with "gerrit version " removed.
     */
    private String formatVersion(String version) {
        if (version == null) {
            return version;
        }
        String[] split = version.split(GERRIT_VERSION_PREFIX);
        if (split.length < 2) {
            return version.trim();
        }
        return split[1].trim();
    }

    /**
     * Gets the gerrit version.
     * @return the gerrit version as valid string.
     */
    private String getGerritVersionString() {
        String version = getGerritVersion();
        if (version == null) {
            version = "";
        }
        return version;
    }

    /**
     * The authentication credentials for ssh connection.
     *
     * @return the credentials.
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * The authentication credentials for ssh connection.
     *
     * @param authentication the credentials.
     */
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * gets the hostname where Gerrit is running.
     *
     * @return the hostname.
     */
    public String getGerritHostName() {
        return gerritHostName;
    }

    /**
     * Sets the hostname where Gerrit is running.
     *
     * @param gerritHostName the hostname.
     */
    public void setGerritHostName(String gerritHostName) {
        this.gerritHostName = gerritHostName;
    }

    /**
     * Gets the port for gerrit ssh commands.
     *
     * @return the port nr.
     */
    public int getGerritSshPort() {
        return gerritSshPort;
    }

    /**
     * Sets the port for gerrit ssh commands.
     *
     * @param gerritSshPort the port nr.
     */
    public void setGerritSshPort(int gerritSshPort) {
        this.gerritSshPort = gerritSshPort;
    }

    /**
     * Gets the proxy for gerrit ssh commands.
     *
     * @return the proxy url.
     */
    public String getGerritProxy() {
        return gerritProxy;
    }

    /**
     * Sets the proxy for gerrit ssh commands.
     *
     * @param gerritProxy the port nr.
     */
    public void setGerritProxy(String gerritProxy) {
        this.gerritProxy = gerritProxy;
    }

    /**
     * Sets the shutdown flag.
     */
    private void setShutdownInProgress() {
            this.shutdownInProgress = true;
    }

    /**
     * If the system is shutting down. I.e. the shutdown method has been called.
     *
     * @return true if so.
     */
    public boolean isShutdownInProgress() {
            return shutdownInProgress;
    }

    /**
     * If already connected.
     * @return true if already connected.
     */
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void reconnect() {
        reconnectCallCount++;
        nullifyWatchdog();
        sshConnection.disconnect();
    }


    /**
     * Count how many times {@link #reconnect()} has been called since object creation.
     *
     * @return the count.
     */
    public int getReconnectCallCount() {
        return reconnectCallCount;
    }

    /**
     * Closes the connection.
     *
     * @param join if the method should wait for the thread to finish before returning.
     */
    public void shutdown(boolean join) {
        setShutdownInProgress();
        nullifyWatchdog();
        if (sshConnection != null) {
            logger.info("Shutting down the ssh connection.");
            try {
                sshConnection.disconnect();
            } catch (Exception ex) {
                logger.warn("Error when disconnecting sshConnection.", ex);
            }
        }
        if (join) {
            try {
                this.join();
            } catch (InterruptedException ex) {
                logger.warn("Got interrupted while waiting for shutdown.", ex);
            }
        }
    }

    /**
     * Notifies all listeners of a Gerrit connection event.
     *
     * @param event the event.
     */
    public void notifyListeners(GerritConnectionEvent event) {
        for (ConnectionListener listener : listeners) {
            try {
                switch(event) {
                case GERRIT_CONNECTION_ESTABLISHED:
                    listener.connectionEstablished();
                    break;
                case GERRIT_CONNECTION_DOWN:
                    listener.connectionDown();
                    break;
                default:
                    break;
                }
            } catch (Exception ex) {
                logger.error("ConnectionListener threw Exception. ", ex);
            }
        }
    }

    /**
     * Notifies all ConnectionListeners that the connection is down.
     */
    protected void notifyConnectionDown() {
        connected = false;
        notifyListeners(GerritConnectionEvent.GERRIT_CONNECTION_DOWN);
    }

    /**
     * Notifies all ConnectionListeners that the connection is established.
     */
    protected void notifyConnectionEstablished() {
        connected = true;
        notifyListeners(GerritConnectionEvent.GERRIT_CONNECTION_ESTABLISHED);
    }
}
