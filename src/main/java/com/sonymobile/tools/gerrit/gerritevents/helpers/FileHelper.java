package com.sonymobile.tools.gerrit.gerritevents.helpers;

import com.sonymobile.tools.gerrit.gerritevents.GerritQueryException;
import com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandler;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Class that holds static function that provides list of files for the change.
 */
public final class FileHelper {
    private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);

    /**
     * Utility class should not have constructor.
     */
    private FileHelper() {
    }

    /**
     * Provides list of files related to the change.
     * @param gerritQueryHandler the query handler, responsible for the queries to gerrit.
     * @param changeId the Gerrit change id.
     * @return list of files from the change, null in case of errors
     */
    public static List<String> getFilesByChange(GerritQueryHandler gerritQueryHandler, String changeId) {
        try {
            List<JSONObject> jsonList = gerritQueryHandler.queryFiles("change:" + changeId);
            for (JSONObject json : jsonList) {
                if (json.has("type") && "stats".equalsIgnoreCase(json.getString("type"))) {
                    continue;
                }
                if (json.has("currentPatchSet")) {
                    JSONObject currentPatchSet = json.getJSONObject("currentPatchSet");
                    if (currentPatchSet.has("files")) {
                        JSONArray changedFiles = currentPatchSet.optJSONArray("files");
                        int numberOfFiles = changedFiles.size();

                        if (numberOfFiles > 0) {
                            List<String> files = new ArrayList<String>(numberOfFiles);
                            for (int i = 0; i < changedFiles.size(); i++) {
                                JSONObject file = changedFiles.getJSONObject(i);
                                files.add(file.getString("file"));
                            }
                            return files;
                        }
                    }

                    break;
                }
            }
        } catch (IOException e) {
            logger.error("IOException occurred. ", e);
        } catch (GerritQueryException e) {
            logger.error("Bad query. ", e);
        }
        return null;
    }
}
