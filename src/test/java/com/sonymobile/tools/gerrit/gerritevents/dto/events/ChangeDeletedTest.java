/*
 *  The MIT License
 *
 *  Copyright 2024 Ericsson.
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

package com.sonymobile.tools.gerrit.gerritevents.dto.events;

import com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ChangeDeletedTest {

    /**
     * Tests that changeDeleted events are parsed correctly from
     * file
     * @throws IOException if we cannot load json from file.
     */
    @Test
    public void fromJsonShouldDeserializeChangeDeletedCorrectly() throws IOException {
        InputStream stream = getClass().getResourceAsStream("DeserializeChangeDeletedTest.json");
        String json = IOUtils.toString(stream);
        JSONObject jsonObject = JSONObject.fromObject(json);
        GerritEvent evt = GerritJsonEventFactory.getEvent(jsonObject);
        GerritTriggeredEvent gEvt = (GerritTriggeredEvent)evt;
        assertEquals(GerritEventType.CHANGE_DELETED, gEvt.getEventType());
        ChangeDeleted deleted = (ChangeDeleted) gEvt;
        assertEquals("admin", deleted.getDeleter().getUsername());
        assertEquals("bwnUVH", deleted.change.getProject());
    }
}
