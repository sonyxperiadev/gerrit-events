/*
 *  The MIT License
 *
 *  Copyright 2015 Sony Mobile Communications Inc. All rights reserved.
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

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REASON;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.SUBMITTER;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;

import net.sf.json.JSONObject;

/**
 * A DTO representation of the merge-failed Gerrit Event.
 * @author David Pursehouse &lt;david.pursehouse@sonymobile.com&gt;
 */
public class MergeFailed extends ChangeBasedEvent {

    /**
     * The user who tried to submit the change.
     */
    private Account submitter;

    /**
     * The reason that the merge failed.
     */
    private String reason;

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.MERGE_FAILED;
    }

    @Override
    public boolean isScorable() {
        return true;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        if (json.containsKey(SUBMITTER)) {
            this.submitter = new Account(json.getJSONObject(SUBMITTER));
        }
        if (json.containsKey(REASON)) {
            this.reason = json.getString(REASON);
        }
    }

    /**
     * Get the submitter.
     * @return the submitter.
     */
    public Account getSubmitter() {
        return submitter;
    }

    /**
     * Set the submitter.
     * @param submitter the submitter.
     */
    public void setSubmitter(Account submitter) {
        this.submitter = submitter;
    }

    /**
     * Get the reason.
     * @return the reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set the reason.
     * @param reason the reason.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
