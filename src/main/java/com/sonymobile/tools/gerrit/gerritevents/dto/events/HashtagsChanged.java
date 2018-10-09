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
import java.util.List;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HASHTAGS;

/**
 * A DTO representation of the hashtags-changed Gerrit Event.
 * @author Diogo Ferreira &lt;diogo@underdev.org&gt;
 */
public class HashtagsChanged extends ChangeBasedEvent {
    private List<String> hashtags;

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

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);

        if (json.containsKey(HASHTAGS)) {
            JSONArray hashtagsArray = json.getJSONArray("hashtags");
            List<String> result = new ArrayList<String>(hashtagsArray.size());
            for (Object tag : hashtagsArray) {
                result.add(tag.toString());
            }
            this.hashtags = result;
        }
    }
}
