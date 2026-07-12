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
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sonymobile.tools.gerrit.gerritevents.helpers.HttpClientFactory;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritChangeKind;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritChangeStatus;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.PatchSet;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeAbandoned;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeMerged;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.GerritTriggeredEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.PatchsetCreated;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.TopicChanged;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.PrivateStateChanged;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.WipStateChanged;

/**
 * Polls the Gerrit REST API over HTTPS to receive change events.
 *
 * <p>This is an alternative to {@link GerritConnection}'s SSH-based
 * {@code stream-events} command. Instead of maintaining a persistent SSH
 * connection, this class periodically polls the Gerrit REST API to detect
 * new and updated changes, then posts corresponding events to the
 * {@link GerritHandler}.</p>
 *
 * <p>Detection works by tracking the last-seen revision and status for each
 * change. When a new change appears or the current revision changes, a
 * {@link PatchsetCreated} event is emitted. When a change's status changes
 * (e.g. from NEW to MERGED), the appropriate lifecycle event is emitted.</p>
 *
 * @author Michael Trimarchi
 */
public class GerritRestPoller extends Thread implements GerritEventSource, Connector {

    private static final Logger logger = LoggerFactory.getLogger(GerritRestPoller.class);

    /**
     * The scheme name used for HTTPS polling.
     */
    public static final String GERRIT_PROTOCOL_SCHEME_NAME = "https";

    /**
     * Gerrit REST API responses are prefixed with this to prevent JSON hijacking.
     */
    private static final String GERRIT_REST_PREFIX = ")]}'";

    /**
     * Time to wait between connection/poll attempts when in error state.
     */
    private static final int ERROR_SLEEP_MILLIS = 5000;

    /**
     * Time to wait after a successful poll with no changes found.
     * This is in addition to the configured poll interval.
     */
    private static final int IDLE_SLEEP_MILLIS = 500;

    /**
     * Multiplier for converting seconds to milliseconds.
     */
    private static final long MILLIS_PER_SECOND = 1000L;

    private final String gerritName;
    private final GerritConnectionConfig2 config;
    private final String frontEndUrl;
    private final int pollIntervalSeconds;
    private final int maxChangesPerPoll;
    private GerritHandler handler;
    private volatile boolean shutdownInProgress;
    private volatile boolean connected;
    private String gerritVersion;
    private final Set<ConnectionListener> listeners = new CopyOnWriteArraySet<ConnectionListener>();
    private HttpClient httpClient;

    /**
     * True after the first successful poll has seeded the baseline state.
     * When false, {@link #processChange} silently populates
     * {@link #knownChanges} without emitting events, preventing all open
     * changes from re-firing PatchsetCreated after a restart.
     */
    private volatile boolean firstPollDone;

    /**
     * Tracks the last known state of each change.
     * Key: changeId, Value: {revision, status}
     */
    private final Map<String, ChangeState> knownChanges = new ConcurrentHashMap<String, ChangeState>();

    /**
     * Stores the last known state for a change.
     */
    private static class ChangeState {
        /** The last known revision. */
        final String revision;
        /** The last known status. */
        final GerritChangeStatus status;
        /** The last known topic, or null. */
        final String topic;
        /** Whether the change is work-in-progress. */
        final boolean wip;
        /** Whether the change is private. */
        final boolean isPrivate;

        /**
         * Creates a new ChangeState.
         * @param revision the revision.
         * @param status the status.
         * @param topic the topic, or null.
         * @param wip whether the change is work-in-progress.
         * @param isPrivate whether the change is private.
         */
        ChangeState(String revision, GerritChangeStatus status, String topic, boolean wip, boolean isPrivate) {
            this.revision = revision;
            this.status = status;
            this.topic = topic;
            this.wip = wip;
            this.isPrivate = isPrivate;
        }
    }

    /**
     * Creates a GerritRestPoller.
     *
     * @param gerritName the name of the Gerrit server.
     * @param config the configuration containing connection and REST values.
     */
    public GerritRestPoller(String gerritName, GerritConnectionConfig2 config) {
        this.gerritName = gerritName;
        this.config = config;
        this.frontEndUrl = config.getGerritFrontEndUrl();
        this.pollIntervalSeconds = config.getHttpsPollInterval();
        this.maxChangesPerPoll = config.getHttpsPollMaxChanges();
        logger.info("{}: GerritRestPoller created with poll interval {}s, max changes {}",
                gerritName, pollIntervalSeconds, maxChangesPerPoll);
    }

    // ---- GerritEventSource implementation ----

    @Override
    public void setHandler(GerritHandler handler) {
        this.handler = handler;
    }

    @Override
    public GerritHandler getHandler() {
        return handler;
    }

    @Override
    public void addListener(ConnectionListener listener) {
        if (!listeners.add(listener)) {
            logger.warn("{}: Connection listener doubly-added: {}", gerritName, listener);
        }
    }

    @Override
    public void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getGerritVersion() {
        return gerritVersion;
    }

    @Override
    public void shutdown(boolean join) {
        shutdownInProgress = true;
        if (join) {
            try {
                this.join();
            } catch (InterruptedException ex) {
                logger.warn("{}: Interrupted while waiting for shutdown.", gerritName, ex);
            }
        }
    }

    // ---- Connector implementation ----

    @Override
    public void reconnect() {
        // On the next poll, re-seed the baseline silently so that
        // existing open changes aren't replayed as PatchsetCreated.
        firstPollDone = false;
        logger.debug("{}: Reconnect requested; will re-seed baseline on next poll.", gerritName);
    }

    // ---- Thread main loop ----

    @Override
    public void run() {
        logger.info("{}: Starting GerritRestPoller.", gerritName);

        // Perform an initial connection check immediately so that
        // server health reporting (doWakeup) detects connectivity
        // without waiting for the first full poll cycle.
        performInitialConnection();

        while (!shutdownInProgress) {
            try {
                // Fetch and detect Gerrit version on first successful request
                if (gerritVersion == null) {
                    gerritVersion = fetchGerritVersion();
                    if (gerritVersion != null) {
                        logger.info("{}: Detected Gerrit version: {}", gerritName, gerritVersion);
                    }
                }

                pollChanges();
                notifyConnectionEstablished();
            } catch (IOException ex) {
                logger.error("{}: Error during HTTPS poll: {}", gerritName, ex.getMessage());
                notifyConnectionDown();
                sleepMillis(ERROR_SLEEP_MILLIS);
            } catch (Exception ex) {
                logger.error("{}: Unexpected error during poll.", gerritName, ex);
                notifyConnectionDown();
                sleepMillis(ERROR_SLEEP_MILLIS);
            }

            if (!shutdownInProgress) {
                sleepMillis(pollIntervalSeconds * MILLIS_PER_SECOND);
            }
        }
        if (connected) {
            notifyConnectionDown();
        }
        handler = null;
        httpClient = null;
        logger.debug("{}: End of GerritRestPoller Thread.", gerritName);
    }

    /**
     * Performs an initial connectivity check to the Gerrit server.
     * Fetches the server version via a lightweight REST API call.
     * If successful, notifies connection listeners immediately so that
     * {@code doWakeup()} detects the server as reachable without waiting
     * for the first full polling cycle to complete.
     */
    private void performInitialConnection() {
        try {
            if (httpClient == null) {
                httpClient = createHttpClient();
            }
            if (gerritVersion == null) {
                gerritVersion = fetchGerritVersion();
                if (gerritVersion != null) {
                    logger.info("{}: Detected Gerrit version: {}", gerritName, gerritVersion);
                }
            }
            // Connectivity check succeeded — notify immediately
            notifyConnectionEstablished();
        } catch (Exception ex) {
            logger.warn("{}: Initial connection check failed, will retry in polling loop: {}",
                    gerritName, ex.getMessage());
        }
    }

    // ---- HTTP helpers ----

    /**
     * Creates an HttpClient configured with the credentials from the config.
     *
     * @return a configured HttpClient.
     */
    private HttpClient createHttpClient() {
        return HttpClientFactory.createClient(
                config.getHttpCredentials(), config.getGerritProxy());
    }

    /**
     * Executes an HTTP GET request to the Gerrit REST API and returns the
     * response body as a string.
     *
     * @param path the REST API path relative to the front-end URL (e.g. "a/changes/").
     * @return the response body.
     * @throws IOException if the request fails.
     */
    private String httpGet(String path) throws IOException {
        String url = frontEndUrl + path;
        logger.trace("{}: GET {}", gerritName, url);
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + statusCode + " for " + url);
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        // Strip Gerrit's JSON hijacking prevention prefix
        if (body.startsWith(GERRIT_REST_PREFIX)) {
            body = body.substring(GERRIT_REST_PREFIX.length());
        }
        return body;
    }

    // ---- Gerrit API methods ----

    /**
     * Fetches the Gerrit server version via the REST API.
     *
     * @return the version string, or null if it could not be determined.
     */
    private String fetchGerritVersion() {
        try {
            String body = httpGet("a/config/server/version");
            return body.replaceAll("^\"|\"$", "").trim();
        } catch (IOException ex) {
            logger.warn("{}: Could not fetch Gerrit version: {}", gerritName, ex.getMessage());
            return null;
        }
    }

    /**
     * Polls the Gerrit REST API for open changes and processes any new or
     * updated changes.
     *
     * @throws IOException if a network error occurs.
     */
    private void pollChanges() throws IOException {
        String queryPath = "a/changes/?q=is:open&n=" + maxChangesPerPoll
                + "&o=CURRENT_REVISION&o=DETAILED_ACCOUNTS&o=CURRENT_COMMIT";
        String body = httpGet(queryPath);
        JSONArray changes;
        try {
            changes = (JSONArray)JSONSerializer.toJSON(body);
        } catch (Exception ex) {
            logger.error("{}: Failed to parse REST API response.", gerritName, ex);
            return;
        }

        if (changes == null || changes.isEmpty()) {
            logger.trace("{}: No open changes found.", gerritName);
            sleepMillis(IDLE_SLEEP_MILLIS);
            return;
        }

        logger.debug("{}: Processing {} open changes.", gerritName, changes.size());
        Provider provider = createProvider();
        Set<String> seenChangeIds = new HashSet<String>();

        for (int i = 0; i < changes.size(); i++) {
            if (shutdownInProgress || interrupted()) {
                return;
            }
            try {
                JSONObject changeJson = changes.getJSONObject(i);
                // Use hex Change-Id (change_id) as key, not the REST "id" triple.
                // This must match the key used in processChange() / knownChanges.
                String changeId;
                if (changeJson.has("change_id")) {
                    changeId = changeJson.getString("change_id");
                } else {
                    changeId = changeJson.getString("id");
                }
                seenChangeIds.add(changeId);
                processChange(changeJson, provider);
            } catch (Exception ex) {
                logger.warn("{}: Error processing change at index {}: {}",
                        gerritName, i, ex.getMessage());
            }
        }

        // Prune stale entries: remove changes that are no longer open
        // (MERGED, ABANDONED, or UNKNOWN — e.g. DELETED — and not present
        // in the current poll response). This prevents unbounded memory
        // growth from accumulating closed changes.
        pruneStaleEntries(seenChangeIds);

        firstPollDone = true;
    }

    /**
     * Removes entries from {@link #knownChanges} whose status is MERGED,
     * ABANDONED, or UNKNOWN (e.g. DELETED) and that are not present in
     * the given set of seen change IDs. NEW entries are never pruned,
     * even if absent from the current poll (pagination safety).
     *
     * <p>Package visibility for testing.</p>
     *
     * @param seenChangeIds the set of change IDs present in the current poll.
     */
    void pruneStaleEntries(Set<String> seenChangeIds) {
        Iterator<Map.Entry<String, ChangeState>> it = knownChanges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ChangeState> entry = it.next();
            if (seenChangeIds.contains(entry.getKey())) {
                continue;
            }
            GerritChangeStatus status = entry.getValue().status;
            if (status == GerritChangeStatus.MERGED
                    || status == GerritChangeStatus.ABANDONED
                    || status == GerritChangeStatus.UNKNOWN) {
                it.remove();
                logger.trace("{}: Pruned stale entry for change {}", gerritName, entry.getKey());
            }
        }
    }

    /**
     * Processes a single change from the REST API response and emits
     * appropriate events if the change is new or has been updated.
     *
     * @param changeJson the JSON object representing the change.
     * @param provider the Provider to attach to events.
     */
    private void processChange(JSONObject changeJson, Provider provider) {
        // Build the Change DTO once — reused by all event-detection branches
        Change change = buildChange(changeJson);
        String changeId = change.getId();

        // Determine current revision
        String currentRevision = null;
        JSONObject currentRevObj = null;
        if (changeJson.has("current_revision")) {
            currentRevision = changeJson.getString("current_revision");
        }
        if (currentRevision != null && changeJson.has("revisions")) {
            JSONObject revisions = changeJson.getJSONObject("revisions");
            if (revisions.has(currentRevision)) {
                currentRevObj = revisions.getJSONObject(currentRevision);
            }
        }

        if (currentRevision == null) {
            logger.debug("{}: Change {} has no current_revision, skipping.", gerritName, changeId);
            return;
        }

        // Extract abandoner (only available from raw JSON, not on Change DTO)
        Account abandoner = null;
        if (changeJson.has("abandoner")) {
            try {
                abandoner = new Account(changeJson.getJSONObject("abandoner"));
            } catch (Exception ex) {
                logger.trace("{}: Could not parse abandoner for change {}: {}",
                        gerritName, changeId, ex.getMessage());
            }
        }

        ChangeState previous = knownChanges.get(changeId);

        // On the first poll after startup, silently seed the baseline state.
        // This prevents all open changes from re-firing PatchsetCreated after
        // a restart while still populating knownChanges for delta detection.
        if (!firstPollDone) {
            knownChanges.put(changeId, new ChangeState(currentRevision, change.getStatus(),
                    change.getTopic(), change.isWip(), change.isPrivate()));
            return;
        }

        // Check if this is a new change or the revision has changed
        boolean isNew = (previous == null);
        boolean revisionChanged = (previous != null && !currentRevision.equals(previous.revision));

        if (isNew || revisionChanged) {
            PatchSet patchSet = buildPatchSet(currentRevObj, currentRevision);

            PatchsetCreated event = new PatchsetCreated();
            event.setChange(change);
            event.setPatchset(patchSet);
            event.setProvider(provider);
            event.setReceivedOn(System.currentTimeMillis());
            event.setAccount(change.getOwner());

            if (handler != null) {
                handler.post(event);
                //CS IGNORE AvoidInlineConditionals FOR NEXT 1 LINES. REASON: Readable ternary.
                String changeType = isNew ? "(new)" : "(updated)";
                logger.debug("{}: Posted PatchsetCreated for change {}/{} rev {} {}",
                        gerritName, change.getProject(), change.getNumber(),
                        currentRevision, changeType);
            }
        }

        // Check for status transitions on known changes
        if (previous != null && previous.status != change.getStatus()) {
            GerritTriggeredEvent statusEvent = null;
            switch (change.getStatus()) {
                case MERGED:
                    statusEvent = buildStatusEvent(change, currentRevision, currentRevObj,
                            provider, abandoner, ChangeMerged.class);
                    break;
                case ABANDONED:
                    statusEvent = buildStatusEvent(change, currentRevision, currentRevObj,
                            provider, abandoner, ChangeAbandoned.class);
                    break;
                default:
                    // RESTORED status is not directly distinguishable from NEW
                    // in all Gerrit versions; handle explicitly if needed
                    break;
            }
            if (statusEvent != null && handler != null) {
                handler.post(statusEvent);
                logger.debug("{}: Posted status transition for change {}/{}: {} -> {}",
                        gerritName, change.getProject(), change.getNumber(),
                        previous.status, change.getStatus());
            }
        }

        // Check for topic/WIP/private changes on known changes
        if (previous != null && !isNew && !revisionChanged) {
            detectTopicChange(change, previous, provider, currentRevObj, currentRevision);
            detectWipStateChange(change, previous, provider, currentRevObj, currentRevision);
            detectPrivateStateChange(change, previous, provider, currentRevObj, currentRevision);
        }

        // Update known state from the Change DTO
        knownChanges.put(changeId, new ChangeState(currentRevision, change.getStatus(),
                change.getTopic(), change.isWip(), change.isPrivate()));
    }

    /**
     * Detects topic changes for a known change and emits a
     * {@link TopicChanged} event when the topic changes.
     *
     * @param change the pre-built Change DTO.
     * @param previous the previous known state.
     * @param provider the Provider to attach.
     * @param currentRevObj the current revision JSON.
     * @param currentRevision the current revision SHA.
     */
    private void detectTopicChange(Change change, ChangeState previous,
            Provider provider, JSONObject currentRevObj, String currentRevision) {

        String currentTopic = change.getTopic();
        String previousTopic = previous.topic;

        //CS IGNORE AvoidInlineConditionals FOR NEXT 2 LINES. REASON: Readable null-safe.
        boolean topicChanged = (previousTopic == null)
                ? (currentTopic != null) : !previousTopic.equals(currentTopic);
        if (topicChanged) {
            PatchSet patchSet = buildPatchSet(currentRevObj, currentRevision);

            TopicChanged topicEvent = new TopicChanged();
            topicEvent.setChange(change);
            topicEvent.setPatchset(patchSet);
            topicEvent.setProvider(provider);
            topicEvent.setReceivedOn(System.currentTimeMillis());
            if (previousTopic != null) {
                topicEvent.setOldTopic(previousTopic);
            }
            Account owner = change.getOwner();
            if (owner != null) {
                topicEvent.setChanger(owner);
                topicEvent.setAccount(owner);
            }
            if (handler != null) {
                handler.post(topicEvent);
                logger.debug("{}: Posted TopicChanged for change {}/{}: {} -> {}",
                        gerritName, change.getProject(), change.getNumber(),
                        previousTopic, currentTopic);
            }
        }
    }

    /**
     * Detects WIP (Work In Progress) state changes for a known change and emits
     * a {@link WipStateChanged} event when the WIP state toggles.
     *
     * @param change the pre-built Change DTO.
     * @param previous the previous known state.
     * @param provider the Provider to attach.
     * @param currentRevObj the current revision JSON.
     * @param currentRevision the current revision SHA.
     */
    private void detectWipStateChange(Change change, ChangeState previous,
            Provider provider, JSONObject currentRevObj, String currentRevision) {

        boolean currentWip = change.isWip();
        if (previous.wip == currentWip) {
            return;
        }

        PatchSet patchSet = buildPatchSet(currentRevObj, currentRevision);

        WipStateChanged event = new WipStateChanged();
        event.setChange(change);
        event.setPatchset(patchSet);
        event.setProvider(provider);
        event.setReceivedOn(System.currentTimeMillis());
        event.setAccount(change.getOwner());
        if (handler != null) {
            handler.post(event);
            logger.debug("{}: Posted WipStateChanged for change {}/{}: {} -> {}",
                    gerritName, change.getProject(), change.getNumber(),
                    previous.wip, currentWip);
        }
    }

    /**
     * Detects private state changes for a known change and emits
     * a {@link PrivateStateChanged} event when the private state toggles.
     *
     * @param change the pre-built Change DTO.
     * @param previous the previous known state.
     * @param provider the Provider to attach.
     * @param currentRevObj the current revision JSON.
     * @param currentRevision the current revision SHA.
     */
    private void detectPrivateStateChange(Change change, ChangeState previous,
            Provider provider, JSONObject currentRevObj, String currentRevision) {

        boolean currentPrivate = change.isPrivate();
        if (previous.isPrivate == currentPrivate) {
            return;
        }

        PatchSet patchSet = buildPatchSet(currentRevObj, currentRevision);

        PrivateStateChanged event = new PrivateStateChanged();
        event.setChange(change);
        event.setPatchset(patchSet);
        event.setProvider(provider);
        event.setReceivedOn(System.currentTimeMillis());
        event.setAccount(change.getOwner());
        if (handler != null) {
            handler.post(event);
            logger.debug("{}: Posted PrivateStateChanged for change {}/{}: {} -> {}",
                    gerritName, change.getProject(), change.getNumber(),
                    previous.isPrivate, currentPrivate);
        }
    }

    /**
     * Builds a Change DTO from the REST API JSON.
     * Uses {@link Change#fromJson} for standard fields and applies
     * REST-API-specific fixups for fields whose key names differ from
     * the event-stream format ({@code _number}, {@code work_in_progress},
     * {@code is_private}, and the missing {@code url}).
     *
     * @param changeJson the JSON object for this change.
     * @return a new Change DTO.
     */
    private Change buildChange(JSONObject changeJson) {
        Change change = new Change();
        change.fromJson(changeJson);

        // Fixup: REST API uses "_number" (int), not "number" (string)
        String changeNumber = String.valueOf(changeJson.optInt("_number", -1));
        change.setNumber(changeNumber);

        // Fixup: REST API "id" is the full "project~changeNumber" triple.
        // Use the hex "change_id" instead, so ChangeId.asUrlPart() produces
        // the valid project~branch~ChangeIdHex triple for Gerrit URLs.
        if (changeJson.has("change_id")) {
            change.setId(changeJson.getString("change_id"));
        }

        // Fixup: REST API uses "work_in_progress", not "wip"
        change.setWip(changeJson.optBoolean("work_in_progress", false));

        // Fixup: REST API uses "is_private", not "private"
        change.setPrivate(changeJson.optBoolean("is_private", false));

        // Fixup: REST API has no "url" field — construct it
        change.setUrl(frontEndUrl + changeNumber);

        return change;
    }

    /**
     * Builds a PatchSet DTO from the revision JSON.
     * @param revisionObj the JSON object for this revision, or null.
     * @param revision the revision SHA.
     * @return a new PatchSet DTO.
     */
    private PatchSet buildPatchSet(JSONObject revisionObj, String revision) {
        PatchSet patchSet = new PatchSet();
        patchSet.setRevision(revision);
        if (revisionObj != null) {
            String patchNumber = String.valueOf(revisionObj.optInt("_number", 1));
            patchSet.setNumber(patchNumber);
            String ref = revisionObj.optString("ref", "");
            patchSet.setRef(ref);
            if (revisionObj.has("uploader")) {
                try {
                    patchSet.setUploader(new Account(revisionObj.getJSONObject("uploader")));
                } catch (Exception ex) {
                    logger.trace("Could not set uploader on patchset: {}", ex.getMessage());
                }
            }
            if (revisionObj.has("kind")) {
                patchSet.setKind(GerritChangeKind.fromString(revisionObj.getString("kind")));
            }
            if (revisionObj.has("created")) {
                String createdStr = revisionObj.getString("created");
                patchSet.setCreatedOn(parseGerritDate(createdStr));
            }
        } else {
            patchSet.setNumber("1");
        }
        return patchSet;
    }

    /**
     * Builds a status transition event (ChangeMerged, ChangeAbandoned, etc.).
     * @param change the pre-built Change DTO.
     * @param revision the current revision SHA.
     * @param revisionObj the JSON object for the revision.
     * @param provider the Provider to attach.
     * @param abandoner the abandoner Account, or null.
     * @param eventClass the event class to build (ChangeMerged.class or ChangeAbandoned.class).
     * @return a new GerritTriggeredEvent, or null on failure.
     */
    private GerritTriggeredEvent buildStatusEvent(Change change, String revision,
            JSONObject revisionObj, Provider provider, Account abandoner, Class<?> eventClass) {
        try {
            PatchSet patchSet = buildPatchSet(revisionObj, revision);

            if (eventClass == ChangeMerged.class) {
                ChangeMerged event = new ChangeMerged();
                event.setChange(change);
                event.setPatchset(patchSet);
                event.setProvider(provider);
                event.setReceivedOn(System.currentTimeMillis());
                event.setAccount(change.getOwner());
                return event;
            } else if (eventClass == ChangeAbandoned.class) {
                ChangeAbandoned event = new ChangeAbandoned();
                event.setChange(change);
                event.setPatchset(patchSet);
                event.setProvider(provider);
                event.setReceivedOn(System.currentTimeMillis());
                event.setAccount(change.getOwner());
                if (abandoner != null) {
                    event.setAbandoner(abandoner);
                }
                return event;
            }
        } catch (Exception ex) {
            logger.warn("{}: Failed to build status event: {}", gerritName, ex.getMessage());
        }
        return null;
    }

    /**
     * Creates a Provider for the current connection.
     *
     * <p>When the configured hostname is empty (e.g. SSH is not in use), the host
     * is derived from {@code frontEndUrl} — which is the HTTPS endpoint configured
     * for this connection.</p>
     * @return a new Provider.
     */
    private Provider createProvider() {
        //CS IGNORE AvoidInlineConditionals FOR NEXT 1 LINES. REASON: Readable null check.
        String version = gerritVersion != null ? gerritVersion : "";
        String host = config.getGerritHostName();
        if (host == null || host.isEmpty()) {
            host = frontEndUrl;
        }
        return new Provider(
                gerritName,
                host,
                String.valueOf(config.getGerritSshPort()),
                GERRIT_PROTOCOL_SCHEME_NAME,
                frontEndUrl,
                version);
    }

    // ---- Date parsing ----

    /**
     * Parses a Gerrit REST API date string.
     * Gerrit uses formats like "2023-01-15 10:00:00.000000000" or
     * "2023-01-15 10:00:00".
     *
     * @param dateStr the date string.
     * @return a Date, or null if parsing fails.
     */
    static Date parseGerritDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            // Strip nanoseconds if present (Gerrit uses 9 digits after dot)
            String normalized = dateStr.replace(" ", "T");
            int dotIndex = normalized.indexOf('.');
            if (dotIndex >= 0) {
                normalized = normalized.substring(0, dotIndex);
            }
            return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(normalized);
        } catch (Exception ex) {
            logger.trace("Could not parse date: {}", dateStr, ex);
            return null;
        }
    }

    // ---- Notification helpers ----

    /**
     * Marks the connection as established and notifies listeners.
     */
    private void notifyConnectionEstablished() {
        if (!connected) {
            connected = true;
            notifyListeners(GerritConnectionEvent.GERRIT_CONNECTION_ESTABLISHED);
        }
    }

    /**
     * Marks the connection as down and notifies listeners.
     */
    private void notifyConnectionDown() {
        if (connected) {
            connected = false;
            notifyListeners(GerritConnectionEvent.GERRIT_CONNECTION_DOWN);
        }
    }

    /**
     * Notifies all registered connection listeners of an event.
     * @param event the connection event.
     */
    private void notifyListeners(GerritConnectionEvent event) {
        for (ConnectionListener listener : listeners) {
            try {
                switch (event) {
                    case GERRIT_CONNECTION_ESTABLISHED:
                        listener.connectionEstablished();
                        break;
                    case GERRIT_CONNECTION_DOWN:
                        listener.connectionDown();
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                logger.error("{}: ConnectionListener threw Exception.", gerritName, ex);
            }
        }
    }

    /**
     * Sleeps for the specified number of milliseconds, handling interrupts.
     * @param millis the number of milliseconds to sleep.
     */
    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            logger.trace("{}: Sleep interrupted.", gerritName);
        }
    }

    /**
     * Returns an unmodifiable view of the set of {@link ConnectionListener}s.
     *
     * @return the set of connection listeners.
     */
    public Set<ConnectionListener> getListenersView() {
        return Collections.unmodifiableSet(listeners);
    }
}
