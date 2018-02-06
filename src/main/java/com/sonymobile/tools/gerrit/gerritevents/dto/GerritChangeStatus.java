package com.sonymobile.tools.gerrit.gerritevents.dto;

/**
 Contains constants that represent the status of changes.
 */
public enum GerritChangeStatus {
    /**
     * Change is open.
     */
    NEW("NEW"),
    /**
     * Change was merged.
     */
    MERGED("MERGED"),
    /**
     * Change was abandoned.
     */
    ABANDONED("ABANDONED"),
    /**
     * Catch-all type if Gerrit adds a new ChangeStatus we don't know about.
     */
    UNKNOWN("UNKNOWN");

    private final String status;

    /**
     * Internal constructor for GerritChangeStatus enum.
     * @param status string value returned in JSON
     */
    GerritChangeStatus(String status) {
        this.status = status;
    }

    /**
     * Look up the GerritChangeStatus from a string representation.
     * @param status the GerritChangeStatus returned in JSON
     * @return GerritChangeStatus enum value
     */
    public static GerritChangeStatus fromString(String status) {
        for (GerritChangeStatus s : GerritChangeStatus.values()) {
            if (s.status.equals(status)) {
                return s;
            }
        }
        return UNKNOWN;
    }
}
