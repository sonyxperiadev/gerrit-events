package com.sonymobile.tools.gerrit.gerritevents.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GerritEventTypeTest {
    
    @Test
    public void testSetInteresting() {
        GerritEventType type = GerritEventType.CHANGE_MERGED;
        type.setInteresting(false);
        assertEquals(false, type.isInteresting());
        type.setInteresting(true);
        assertEquals(true, type.isInteresting());
    }
}