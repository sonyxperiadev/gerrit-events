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

import com.sonymobile.tools.gerrit.gerritevents.GerritCmdRunner2;
import com.sonymobile.tools.gerrit.gerritevents.GerritConnectionConfig;
import com.sonymobile.tools.gerrit.gerritevents.GerritConnectionConfig2;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;

/**
 * An abstract Job implementation
 * to be scheduled on {@link com.sonymobile.tools.gerrit.gerritevents.GerritSendCommandQueue}.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public abstract class AbstractSendCommandJob2 extends AbstractSendCommandJob implements GerritCmdRunner2 {

    /**
     * Standard constructor taking the latest configuration.
     * @param config the connection config.
     */
    protected AbstractSendCommandJob2(GerritConnectionConfig2 config) {
        super(config);
    }

    /**
     * Sends a command to the Gerrit server.
     * @param command the command.
     * @throws IOException if error.
     * @throws InterruptedException if interrupted.
     */
    @Override
    public void sendCommand2(String command) throws IOException, InterruptedException {
        GerritConnectionConfig config = getConfig();
        SshConnection ssh = null;
        try {
            ssh = SshConnectionFactory.getConnection(config.getGerritHostName(),
                    config.getGerritSshPort(), config.getGerritProxy(), config.getGerritAuthentication());
            ssh.executeCommand(command);
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                throw (InterruptedException)ex;
            } else {
                throw new IOException("Error during sending command", ex);
            }
        } finally {
            if (ssh != null) {
                ssh.disconnect();
            }
        }
    }

    /**
     * Runs a command on the gerrit server and returns the output from the command.
     * @param command the command.
     * @return the output of the command, or null if something went wrong.
     * @throws IOException if error.
     * @throws InterruptedException if interrupted.
     */
    @Override
    public String sendCommandStr2(String command) throws IOException, InterruptedException {
        GerritConnectionConfig config = getConfig();
        String str = null;
        SshConnection ssh = null;
        try {
            ssh = SshConnectionFactory.getConnection(config.getGerritHostName(),
                    config.getGerritSshPort(), config.getGerritProxy(), config.getGerritAuthentication());
            str = ssh.executeCommand(command);
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                throw (InterruptedException)ex;
            } else {
                throw new IOException("Error during sending command", ex);
            }
        } finally {
            if (ssh != null) {
                ssh.disconnect();
            }
        }
        return str;
    }
}
