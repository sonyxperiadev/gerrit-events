package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;

import java.io.IOException;

/**
 * {@link GerritQueryHandler} with a persisted SSH connection.
 * Saves the performance overhead of creating a new connection for each query.
 */
public class GerritQueryHandlerWithPersistedConnection extends GerritQueryHandler {

    private SshConnection activeConnection = null;

    /**
     * Creates a {@link GerritQueryHandler} with persisted SSH connection.
     *
     * @param gerritHostName    the hostName
     * @param gerritSshPort     the ssh port that the gerrit server listens to.
     * @param gerritProxy       the ssh Proxy url
     * @param authentication    the authentication credentials.
     * @param connectionTimeout the connection timeout.
     */
    public GerritQueryHandlerWithPersistedConnection(String gerritHostName, int gerritSshPort,
                                                     String gerritProxy, Authentication authentication,
                                                     int connectionTimeout) {
        super(gerritHostName, gerritSshPort, gerritProxy, authentication, connectionTimeout);
    }

    /**
     * Returns a fresh SSH connection if the persisted one is not valid.
     * Otherwise returns the existing connection.
     *
     * @return an active SSH connection
     * @throws IOException for IO issues
     */
    @Override
    protected SshConnection getConnection() throws IOException {
        if (!isPersistedConnectionValid()) {
            activeConnection = super.getConnection();
            logger.trace("SSH connection is not valid anymore, a new one was created.");
        }
        return activeConnection;
    }

    /**
     * Do not cleanup the current connection since we want to persist it.
     *
     * @param ssh the current SSH connection
     */
    @Override
    protected void cleanupConnection(SshConnection ssh) {
        // do nothing
    }

    /**
     * Checks if the persisted connection is still valid.
     *
     * @return true if the persistent connection is valid.
     */
    private boolean isPersistedConnectionValid() {
        return activeConnection != null && activeConnection.isConnected();
    }

    /**
     * Disconnects the persisted connection.
     */
    public void disconnect() {
        if (isPersistedConnectionValid()) {
            activeConnection.disconnect();
            logger.trace("The persisted SSH connection was disconnected.");
        }
    }
}
