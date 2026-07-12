/*
 *  The MIT License
 *
 *  Copyright 2026 Amarula Solutions All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;

//CS IGNORE MagicNumber FOR NEXT 400 LINES. REASON: TestData

/**
 * Tests for {@link GerritRestQueryHandler}.
 *
 * @author Michael Trimarchi
 */
public class GerritRestQueryHandlerTest {

    private GerritRestQueryHandler handler;
    private static final String FRONTEND_URL = "https://gerrit.example.com/";

    /**
     * Set up test fixtures.
     */
    @Before
    public void setUp() {
        handler = new GerritRestQueryHandler(
                FRONTEND_URL,
                new UsernamePasswordCredentials("user", "pass"),
                "gerrit.example.com",
                29418,
                "",
                new com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication(null, "user", null));
    }

    // ---- Construction tests ----

    /**
     * Tests construction completes without error.
     */
    @Test
    public void testConstruction() {
        assertNotNull(handler);
    }

    /**
     * Tests that GerritRestQueryHandler extends GerritQueryHandler.
     */
    @Test
    public void testExtendsGerritQueryHandler() {
        assertTrue(handler instanceof GerritQueryHandler);
    }

    // ---- REST-to-SSH query format conversion tests ----

    /**
     * Tests conversion of a minimal REST change to query format.
     * Uses {@code convertSingleChange} via Whitebox for direct verification.
     */
    @Test
    public void testConvertMinimalChange() throws Exception {
        JSONObject rest = new JSONObject();
        rest.put("id", "myProject~master~Iabc123def456");
        rest.put("change_id", "Iabc123def456");
        rest.put("project", "myProject");
        rest.put("branch", "master");
        rest.put("_number", 42);
        rest.put("subject", "Fix the thing");
        rest.put("status", "NEW");
        rest.put("created", "2023-01-15 10:00:00");
        rest.put("updated", "2023-01-15 11:00:00");
        rest.put("current_revision", "abc123def456");
        JSONObject owner = new JSONObject();
        owner.put("name", "Test User");
        owner.put("email", "test@example.com");
        rest.put("owner", owner);

        JSONObject revisions = new JSONObject();
        JSONObject revObj = new JSONObject();
        revObj.put("_number", 1);
        revObj.put("ref", "refs/changes/42/1");
        revObj.put("kind", "REWORK");
        revObj.put("created", "2023-01-15 10:00:00");
        JSONObject uploader = new JSONObject();
        uploader.put("name", "Test User");
        uploader.put("email", "test@example.com");
        revObj.put("uploader", uploader);
        revisions.put("abc123def456", revObj);
        rest.put("revisions", revisions);

        JSONObject q = (JSONObject)org.powermock.reflect.Whitebox.invokeMethod(
                handler, "convertSingleChange", rest);
        assertNotNull(q);

        assertEquals("myProject", q.getString("project"));
        assertEquals("master", q.getString("branch"));
        assertEquals("Iabc123def456", q.getString("id"));
        assertEquals("42", q.getString("number"));
        assertEquals("Fix the thing", q.getString("subject"));
        assertEquals("NEW", q.getString("status"));
        assertTrue(q.getBoolean("open"));
        assertEquals(FRONTEND_URL + "42", q.getString("url"));
        assertTrue(q.has("owner"));
        assertTrue(q.has("currentPatchSet"));
        assertTrue(q.getLong("createdOn") > 0);
        assertTrue(q.getLong("lastUpdated") > 0);

        JSONObject patchSet = q.getJSONObject("currentPatchSet");
        assertEquals("1", patchSet.getString("number"));
        assertEquals("abc123def456", patchSet.getString("revision"));
        assertEquals("refs/changes/42/1", patchSet.getString("ref"));
        assertEquals("REWORK", patchSet.getString("kind"));
        assertTrue(patchSet.has("uploader"));
        assertTrue(patchSet.getLong("createdOn") > 0);
    }

    /**
     * Tests conversion with commit message in revision.
     */
    @Test
    public void testConvertWithCommitMessage() throws Exception {
        JSONObject rest = new JSONObject();
        rest.put("id", "proj~master~Ixyz789");
        rest.put("change_id", "Ixyz789");
        rest.put("project", "proj");
        rest.put("branch", "master");
        rest.put("_number", 1);
        rest.put("subject", "Subject");
        rest.put("status", "NEW");
        rest.put("current_revision", "rev123");
        JSONObject revisions = new JSONObject();
        JSONObject revObj = new JSONObject();
        revObj.put("_number", 1);
        revObj.put("ref", "refs/changes/1/1");
        JSONObject commit = new JSONObject();
        commit.put("message", "Subject\n\nLong description.");
        revObj.put("commit", commit);
        revisions.put("rev123", revObj);
        rest.put("revisions", revisions);

        JSONObject q = (JSONObject)org.powermock.reflect.Whitebox.invokeMethod(
                handler, "convertSingleChange", rest);

        assertNotNull(q);
        assertEquals("Subject\n\nLong description.", q.getString("commitMessage"));
    }

    /**
     * Tests conversion with file changes.
     */
    @Test
    public void testConvertWithFiles() throws Exception {
        JSONObject rest = new JSONObject();
        rest.put("id", "proj~master~Ifile123");
        rest.put("change_id", "Ifile123");
        rest.put("project", "proj");
        rest.put("branch", "master");
        rest.put("_number", 5);
        rest.put("subject", "Add files");
        rest.put("status", "NEW");
        rest.put("current_revision", "revfile");
        JSONObject revisions = new JSONObject();
        JSONObject revObj = new JSONObject();
        revObj.put("_number", 1);
        revObj.put("ref", "refs/changes/5/1");
        JSONObject files = new JSONObject();
        JSONObject fileInfo = new JSONObject();
        fileInfo.put("lines_inserted", 10);
        fileInfo.put("lines_deleted", 3);
        files.put("src/main/java/Foo.java", fileInfo);
        revObj.put("files", files);
        revisions.put("revfile", revObj);
        rest.put("revisions", revisions);

        JSONObject q = (JSONObject)org.powermock.reflect.Whitebox.invokeMethod(
                handler, "convertSingleChange", rest);

        assertNotNull(q);
        assertTrue(q.has("currentPatchSet"));
        JSONObject ps = q.getJSONObject("currentPatchSet");
        assertTrue(ps.has("files"));
        JSONArray fileArray = ps.getJSONArray("files");
        assertEquals(1, fileArray.size());
        JSONObject fileObj = fileArray.getJSONObject(0);
        assertEquals("src/main/java/Foo.java", fileObj.getString("file"));
        assertEquals(10, fileObj.getInt("linesInserted"));
        assertEquals(3, fileObj.getInt("linesDeleted"));
    }

    /**
     * Tests conversion of a REST change with abbreviated status.
     */
    @Test
    public void testConvertAbandonedChange() throws Exception {
        JSONObject rest = new JSONObject();
        rest.put("id", "proj~master~Iaban123");
        rest.put("change_id", "Iaban123");
        rest.put("project", "proj");
        rest.put("branch", "master");
        rest.put("_number", 10);
        rest.put("subject", "Abandoned change");
        rest.put("status", "ABANDONED");
        rest.put("current_revision", "abanrev");
        JSONObject revisions = new JSONObject();
        JSONObject revObj = new JSONObject();
        revObj.put("_number", 1);
        revObj.put("ref", "refs/changes/10/1");
        revisions.put("abanrev", revObj);
        rest.put("revisions", revisions);

        JSONObject q = (JSONObject)org.powermock.reflect.Whitebox.invokeMethod(
                handler, "convertSingleChange", rest);

        assertNotNull(q);
        assertEquals("ABANDONED", q.getString("status"));
        assertFalse(q.getBoolean("open"));
    }

    /**
     * Tests conversion with the full Gerrit id format (no change_id).
     */
    @Test
    public void testConvertFallbackId() throws Exception {
        JSONObject rest = new JSONObject();
        // No change_id field — should fall back to id
        rest.put("id", "someProject~main~Iabc123");
        rest.put("project", "someProject");
        rest.put("branch", "main");
        rest.put("_number", 100);
        rest.put("subject", "Test");
        rest.put("status", "NEW");
        rest.put("current_revision", "rev1");
        JSONObject revisions = new JSONObject();
        JSONObject revObj = new JSONObject();
        revObj.put("_number", 1);
        revObj.put("ref", "refs/changes/100/1");
        revisions.put("rev1", revObj);
        rest.put("revisions", revisions);

        JSONObject q = (JSONObject)org.powermock.reflect.Whitebox.invokeMethod(
                handler, "convertSingleChange", rest);

        assertNotNull(q);
        assertEquals("someProject~main~Iabc123", q.getString("id"));
    }

    /**
     * Tests conversion with missing revision — currentPatchSet should be absent.
     */
    @Test
    public void testConvertNoRevision() throws Exception {
        JSONObject rest = new JSONObject();
        rest.put("id", "proj~master~Inorev");
        rest.put("change_id", "Inorev");
        rest.put("project", "proj");
        rest.put("_number", 3);
        rest.put("subject", "No revision");
        rest.put("status", "NEW");
        // No current_revision, no revisions

        JSONObject q = (JSONObject)org.powermock.reflect.Whitebox.invokeMethod(
                handler, "convertSingleChange", rest);

        assertNotNull(q);
        assertEquals("Inorev", q.getString("id"));
        assertFalse(q.has("currentPatchSet"));
    }

    /**
     * Tests that the query methods override the superclass SSH methods.
     */
    @Test
    public void testMethodOverride() throws Exception {
        java.lang.reflect.Method m = GerritRestQueryHandler.class.getMethod(
                "queryJava", String.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class);
        assertEquals(GerritRestQueryHandler.class, m.getDeclaringClass());
    }

    /**
     * Tests that an HTTP 401 causes an IOException in queryJava.
     */
    @Test(expected = java.io.IOException.class)
    public void testBadCredentialsThrowsIOException() throws Exception {
        GerritRestQueryHandler badHandler = new GerritRestQueryHandler(
                "https://httpstat.us/401/",
                new UsernamePasswordCredentials("bad", "creds"),
                "host", 29418, "", null);
        badHandler.queryJava("status:open", true, true, false, false, false);
    }
}
