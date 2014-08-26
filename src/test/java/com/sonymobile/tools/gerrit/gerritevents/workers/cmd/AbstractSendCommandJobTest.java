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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sonymobile.tools.gerrit.gerritevents.GerritConnectionConfig2;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshException;


// CS IGNORE AvoidStarImport FOR NEXT 4 LINES. REASON: Test code.
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.mockito.Matchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests {@link com.sonymobile.tools.gerrit.gerritevents.workers.cmd.AbstractSendCommandJob}.
 * @author rinrinne (rinrin.ne@gmail.com)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SshConnectionFactory.class)
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
        PowerMockito.mockStatic(SshConnectionFactory.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenReturn(mockSshConnection);
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        Assert.assertThat(job.sendCommand("test command"), is(true));
        Mockito.verify(mockSshConnection).executeCommand(anyString());
        Mockito.verify(mockSshConnection).disconnect();
    }

    /**
     * Tests {@link sendCommand()} without connection.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandNoConnection() throws IOException {
        PowerMockito.mockStatic(SshConnectionFactory.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenThrow(new IOException());
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        Assert.assertThat(job.sendCommand("test command"), is(false));
    }

    /**
     * Tests {@link sendCommand()} with error.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandWithError() throws IOException {
        PowerMockito.mockStatic(SshConnectionFactory.class);
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenReturn(mockSshConnection);
        when(mockSshConnection.executeCommand(anyString())).thenThrow(new SshException());
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        Assert.assertThat(job.sendCommand("test command"), is(false));
        Mockito.verify(mockSshConnection).executeCommand(anyString());
        Mockito.verify(mockSshConnection).disconnect();
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
        PowerMockito.mockStatic(SshConnectionFactory.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenReturn(mockSshConnection);
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        Assert.assertThat(job.sendCommandStr("test command"), is("OK"));
        Mockito.verify(mockSshConnection).executeCommand(anyString());
        Mockito.verify(mockSshConnection).disconnect();
    }

    /**
     * Tests {@link sendCommandStr()} without connection.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandStrNoConnection() throws IOException {
        PowerMockito.mockStatic(SshConnectionFactory.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenThrow(new IOException());
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        Assert.assertNull(job.sendCommandStr("test command"));
    }

    /**
     * Tests {@link sendCommandStr()} with error.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommandStrWithError() throws IOException {
        PowerMockito.mockStatic(SshConnectionFactory.class);
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(mockSshConnection.executeCommand(anyString())).thenReturn("OK");
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenReturn(mockSshConnection);
        when(mockSshConnection.executeCommand(anyString())).thenThrow(new SshException());
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        Assert.assertNull(job.sendCommandStr("test command"));
        Mockito.verify(mockSshConnection).executeCommand(anyString());
        Mockito.verify(mockSshConnection).disconnect();
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
        PowerMockito.mockStatic(SshConnectionFactory.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenReturn(mockSshConnection);
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        String str = null;
        boolean catched = false;
        try {
            str = job.sendCommand2("test command");
        } catch (IOException ex) {
            catched = true;
        }
        Assert.assertThat(str, is("OK"));
        Assert.assertThat(catched, is(false));
        Mockito.verify(mockSshConnection).executeCommand(anyString());
        Mockito.verify(mockSshConnection).disconnect();
    }

    /**
     * Tests {@link sendCommand2()} without connection.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommand2WithNoConnection() throws IOException {
        PowerMockito.mockStatic(SshConnectionFactory.class);
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenThrow(new IOException());
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        String str = null;
        boolean catched = false;
        try {
            str = job.sendCommand2("test command");
        } catch (IOException ex) {
            catched = true;
        }
        Assert.assertNull(str);
        Assert.assertThat(catched, is(true));
    }

    /**
     * Tests {@link sendCommand2()} with exception.
     *
     * @throws IOException if so.
     */
    @Test
    public void testSendCommand2WithException() throws IOException {
        PowerMockito.mockStatic(SshConnectionFactory.class);
        SshConnection mockSshConnection = mock(SshConnection.class);
        when(mockSshConnection.executeCommand(anyString())).thenReturn("OK");
        when(SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), (Authentication)anyObject()))
            .thenReturn(mockSshConnection);
        when(mockSshConnection.executeCommand(anyString())).thenThrow(new SshException());
        AbstractSendCommandJob job = new AbstractSendCommandJobImpl(mockConfig);
        String str = null;
        boolean catched = false;
        try {
            str = job.sendCommand2("test command");
        } catch (IOException ex) {
            catched = true;
        }
        Assert.assertNull(str);
        Assert.assertThat(catched, is(true));
        Mockito.verify(mockSshConnection).executeCommand(anyString());
        Mockito.verify(mockSshConnection).disconnect();
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
