/*
 * The MIT License
 *
 * Copyright 2010 Sony Mobile Communications Inc. All rights reserved.
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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.BRANCH;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.ID;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NUMBER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.SUBJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.OWNER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.URL;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.EMAIL;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NAME;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.COMMENTS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.MESSAGE;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REVIEWER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.CREATED_ON;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.LAST_UPDATED;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change}.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class ChangeTest {
    private Account account;
    private JSONObject jsonAccount;

    /**
     * Sets up a dummy Account object and a JSON version before each test.
     */
    @Before
    public void setUp() {
        account = new Account();
        account.setEmail("robert.sandell@sonyericsson.com");
        account.setName("Bobby");
        jsonAccount = new JSONObject();
        jsonAccount.put(EMAIL, account.getEmail());
        jsonAccount.put(NAME, account.getName());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JSONObject)}.
     * @throws Exception if so.
     */
    @Test
    public void testFromJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "project");
        json.put(BRANCH, "branch");
        json.put(ID, "I2343434344");
        json.put(NUMBER, "100");
        json.put(SUBJECT, "subject");
        json.put(OWNER, jsonAccount);
        json.put(URL, "http://localhost:8080");
        Change change = new Change();
        change.fromJson(json);

        assertEquals(change.getProject(), "project");
        assertEquals(change.getBranch(), "branch");
        assertEquals(change.getId(), "I2343434344");
        assertEquals(change.getNumber(), "100");
        assertEquals(change.getSubject(), "subject");
        assertTrue(change.getOwner().equals(account));
        assertEquals(change.getUrl(), "http://localhost:8080");
        assertNull(change.getComments());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JSONObject)}.
     * Without any JSON URL data.
     * @throws Exception if so.
     */
    @Test
    public void testFromJsonNoUrl() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "project");
        json.put(BRANCH, "branch");
        json.put(ID, "I2343434344");
        json.put(NUMBER, "100");
        json.put(SUBJECT, "subject");
        json.put(OWNER, jsonAccount);
        Change change = new Change();
        change.fromJson(json);

        assertEquals(change.getProject(), "project");
        assertEquals(change.getBranch(), "branch");
        assertEquals(change.getId(), "I2343434344");
        assertEquals(change.getNumber(), "100");
        assertEquals(change.getSubject(), "subject");
        assertTrue(change.getOwner().equals(account));
        assertNull(change.getUrl());
        assertNull(change.getComments());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JSONObject)}.
     * With comments.
     * @throws Exception if so.
     */
    @Test
    public void testFromJsonWithEmptyComments() throws Exception {
        JSONArray jsonComments = new JSONArray();
        JSONObject json = new JSONObject();
        json.put(COMMENTS, jsonComments);
        Change change = new Change();
        change.fromJson(json);

        assertNotNull(change.getComments());
        assertTrue(change.getComments().isEmpty());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JSONObject)}.
     * With comments.
     * @throws Exception if so.
     */
    @Test
    public void testFromJsonWithNonEmptyComments() throws Exception {
        JSONObject jsonComment = new JSONObject();
        jsonComment.put(MESSAGE, "Some review message");
        jsonComment.put(REVIEWER, jsonAccount);
        JSONArray jsonComments = new JSONArray();
        jsonComments.add(jsonComment);
        JSONObject json = new JSONObject();
        json.put(COMMENTS, jsonComments);
        Change change = new Change();
        change.fromJson(json);

        assertNotNull(change.getComments());
        assertEquals(1, change.getComments().size());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JSONObject)}.
     * With date values, createdOn and lastUpdated.
     * @throws Exception if so.
     */
    @Test
    // CS IGNORE MagicNumber FOR NEXT 3 LINES. REASON: TestData
    public void testFromJsonWithDateValues() throws Exception {
        long createdOn = 100000000L;
        long lastUpdated = 110000000L;
        JSONObject json = new JSONObject();
        //In gerrit, time is written in seconds, not milliseconds.
        long createdOnInMilliseconds = TimeUnit.SECONDS.toMillis(createdOn);
        Date createdOnAsDate = new Date(createdOnInMilliseconds);
        //In gerrit, time is written in seconds, not milliseconds.
        long lastUpdatedInMilliseconds = TimeUnit.SECONDS.toMillis(lastUpdated);
        Date lastUpdatedAsDate = new Date(lastUpdatedInMilliseconds);
        json.put(CREATED_ON, createdOn);
        json.put(LAST_UPDATED, lastUpdated);
        Change change = new Change();
        change.fromJson(json);

        assertEquals(createdOnAsDate, change.getCreatedOn());
        assertEquals(lastUpdatedAsDate, change.getLastUpdated());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change#Change(net.sf.json.JSONObject)}.
     * @throws Exception if so.
     */
    @Test
    public void testInitJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "project");
        json.put(BRANCH, "branch");
        json.put(ID, "I2343434344");
        json.put(NUMBER, "100");
        json.put(SUBJECT, "subject");
        json.put(OWNER, jsonAccount);
        json.put(URL, "http://localhost:8080");
        Change change = new Change(json);

        assertEquals(change.getProject(), "project");
        assertEquals(change.getBranch(), "branch");
        assertEquals(change.getId(), "I2343434344");
        assertEquals(change.getNumber(), "100");
        assertEquals(change.getSubject(), "subject");
        assertTrue(change.getOwner().equals(account));
        assertEquals(change.getUrl(), "http://localhost:8080");
        assertNull(change.getComments());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change#equals(Object)}.
     * @throws Exception if so.
     */
    @Test
    public void testEquals() throws Exception {
        JSONObject json = new JSONObject();
        json.put(PROJECT, "project");
        json.put(BRANCH, "branch");
        json.put(ID, "I2343434344");
        json.put(NUMBER, "100");
        json.put(SUBJECT, "subject");
        json.put(OWNER, jsonAccount);
        json.put(URL, "http://localhost:8080");
        Change change = new Change(json);

        Change change2 = new Change();
        change2.setProject("project");
        change2.setBranch("branch");
        change2.setId("I2343434344");
        change2.setNumber("100");
        change2.setSubject("subject");
        change2.setOwner(account);
        change2.setUrl("http://localhost:8080");

        assertTrue(change.equals(change2));
    }
}
