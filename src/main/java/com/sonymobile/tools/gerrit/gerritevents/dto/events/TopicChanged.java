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

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.CHANGER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.OLD_TOPIC;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;

import com.sonymobile.tools.gerrit.gerritevents.dto.rest.Topic;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * A DTO representation of the topic-changed Gerrit Event.
 * @author David Pursehouse &lt;david.pursehouse@sonymobile.com&gt;
 */
public class TopicChanged  extends ChangeBasedEvent {

    /**
     * The previous topic name.
     */
    private transient String oldTopic;

    /**
     * The previous topic object.  New topic is in change.topic.
     */
    private Topic oldTopicObject;

    /**
     * The user who triggered this event.
     */
    private Account changer;

    /**
     * Converts old serialized data to newer construct.
     *
     * @return itself
     */
    @SuppressWarnings("unused")
    private Object readResolve() {
        if (StringUtils.isNotEmpty(oldTopic)) {
            oldTopicObject = new Topic(oldTopic);
            oldTopic = null;
        }

        if (oldTopicObject != null && StringUtils.isEmpty(oldTopicObject.getName())) {
            oldTopicObject = null;
        }

        return this;
    }

    /**
     * Set the user who changed the topic.
     * @param changer the user.
     */
    public void setChanger(Account changer) {
        this.changer = changer;
    }

    /**
     * @return the user who changed the topic.
     */
    public Account getChanger() {
        return changer;
    }

    /**
     * Set the old topic.
     * @param oldTopic the old topic.
     */
    public void setOldTopic(String oldTopic) {
        this.oldTopicObject = new Topic(oldTopic);
    }

    /**
     * Shortcut for {@code getOldTopicObject() != null ? getOldTopicObject().getName() : null}.
     * @return the old topic name if exists. null otherwise.
     */
    public String getOldTopic() {
        if (oldTopicObject == null) {
            return null;
        }
        return oldTopicObject.getName();
    }

    /**
     * The old topic info related to this change. Can be null if there is no old topic.
     * @return the old topic.
     */
    public Topic getOldTopicObject() {
        return oldTopicObject;
    }

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.TOPIC_CHANGED;
    }

    @Override
    public boolean isScorable() {
        return false;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);

        if (json.containsKey(CHANGER)) {
            this.changer = new Account(json.getJSONObject(CHANGER));
        }
        if (json.containsKey(OLD_TOPIC)) {
            this.oldTopicObject = new Topic(json.getString(OLD_TOPIC));
        }
    }

}
