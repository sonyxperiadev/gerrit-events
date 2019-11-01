package com.sonymobile.tools.gerrit.gerritevents.dto.events;

import java.util.ArrayList;
import java.util.List;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Approval;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getString;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REVIEWER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.APPROVALS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.COMMENT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REMOVER;

/**
 * A DTO representation of the vote-deleted Gerrit Event.
 * @author ioanaanamariamarcu
 */
public class VoteDeleted extends ChangeBasedEvent {
    /**
     * Comment string
     */
    private String comment;

    private List<Approval> approvals = new ArrayList<Approval>();
    /**
     * The user whose vote was removed.
     */
    private Account reviewer;
    /**
     * The user who removed the vote.
     */
    private Account remover;

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.VOTE_DELETED;
    }

    /**
     * Get the code review approvals associated with this Gerrit comment.
     * @return a list of code review approvals.
     */
    public List<Approval> getApprovals() {
        return approvals;
    }

    /**
     * Set the code review approvals for this event.
     * @param approvals the list of Approvals.
     */
    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }

    /**
     * Comment.
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Comment.
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Set the reviewer.
     * @param reviewer the reviewer.
     */
    public void setReviewer(Account reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * Get the remover.
     * @return the remover.
     */
    public Account getRemover() {
        return remover;
    }

    /**
     * Set the remover.
     * @param remover the remover.
     */
    public void setRemover(Account remover) {
        this.remover = remover;
    }

    /**
     * Get the reviewer.
     * @return the reviewer.
     */
    public Account getReviewer() {
        return reviewer;
    }

    @Override
    public boolean isScorable() {
        return true;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        comment = getString(json, COMMENT);
        if (json.containsKey(REVIEWER)) {
            this.reviewer = new Account(json.getJSONObject(REVIEWER));
        }
        if (json.containsKey(REMOVER)) {
            this.remover = new Account(json.getJSONObject(REMOVER));
        }
        if (json.containsKey(APPROVALS)) {
            JSONArray eventApprovals = json.getJSONArray(APPROVALS);
            for (int i = 0; i < eventApprovals.size(); i++) {
                approvals.add(new Approval(eventApprovals.getJSONObject(i)));
            }
        }
    }
}
