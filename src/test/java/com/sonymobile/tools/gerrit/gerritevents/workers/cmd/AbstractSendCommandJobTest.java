/*
 * The MIT License
 *
 * Copyright 2014 rinrinne a.k.a. rin_ne All rights reserved.
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

package com.sonymobile.tools.gerrit.gerritevents.workers.cmd;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import com.sonymobile.tools.gerrit.gerritevents.GerritConnectionConfig2;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshException;


// CS IGNORE AvoidStarImport FOR NEXT 4 LINES. REASON: Test code.
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;

/**
 * Tests {@link com.sonymobile.tools.gerrit.gerritevents.workers.cmd.AbstractSendCommandJob}.
 * @author rinrinne (rinrin.ne@gmail.com)
 */
public class AbstractSendCommandJobTest {
    private static GerritConnectionConfig2 mockConfig;

    //CS IGNORE MagicNumber FOR NEXT 400 LINES. REASON: Test data.

    /**
     * Setup before all tests in this class.
     *
     * @throws IOException if so.
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        mockConfig = mock(GerritConnectionConfig2.class);
    }

    /**
     * Tests {@link sendCommand()}.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommand() throws IOException {
        SshConnection mockSshConnection = mock(SshConnection.class);
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenReturn(mockSshConnection);
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThat(job.sendCommand("test command"), is(true));
            verify(mockSshConnection).executeCommand(anyString());
            verify(mockSshConnection).disconnect();
        }
    }

    /**
     * Tests {@link sendCommand()} without connection.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandNoConnection() throws IOException {
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenThrow(new IOException());
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThat(job.sendCommand("test command"), is(false));
        }
    }

    /**
     * Tests {@link sendCommand()} with error.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandWithError() throws IOException {
        SshConnection mockSshConnection = mock(SshConnection.class);
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenReturn(mockSshConnection);
            when(mockSshConnection.executeCommand(anyString())).thenThrow(new SshException());
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThat(job.sendCommand("test command"), is(false));
            verify(mockSshConnection).executeCommand(anyString());
            verify(mockSshConnection).disconnect();
        }
    }

    /**
     * Tests {@link sendCommandStr()}.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandStr() throws IOException {
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(mockSshConnection.executeCommand(anyString())).thenReturn("OK");
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenReturn(mockSshConnection);
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThat(job.sendCommandStr("test command"), is("OK"));
            verify(mockSshConnection).executeCommand(anyString());
            verify(mockSshConnection).disconnect();
        }
    }

    /**
     * Tests {@link sendCommandStr()} without connection.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandStrNoConnection() throws IOException {
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenThrow(new IOException());
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertNull(job.sendCommandStr("test command"));
        }
    }

    /**
     * Tests {@link sendCommandStr()} with error.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandStrWithError() throws IOException {
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(mockSshConnection.executeCommand(anyString())).thenReturn("OK");
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenReturn(mockSshConnection);
            when(mockSshConnection.executeCommand(anyString())).thenThrow(new SshException());
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertNull(job.sendCommandStr("test command"));
            verify(mockSshConnection).executeCommand(anyString());
            verify(mockSshConnection).disconnect();
        }
    }

    /**
     * Tests {@link sendCommand2()}.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommand2() throws IOException {
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(mockSshConnection.executeCommand(anyString())).thenReturn("OK");
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenReturn(mockSshConnection);
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThat(job.sendCommand2("test command"), is("OK"));
            verify(mockSshConnection).executeCommand(anyString());
            verify(mockSshConnection).disconnect();
        }
    }

    /**
     * Tests {@link sendCommand2()} without connection.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommand2WithNoConnection() throws IOException {
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenThrow(new IOException());
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThrows(IOException.class, () -> job.sendCommand2("test command"));
        }
    }

    /**
     * Tests {@link sendCommand2()} with exception.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommand2WithException() throws IOException {
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(mockSshConnection.executeCommand(anyString())).thenReturn("OK");
        try (MockedStatic<SshConnectionFactory> factory = mockStatic(SshConnectionFactory.class)) {
            factory.when(() -> SshConnectionFactory.getConnection(nullable(String.class), anyInt(),
                nullable(String.class), nullable(Authentication.class)))
                .thenReturn(mockSshConnection);
            when(mockSshConnection.executeCommand(anyString())).thenThrow(new SshException());
            AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
            Assert.assertThrows(IOException.class, () -> job.sendCommand2("test command"));
            verify(mockSshConnection).executeCommand(anyString());
            verify(mockSshConnection).disconnect();
        }
    }
    /**
     * An implementation class of AbstractSendCommandJob.
     * @author rinrinne (rinrin.ne@gmail.com)
     */
    public static class AbstractSendCommandJobImpl extends AbstractSendCommandJob {
        /**
         * Default constructor.
         * @param config the config.
         */
        protected AbstractSendCommandJobImpl(GerritConnectionConfig2 config) {
            super(config);
        }
        @Override
        public void run() {
        }
    }
}
