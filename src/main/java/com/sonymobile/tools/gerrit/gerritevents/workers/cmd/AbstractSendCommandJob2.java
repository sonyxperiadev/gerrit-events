/*
 * The MIT License
 *
 * Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 * Copyright 2014 Sony Mobile Communications AB. All rights reserved.
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
     * @return true if there were no exceptions when sending.
     */
    @Override
    public boolean sendCommand(String command) {
        try {
            SshConnection ssh = SshConnectionFactory.getConnection(config.getGerritHostName(),
                    config.getGerritSshPort(), config.getGerritProxy(), config.getGerritAuthentication());
            ssh.executeCommand(command);
            ssh.disconnect();
            return true;
        } catch (Exception ex) {
            logger.error("Could not run command " + command, ex);
            return false;
        }
    }

    /**
     * Sends a command to the Gerrit server, returning the output from the command.
     * @param command the command.
     * @return the output from the command.
     */
    @Override
    public String sendCommandStr(String command) {
        try {
            SshConnection ssh = SshConnectionFactory.getConnection(config.getGerritHostName(),
                    config.getGerritSshPort(), config.getGerritProxy(), config.getGerritAuthentication());
            String str = ssh.executeCommand(command);
            ssh.disconnect();
            return str;
        } catch (Exception ex) {
            logger.error("Could not run command " + command, ex);
            return null;
        }
    }

    /**
     * Sends a command to the Gerrit server.
     * @param command the command.
     * @return true if there were no exceptions when sending.
     * @throws IOException if error.
     * @throws InterruptedException if interrupted.
     */
    @Override
    public void sendCommand2(String command) throws IOException, InterruptedException {
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
     * Sends a command to the Gerrit server, returning the output from the command.
     * This is blocking method until command is actually sent or intrrupted.
     * @throws IOException if error.
     * @throws InterruptedException if interrupted.
     */
    @Override
    public String sendCommandStr2(String command) throws IOException, InterruptedException {
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
