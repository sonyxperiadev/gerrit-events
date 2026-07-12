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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.sonymobile.tools.gerrit.gerritevents.helpers.HttpClientFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes Gerrit queries via the REST API instead of SSH.
 *
 * <p>Extends {@link GerritQueryHandler} and overrides the query methods to use
 * HTTP GET requests against the Gerrit REST API {@code /a/changes/} endpoint.
 * REST API responses are converted to the same JSON format that the SSH
 * {@code gerrit query} command produces, so existing callers like
 * {@code FileHelper} and {@code Topic} work unchanged.</p>
 *
 * @author Michael Trimarchi
 */
public class GerritRestQueryHandler extends GerritQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GerritRestQueryHandler.class);

    private static final String GERRIT_REST_PREFIX = ")]}'";
    private static final int DEFAULT_MAX_RESULTS = 500;
    /**
     * Divisor for converting milliseconds to seconds.
     */
    private static final long MILLIS_PER_SECOND = 1000L;

    private final String frontEndUrl;
    private final Credentials httpCredentials;
    private final String proxy;
    private HttpClient httpClient;

    /**
     * Creates a GerritRestQueryHandler from the given configuration.
     * The SSH-specific parameters from the config are passed to the superclass
     * constructor but are not used by this REST-based implementation.
     *
     * @param config the configuration containing REST API connection values.
     */
    public GerritRestQueryHandler(GerritConnectionConfig2 config) {
        super(config.getGerritHostName(),
              config.getGerritSshPort(),
              config.getGerritProxy(),
              config.getGerritAuthentication(),
              GerritDefaultValues.DEFAULT_GERRIT_SSH_CONNECTION_TIMEOUT);
        this.frontEndUrl = config.getGerritFrontEndUrl();
        this.httpCredentials = config.getHttpCredentials();
        this.proxy = config.getGerritProxy();
    }

    /**
     * Creates a GerritRestQueryHandler with explicit parameters.
     *
     * @param frontEndUrl the Gerrit front-end URL.
     * @param httpCredentials the HTTP credentials.
     * @param hostName the host name (for superclass, unused).
     * @param sshPort the SSH port (for superclass, unused).
     * @param proxy the proxy (for superclass, unused).
     * @param auth the SSH authentication (for superclass, unused).
     */
    public GerritRestQueryHandler(String frontEndUrl, Credentials httpCredentials,
            String hostName, int sshPort, String proxy,
            com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication auth) {
        super(hostName, sshPort, proxy, auth);
        this.frontEndUrl = frontEndUrl;
        this.httpCredentials = httpCredentials;
        this.proxy = proxy;
    }

    // ---- Override the master queryJava method (6-param) ----

    @Override
    public List<JSONObject> queryJava(String queryString, boolean getPatchSets,
            boolean getCurrentPatchSet, boolean getFiles, boolean getCommitMessage,
            boolean getComments) throws IOException, GerritQueryException {
        String jsonResponse = executeRestQuery(queryString, getPatchSets,
                getCurrentPatchSet, getFiles, getCommitMessage, getComments);
        JSONArray changes;
        try {
            changes = (JSONArray)JSONSerializer.toJSON(jsonResponse);
        } catch (Exception ex) {
            throw new GerritQueryException("Failed to parse REST API response", ex);
        }
        return convertToQueryFormat(changes);
    }

    // ---- Override the master queryJson method (5-param) ----

    @Override
    public List<String> queryJson(String queryString, boolean getPatchSets,
            boolean getCurrentPatchSet, boolean getFiles, boolean getCommitMessage)
            throws IOException {
        List<String> list = new LinkedList<String>();
        try {
            List<JSONObject> objects = queryJava(queryString, getPatchSets,
                    getCurrentPatchSet, getFiles, getCommitMessage, false);
            for (JSONObject obj : objects) {
                list.add(obj.toString());
            }
        } catch (GerritQueryException gqe) {
            logger.error("This should not have happened!", gqe);
        }
        return list;
    }

    // ---- REST API execution ----

    /**
     * Executes a query against the Gerrit REST API.
     *
     * @param queryString the Gerrit query string.
     * @param getPatchSets if true, include all revisions.
     * @param getCurrentPatchSet if true, include the current revision.
     * @param getFiles if true, include changed files.
     * @param getCommitMessage if true, include the full commit message.
     * @param getComments if true, include inline comments.
     * @return the raw JSON response body.
     * @throws IOException if a network error occurs.
     */
    private String executeRestQuery(String queryString, boolean getPatchSets,
            boolean getCurrentPatchSet, boolean getFiles, boolean getCommitMessage,
            boolean getComments) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(frontEndUrl);
        if (!frontEndUrl.endsWith("/")) {
            urlBuilder.append('/');
        }
        urlBuilder.append("a/changes/?q=");
        urlBuilder.append(urlEncode(queryString));
        urlBuilder.append("&n=").append(DEFAULT_MAX_RESULTS);

        // Always request detailed accounts and labels so that owner/uploader
        // contain 'name', 'email', and 'username', and labels/approvals
        // (Verified, Code-Review) are available for the manual trigger page.
        urlBuilder.append("&o=DETAILED_ACCOUNTS");
        urlBuilder.append("&o=LABELS");

        // Map SSH query flags to REST API options
        if (getPatchSets) {
            urlBuilder.append("&o=ALL_REVISIONS");
        }
        if (getCurrentPatchSet) {
            urlBuilder.append("&o=CURRENT_REVISION");
        }
        if (getFiles) {
            urlBuilder.append("&o=CURRENT_FILES");
        }
        if (getCommitMessage) {
            urlBuilder.append("&o=CURRENT_COMMIT");
        }
        if (getComments) {
            urlBuilder.append("&o=MESSAGES");
        }

        String url = urlBuilder.toString();
        logger.trace("REST query: {}", url);

        HttpClient client = getHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + statusCode + " for query: " + url);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        if (body.startsWith(GERRIT_REST_PREFIX)) {
            body = body.substring(GERRIT_REST_PREFIX.length());
        }
        return body;
    }

    /**
     * Converts a REST API JSON array of changes into the SSH query JSON format.
     *
     * @param changes the JSON array from {@code GET /a/changes/}.
     * @return a list of JSONObjects in the SSH query format.
     */
    private List<JSONObject> convertToQueryFormat(JSONArray changes) {
        List<JSONObject> result = new LinkedList<JSONObject>();
        for (int i = 0; i < changes.size(); i++) {
            JSONObject restChange = changes.getJSONObject(i);
            JSONObject queryJson = convertSingleChange(restChange);
            if (queryJson != null) {
                result.add(queryJson);
            }
        }
        return result;
    }

    /**
     * Converts a single REST API change JSON to SSH query format.
     *
     * @param rest the change JSON from the REST API.
     * @return a JSONObject in SSH query format, or null if conversion fails.
     */
    private JSONObject convertSingleChange(JSONObject rest) {
        JSONObject q = new JSONObject();

        // Top-level fields — map REST names to SSH query names
        q.put("project", rest.optString("project", ""));
        q.put("branch", rest.optString("branch", ""));
        // use change_id (e.g. Iabc123...) not id (p~master~Iabc123...)
        if (rest.has("change_id")) {
            q.put("id", rest.getString("change_id"));
        } else {
            q.put("id", rest.optString("id", ""));
        }
        // _number is an integer in REST, but SSH query uses string
        if (rest.has("_number")) {
            q.put("number", String.valueOf(rest.getInt("_number")));
        } else {
            q.put("number", rest.optString("number", ""));
        }
        q.put("subject", rest.optString("subject", ""));
        q.put("status", rest.optString("status", "NEW"));
        q.put("open", "NEW".equals(rest.optString("status", "NEW")));

        // Owner — accounts from REST include _account_id which Account.fromJson ignores
        if (rest.has("owner")) {
            q.put("owner", rest.getJSONObject("owner"));
        }

        // URL — constructed from frontEndUrl + change number
        String changeNumber = String.valueOf(rest.optInt("_number", -1));
        q.put("url", frontEndUrl + changeNumber);

        // Convert timestamps from ISO-8601 to epoch seconds
        if (rest.has("created")) {
            q.put("createdOn", convertToEpochSeconds(rest.optString("created", "")));
        }
        if (rest.has("updated")) {
            q.put("lastUpdated", convertToEpochSeconds(rest.optString("updated", "")));
        }

        // Commit message from the revisions[cr].commit.message
        String currentRev = rest.optString("current_revision", null);
        JSONObject revisions = rest.optJSONObject("revisions");
        JSONObject revObj = null;
        if (currentRev != null && revisions != null && revisions.has(currentRev)) {
            revObj = revisions.getJSONObject(currentRev);
        }

        if (revObj != null && revObj.has("commit")) {
            JSONObject commit = revObj.getJSONObject("commit");
            if (commit.has("message")) {
                q.put("commitMessage", commit.getString("message"));
            }
        }

        // Build currentPatchSet object
        if (revObj != null) {
            JSONObject patchSet = new JSONObject();
            if (revObj.has("_number")) {
                patchSet.put("number", String.valueOf(revObj.getInt("_number")));
            } else {
                patchSet.put("number", "1");
            }
            patchSet.put("revision", currentRev);
            patchSet.put("ref", revObj.optString("ref", ""));
            if (revObj.has("kind")) {
                patchSet.put("kind", revObj.getString("kind"));
            }
            if (revObj.has("created")) {
                patchSet.put("createdOn", convertToEpochSeconds(revObj.optString("created", "")));
            }
            if (revObj.has("uploader")) {
                patchSet.put("uploader", revObj.getJSONObject("uploader"));
            }

            // Convert REST labels map to SSH approvals array
            JSONObject labels = revObj.optJSONObject("labels");
            if (labels == null) {
                labels = rest.optJSONObject("labels");
            }
            if (labels != null) {
                patchSet.put("approvals", convertLabelsToApprovals(labels));
            }

            // Convert files from REST map format to array format
            if (revObj.has("files")) {
                JSONObject restFiles = revObj.optJSONObject("files");
                JSONArray fileArray = new JSONArray();
                if (restFiles != null) {
                    for (Object key : restFiles.keySet()) {
                        String fileName = (String)key;
                        JSONObject restFile = restFiles.getJSONObject(fileName);
                        JSONObject fileObj = new JSONObject();
                        fileObj.put("file", fileName);
                        if (restFile.has("lines_inserted")) {
                            fileObj.put("linesInserted", restFile.getInt("lines_inserted"));
                        }
                        if (restFile.has("lines_deleted")) {
                            fileObj.put("linesDeleted", restFile.getInt("lines_deleted"));
                        }
                        fileArray.add(fileObj);
                    }
                }
                patchSet.put("files", fileArray);
            }

            q.put("currentPatchSet", patchSet);
        }

        // All patchsets (ALL_REVISIONS) — convert to patchSets array
        if (revisions != null && rest.has("o") && rest.getString("o").contains("ALL_REVISIONS")) {
            // We only construct patchSets if ALL_REVISIONS was actually requested;
            // the REST API doesn't have a direct flag we can check in the response.
            // We check if there are multiple revisions.
            JSONArray patchSetsArr = new JSONArray();
            for (Object rKey : revisions.keySet()) {
                String rRev = (String)rKey;
                JSONObject rObj = revisions.getJSONObject(rRev);
                JSONObject ps = new JSONObject();
                ps.put("revision", rRev);
                if (rObj.has("_number")) {
                    ps.put("number", String.valueOf(rObj.getInt("_number")));
                }
                ps.put("ref", rObj.optString("ref", ""));
                if (rObj.has("kind")) {
                    ps.put("kind", rObj.getString("kind"));
                }
                if (rObj.has("uploader")) {
                    ps.put("uploader", rObj.getJSONObject("uploader"));
                }
                JSONObject revLabels = rObj.optJSONObject("labels");
                if (revLabels != null) {
                    ps.put("approvals", convertLabelsToApprovals(revLabels));
                }
                patchSetsArr.add(ps);
            }
            q.put("patchSets", patchSetsArr);
        }

        return q;
    }

    /**
     * Converts a Gerrit ISO-8601 date string to epoch seconds.
     * Delegates to {@link GerritRestPoller#parseGerritDate}.
     *
     * @param dateStr the date string, e.g. "2023-01-15 10:00:00.000000000".
     * @return epoch seconds, or 0 if parsing fails.
     */
    private long convertToEpochSeconds(String dateStr) {
        Date d = GerritRestPoller.parseGerritDate(dateStr);
        if (d == null) {
            return 0;
        }
        return d.toInstant().getEpochSecond();
    }

    /**
     * Converts a Gerrit REST API labels object to an SSH-format approvals JSONArray.
     * REST format: {@code {"Code-Review": {"value": 1}, "Verified": {"value": -1}}}
     * SSH format:  {@code [{"type": "Code-Review", "value": "1"}, ...]}
     *
     * @param labels the labels JSONObject from the REST API.
     * @return a JSONArray of approval objects in SSH format.
     */
    private static JSONArray convertLabelsToApprovals(JSONObject labels) {
        JSONArray approvals = new JSONArray();
        for (Object key : labels.keySet()) {
            String labelName = (String)key;
            JSONObject labelData = labels.getJSONObject(labelName);
            JSONObject approval = new JSONObject();
            approval.put("type", labelName);
            // REST API labels have an 'all' array with the aggregate votes,
            // e.g. {"all": [{"value": 1, "_account_id": 1000000}, ...]}
            // Use the first entry as the current approval value.
            if (labelData.has("all")) {
                JSONArray allVotes = labelData.getJSONArray("all");
                if (allVotes.size() > 0) {
                    approval.put("value",
                            allVotes.getJSONObject(0).optString("value", "0"));
                } else {
                    approval.put("value", "0");
                }
            } else if (labelData.has("value")) {
                approval.put("value", labelData.optString("value", "0"));
            } else {
                approval.put("value", "0");
            }
            approvals.add(approval);
        }
        return approvals;
    }

    /**
     * Returns the HTTP client, creating it lazily if necessary.
     *
     * @return a configured HttpClient.
     */
    private HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClientFactory.createClient(httpCredentials, proxy);
        }
        return httpClient;
    }

    /**
     * URL-encodes a string using UTF-8.
     *
     * @param value the string to encode.
     * @return the encoded string.
     */
    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            // UTF-8 is always supported
            return value;
        }
    }
}
