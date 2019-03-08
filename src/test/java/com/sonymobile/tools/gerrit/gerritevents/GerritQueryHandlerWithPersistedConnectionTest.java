package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Tests for {@link com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandlerWithPersistedConnection}.
 *
 * @author : apetres
 */
public class GerritQueryHandlerWithPersistedConnectionTest extends GerritQueryHandlerTestBase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        queryHandler = new GerritQueryHandlerWithPersistedConnection("", 0, "", new Authentication(null, ""), 0);
    }

    /**
     * Test {@Link GerritQueryHandlerWithPersistedConnection.queryJava} creates only a single SSH connection
     * regardless of the number of times it is called.
     *
     * @throws Exception when something wrong.
     */
    @Test
    public void sshConnectionIsCreatedOnlyOnce() throws Exception {

        queryHandler.queryJava("X");
        queryHandler.queryJava("Y");

        verifyStatic(SshConnectionFactory.class, times(1));
        SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), any(Authentication.class), anyInt());
    }
}
