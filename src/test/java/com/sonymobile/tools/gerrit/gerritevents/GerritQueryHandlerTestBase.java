package com.sonymobile.tools.gerrit.gerritevents;

import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Reader;
import java.io.StringReader;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Test base for testing {@link com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandler} implementations.
 */
public abstract class GerritQueryHandlerTestBase {

    GerritQueryHandler queryHandler;

    SshConnection sshConnectionMock;

    MockedStatic<SshConnectionFactory> sshConnectionFactoryMock;

    /**
     * Prepare mock for sshConnection.
     *
     * @throws Exception when something wrong.
     */
    @Before
    public void setUp() throws Exception {

        sshConnectionMock = mock(SshConnection.class);

        sshConnectionFactoryMock = mockStatic(SshConnectionFactory.class);
        sshConnectionFactoryMock.when(() -> SshConnectionFactory.getConnection(
                isA(String.class), anyInt(), isA(String.class), isA(Authentication.class), anyInt()))
                .thenReturn(sshConnectionMock);

        when(sshConnectionMock.isConnected()).thenReturn(true);
        when(sshConnectionMock.executeCommandReader(anyString())).thenAnswer(new Answer<Reader>() {

            @Override
            public Reader answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new StringReader("{\"project\":\"test\"}");
            }
        });
    }

    /**
     * Releases the static mock. A {@link MockedStatic} is scoped to the thread that opened it and
     * must be closed, otherwise the next test on this thread fails to register its own static mock.
     */
    @After
    public void tearDown() {
        if (sshConnectionFactoryMock != null) {
            sshConnectionFactoryMock.close();
        }
    }
}
