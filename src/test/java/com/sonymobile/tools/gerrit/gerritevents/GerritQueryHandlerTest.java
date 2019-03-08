/*
 * The MIT License
 *
 * Copyright 2013 Slawomir Jaranowski. All rights reserved.
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

import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Tests for {@link com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandler}.
 *
 * @author : slawomir.jaranowski
 */
public class GerritQueryHandlerTest extends GerritQueryHandlerTestBase {


    /**
     * Create GerritQueryHandler for test.
     *
     * @throws Exception when something wrong.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        queryHandler = new GerritQueryHandler("", 0, "", new Authentication(null, ""), 0);
    }

    /**
     * Test {@Link GerritQueryHandler.queryJava} and {@Link GerritQueryHandler.queryJson}
     * with only one parameter.
     *
     * @throws Exception when something wrong.
     */

    @Test
    public void testQueryStr() throws Exception {

        queryHandler.queryJava("X");
        queryHandler.queryJson("X");

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --patch-sets --current-patch-set \"X\"");

    }

    /**
     * Test {@Link GerritQueryHandler.queryJava} and {@Link GerritQueryHandler.queryJson}
     * with 4 parameter list.
     *
     * @throws Exception when something wrong.
     */
    @Test
    public void testQueryStr3Bool() throws Exception {

        queryHandler.queryJava("X", true, true, true);
        queryHandler.queryJson("X", true, true, true);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --patch-sets --current-patch-set --files \"X\"");

        queryHandler.queryJava("X", true, true, false);
        queryHandler.queryJson("X", true, true, false);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --patch-sets --current-patch-set \"X\"");

        queryHandler.queryJava("X", true, false, true);
        queryHandler.queryJson("X", true, false, true);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --patch-sets --files \"X\"");

        queryHandler.queryJava("X", true, false, false);
        queryHandler.queryJson("X", true, false, false);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --patch-sets \"X\"");

        queryHandler.queryJava("X", false, true, true);
        queryHandler.queryJson("X", false, true, true);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --current-patch-set --files \"X\"");

        queryHandler.queryJava("X", false, true, false);
        queryHandler.queryJson("X", false, true, false);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --current-patch-set \"X\"");

        queryHandler.queryJava("X", false, false, true);
        queryHandler.queryJson("X", false, false, true);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --files \"X\"");

        queryHandler.queryJava("X", false, false, false);
        queryHandler.queryJson("X", false, false, false);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON \"X\"");

    }

    /**
     * Test {@Link GerritQueryHandler.queryJava} and {@Link GerritQueryHandler.queryJson}
     * with full parameter list.
     *
     * @throws Exception when something wrong.
     */
    @Test
    public void testQueryStr4Bool() throws Exception {

        queryHandler.queryJava("X", false, false, false, true);
        queryHandler.queryJson("X", false, false, false, true);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON --commit-message \"X\"");

        queryHandler.queryJava("X", false, false, false, false);
        queryHandler.queryJson("X", false, false, false, false);

        verify(sshConnectionMock, times(2))
                .executeCommandReader("gerrit query --format=JSON \"X\"");

    }

    /**
     * Test {@Link GerritQueryHandler.queryFiles}.
     *
     * @throws Exception when something wrong.
     */
    @Test
    public void testQueryFiles() throws Exception {

        queryHandler.queryFiles("X");

        verify(sshConnectionMock)
                .executeCommandReader("gerrit query --format=JSON --current-patch-set --files \"X\"");

    }

    /**
     * Test {@Link GerritQueryHandler.queryJava} creates a new SSH connection each time it is called.
     *
     * @throws Exception when something wrong.
     */
    @Test
    public void sshConnectionIsRecreatedForEachQuery() throws Exception {

        queryHandler.queryJava("X");
        queryHandler.queryJava("Y");

        verifyStatic(SshConnectionFactory.class, times(2));
        SshConnectionFactory.getConnection(anyString(), anyInt(), anyString(), any(Authentication.class), anyInt());
    }
}
