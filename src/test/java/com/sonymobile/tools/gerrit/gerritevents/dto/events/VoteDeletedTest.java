package com.sonymobile.tools.gerrit.gerritevents.dto.events;

import com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Approval;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ioanaanamariamarcu
 */
public class VoteDeletedTest {

    /**
     * Given a vote deleted changed event in JSON format, it can be converted correctly.
     * @throws IOException if the json file cannot be loaded.
     */
    @Test
    public void fromJsonShouldDeserializeVoteDeletedCorrectly() throws IOException {
        InputStream stream = getClass().getResourceAsStream("DeserializeVoteDeletedTest.json");
        String json = IOUtils.toString(stream);
        JSONObject jsonObject = JSONObject.fromObject(json);
        GerritEvent evt = GerritJsonEventFactory.getEvent(jsonObject);

        assertTrue("is a VoteDeleted event", evt instanceof VoteDeleted);
        assertEquals("Approvals match", 1, ((VoteDeleted)evt).getApprovals().size());
        Approval approval = ((VoteDeleted)evt).getApprovals().get(0);
        assertEquals("Type matches", "Validated", approval.getType());
        assertEquals("Value matches", "0", approval.getValue());
        assertEquals("OldValue matches", "-2", approval.getOldValue());
        Account remover = new Account("Evil", "evil@company.com");
        remover.setUsername("evil");
        assertEquals("Remover matches", remover, ((VoteDeleted)evt).getRemover());
    }
}
