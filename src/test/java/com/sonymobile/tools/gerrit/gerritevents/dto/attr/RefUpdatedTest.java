/*
 * The MIT License
 *
 * Copyright 2015 Ericsson. All rights reserved.
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

package com.sonymobile.tools.gerrit.gerritevents.dto.attr;

import net.sf.json.JSONObject;
import org.junit.Test;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.OLDREV;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NEWREV;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REFNAME;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROJECT;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.RefUpdate}.
 */
public class RefUpdatedTest {

    /**
     * Test to verify that getRefName returns branch name when
     * event contains long name.
     * @throws Exception if so.
     */
    @Test
    public void testLongRefName() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "abc");
        json.put(OLDREV, "ad123456789");
        json.put(NEWREV, "cd123456789");
        json.put(REFNAME, "refs/heads/master");
        RefUpdate refUpdate = new RefUpdate();
        refUpdate.fromJson(json);

        assertEquals("master", refUpdate.getRefName());
        assertEquals("refs/heads/master", refUpdate.getRef());
    }

    /**
     * Test to verify that getRefName returns branch name when
     * event contains short name.
     * @throws Exception if so.
     */
    @Test
    public void testShortRefName() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "abc");
        json.put(OLDREV, "ad123456789");
        json.put(NEWREV, "cd123456789");
        json.put(REFNAME, "master");
        RefUpdate refUpdate = new RefUpdate();
        refUpdate.fromJson(json);

        assertEquals("master", refUpdate.getRefName());
        assertEquals("refs/heads/master", refUpdate.getRef());
    }

    /**
     * Test to verify that getRefName returns complete refname when
     * event corresponds to a tag.
     * @throws Exception if so.
     */
    @Test
    public void testTags() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "abc");
        json.put(OLDREV, "ad123456789");
        json.put(NEWREV, "cd123456789");
        json.put(REFNAME, "refs/tags/abc");
        RefUpdate refUpdate = new RefUpdate();
        refUpdate.fromJson(json);

        assertEquals("refs/tags/abc", refUpdate.getRefName());
        assertEquals("refs/tags/abc", refUpdate.getRef());
    }
}
