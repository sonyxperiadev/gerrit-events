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

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REVIEWER;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;

import net.sf.json.JSONObject;

/**
 * A DTO representation of the reviewer-added Gerrit Event.
 * @author David Pursehouse &lt;david.pursehouse@sonymobile.com&gt;
 */
public class ReviewerAdded extends ChangeBasedEvent {

    /**
     * The reviewer who was added.
     */
    private Account reviewer;

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.REVIEWER_ADDED;
    }

    @Override
    public boolean isScorable() {
        return true;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        if (json.containsKey(REVIEWER)) {
            this.reviewer = new Account(json.getJSONObject(REVIEWER));
        }
    }

    /**
     * Set the reviewer.
     * @param reviewer the reviewer.
     */
    public void setReviewer(Account reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * Get the reviewer.
     * @return the reviewer.
     */
    public Account getReviewer() {
        return reviewer;
    }
}
