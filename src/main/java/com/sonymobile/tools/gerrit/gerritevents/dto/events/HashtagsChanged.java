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

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.ADDED;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.EDITOR;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HASHTAGS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REMOVED;

import java.util.ArrayList;
import java.util.List;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * A DTO representation of the hashtags-changed Gerrit Event.
 * @author David Pursehouse &lt;david.pursehouse@sonymobile.com&gt;
 */
public class HashtagsChanged extends ChangeBasedEvent {

    /**
     * The user who changed the hashtags.
     */
    private Account editor;

    /**
     * The hashtags that were added to the change.
     */
    private List<String> added;

    /**
     * The hashtags that were removed from the change.
     */
    private List<String> removed;

    /**
     * Hashtags currently on the change.
     */
    private List<String> hashtags;

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.HASHTAGS_CHANGED;
    }

    @Override
    public boolean isScorable() {
        return true;
    }

    /**
     * Get the user who edited the hashtags.
     * @return the user.
     */
    public Account getEditor() {
        return editor;
    }

    /**
     * Set the user who edited the hashtags.
     * @param editor the user.
     */
    public void setEditor(Account editor) {
        this.editor = editor;
    }

    /**
     * Get the hashtags that were added to the change.
     * @return the hashtags.
     */
    public List<String> getAdded() {
        return added;
    }

    /**
     * Set added hashtags.
     * @param added the hashtags.
     */
    public void setAdded(List<String> added) {
        this.added = added;
    }

    /**
     * Get the hashtags that were removed from the change.
     * @return the hashtags.
     */
    public List<String> getRemoved() {
        return removed;
    }

    /**
     * Set removed hashtags.
     * @param removed the hashtags.
     */
    public void setRemoved(List<String> removed) {
        this.removed = removed;
    }

    /**
     * Get the hashtags on the change.
     * @return the hashtags.
     */
    public List<String> getHashtags() {
        return hashtags;
    }
    /**
     * Set the hashtags on the change.
     * @param hashtags the hashtags.
     */
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    /**
     * Convert a JSON array of Strings to a list of Strings.
     * @param array the array to convert.
     * @return list of Strings found in the Array.
     */
    private List<String> convertJSONArrayOfStringsToList(JSONArray array) {
        List<String> l = new ArrayList<String>();
        for (int ii = 0; ii < array.size(); ii++) {
            l.add(array.getString(ii));
        }
        return l;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        if (json.containsKey(EDITOR)) {
            this.editor = new Account(json.getJSONObject(EDITOR));
        }
        if (json.containsKey(ADDED)) {
            this.added = convertJSONArrayOfStringsToList(json.getJSONArray(ADDED));
        }
        if (json.containsKey(REMOVED)) {
            this.removed = convertJSONArrayOfStringsToList(json.getJSONArray(REMOVED));
        }
        if (json.containsKey(HASHTAGS)) {
            this.hashtags = convertJSONArrayOfStringsToList(json.getJSONArray(HASHTAGS));
        }
    }
}
