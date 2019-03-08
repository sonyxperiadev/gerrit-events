package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.Reader;
import java.io.StringReader;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test base for testing {@link com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandler} implementations.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SshConnectionFactory.class)
@PowerMockIgnore("org.slf4j.*") // Prevent warning about multiple sl4fj binding
public abstract class GerritQueryHandlerTestBase {

    GerritQueryHandler queryHandler;

    SshConnection sshConnectionMock;

    /**
     * Prepare mock for sshConnection.
     *
     * @throws Exception when something wrong.
     */
    @Before
    public void setUp() throws Exception {

        sshConnectionMock = mock(SshConnection.class);

        PowerMockito.mockStatic(SshConnectionFactory.class);
        PowerMockito.doReturn(sshConnectionMock).when(SshConnectionFactory.class, "getConnection",
                isA(String.class), isA(Integer.class), isA(String.class), isA(Authentication.class), isA(Integer.class));

        when(sshConnectionMock.isConnected()).thenReturn(true);
        when(sshConnectionMock.executeCommandReader(anyString())).thenAnswer(new Answer<Reader>() {

            @Override
            public Reader answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new StringReader("{\"project\":\"test\"}");
            }
        });
    }
}
