package com.sonymobile.tools.gerrit.gerritevents.dto;

/**
 Contains constants that represent the status of changes.
 */
public enum GerritChangeStatus {
    /**
     * Change is open.
     */
    NEW,
    /**
     * Change was merged.
     */
    MERGED,
    /**
     * Change was abandoned.
     */
    ABANDONED,
    /**
     * Catch-all type if Gerrit adds a new ChangeStatus we don't know about.
     */
    UNKNOWN;

    /**
     * Look up the GerritChangeStatus from a string representation.
     * @param status the GerritChangeStatus returned in JSON
     * @return GerritChangeStatus enum value
     */
    public static GerritChangeStatus fromString(String status) {
        for (GerritChangeStatus s : GerritChangeStatus.values()) {
            if (s.name().equals(status)) {
                return s;
            }
        }
        return UNKNOWN;
    }
}
