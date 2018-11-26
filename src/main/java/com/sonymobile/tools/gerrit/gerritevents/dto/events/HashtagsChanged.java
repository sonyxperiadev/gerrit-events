/*
 *  The MIT License
 *
 *  Copyright 2018 Diogo Ferreira <diogo@underdev.org> All rights reserved.
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

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HASHTAGS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HASHTAGS_ADDED;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HASHTAGS_REMOVED;

/**
 * A DTO representation of the hashtags-changed Gerrit Event.
 * @author Diogo Ferreira &lt;diogo@underdev.org&gt;
 */
public class HashtagsChanged extends ChangeBasedEvent {
    private List<String> hashtags;
    private List<String> removedHashtags;
    private List<String> addedHashtags;

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.HASHTAGS_CHANGED;
    }

    @Override
    public boolean isScorable() {
        return false;
    }

    /**
     * Obtains the list of hashtags.
     * @return A list of strings, each containing a hashtag.
     */
    public List<String> getHashtags() {
        return hashtags;
    }

    /**
     * Obtains the list of added hashtags.
     * @return A list of strings, each containing an added hashtag.
     */
    public List<String> getAddedHashtags() {
        return addedHashtags;
    }

    /**
     * Obtains the list of removed hashtags.
     * @return A list of strings, each containing a removed hashtag.
     */
    public List<String> getRemovedHashtags() {
        return removedHashtags;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);

        this.hashtags = hashtagsFromArray(json, HASHTAGS);
        this.removedHashtags = hashtagsFromArray(json, HASHTAGS_REMOVED);
        this.addedHashtags = hashtagsFromArray(json, HASHTAGS_ADDED);
    }

    /**
     * Converts an array key from JSON into a list of the strings it contains.
     *
     * @param json The json object.
     * @param array The array name within the json object.
     * @return A list of strings contained in the array or an empty list if the array is not present.
     */
    private List<String> hashtagsFromArray(JSONObject json, String array) {
        if (json.containsKey(array)) {
            JSONArray hashtagsArray = json.getJSONArray(array);
            List<String> result = new ArrayList<String>(hashtagsArray.size());
            for (Object tag : hashtagsArray) {
                result.add(tag.toString());
            }
            return result;
        }
        return Collections.emptyList();
    }
}
