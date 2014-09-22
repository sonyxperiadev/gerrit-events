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
import java.util.concurrent.Callable;

import com.sonymobile.tools.gerrit.gerritevents.GerritConnectionConfig2;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract Job implementation
 * to be scheduled on {@link com.sonymobile.tools.gerrit.gerritevents.GerritSendCommandQueue}.
 *
 * @author rinrinne (rinrin.ne@gmail.com)
 */
public abstract class AbstractSendCommandJob2 implements Callable<String> {

    /**
     * An instance of a logger for sub-classes to use.
     */
    protected static Logger logger = LoggerFactory.getLogger(AbstractSendCommandJob2.class);

    /**
     * The Gerrit hostname.
     */
    protected final String host;
    /**
     * The Gerrit SSH port.
     */
    protected final int port;
    /**
     * The proxy for Gerrit SSH.
     */
    protected final String proxy;
    /**
     * The authentication for Gerrit.
     */
    protected final Authentication auth;

    /**
     * Standard constructor taking the latest configuration.
     * @param config the connection config.
     */
    protected AbstractSendCommandJob2(GerritConnectionConfig2 config) {
        this.host = config.getGerritHostName();
        this.port = config.getGerritSshPort();
        this.proxy = config.getGerritProxy();
        this.auth = config.getGerritAuthentication();
    }

    /**
     * Creates Gerrit command.
     * @return the Gerrit command.
     */
    protected abstract String createGerritCommand();

    @Override
    public String call() throws IOException {
        String str = null;
        SshConnection ssh = null;
        try {
            ssh = SshConnectionFactory.getConnection(host, port, proxy, auth);
            str = ssh.executeCommand(createGerritCommand());
        } catch (Exception ex) {
            throw new IOException("Error during sending command", ex);
        } finally {
            if (ssh != null) {
                ssh.disconnect();
            }
        }
        return str;
    }
}
