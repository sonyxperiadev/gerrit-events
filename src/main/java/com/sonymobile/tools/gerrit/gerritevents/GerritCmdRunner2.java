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

package com.sonymobile.tools.gerrit.gerritevents;

import java.io.IOException;

/**
 * @author rinrinne (rinrin.ne@gmail.com)
 */
public interface GerritCmdRunner2 extends GerritCmdRunner {
    /**
     * Runs a command on the gerrit server.
     * @param command the command.
     * @throws IOException if error.
     * @throws InterruptedException if interrupted.
     */
    void sendCommand2(String command) throws IOException, InterruptedException;

    /**
     * Runs a command on the gerrit server and returns the output from the command.
     * @param command the command.
     * @return the output of the command, or null if something went wrong.
     * @throws IOException if error.
     * @throws InterruptedException if interrupted.
     */
    String sendCommandStr2(String command) throws IOException, InterruptedException;
}
