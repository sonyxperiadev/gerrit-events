/*
 *  The MIT License
 *
 *  Copyright 2012 Hewlett-Packard Development Company, L.P.
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

import java.util.ArrayList;
import java.util.List;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Approval;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getString;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.AUTHOR;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.APPROVALS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.COMMENT;

/**
 * A DTO representation of the comment-added Gerrit Event.
 * @author James E. Blair &lt;jeblair@hp.com&gt;
 */
public class CommentAdded extends ChangeBasedEvent {
    /**
     * Comment string
     */
    private String comment;

    private List<Approval> approvals = new ArrayList<Approval>();

    @Override
    public String getEventType() {
        return GerritEventType.COMMENT_ADDED.getTypeValue();
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

    @Override
    public boolean isScorable() {
        return true;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        comment = getString(json, COMMENT);
        if (json.containsKey(AUTHOR)) {
            account = new Account(json.getJSONObject(AUTHOR));
        }
        if (json.containsKey(APPROVALS)) {
            JSONArray eventApprovals = json.getJSONArray(APPROVALS);
            for (int i = 0; i < eventApprovals.size(); i++) {
                approvals.add(new Approval(eventApprovals.getJSONObject(i)));
            }
        }
    }
}
