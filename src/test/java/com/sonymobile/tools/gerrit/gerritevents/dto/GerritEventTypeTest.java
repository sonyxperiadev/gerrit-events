package com.sonymobile.tools.gerrit.gerritevents.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link GerritEventType}.
 * @author Christoffer Cortes, christoffer.cortes.sjowall@ericsson.com
 */
public class GerritEventTypeTest {

    /**
     * Tests {@link GerritEventType#setInteresting(boolean)}.
     */
    @Test
    public void testSetInteresting() {
        GerritEventType type = GerritEventType.CHANGE_MERGED;
        try {
            type.setInteresting(false);
            assertEquals(false, type.isInteresting());
            type.setInteresting(true);
            assertEquals(true, type.isInteresting());
        } finally {
            type.setInteresting(true);
        }
    }
}
