/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Mobile Communications Inc. All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents.dto;

/**
 * Contains constants that represent the names of Gerrit Event JSON properties.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public abstract class GerritEventKeys {
    /**
     * abandoner.
     */
    public static final String ABANDONER = "abandoner";
    /**
     * change.
     */
    public static final String CHANGE = "change";
    /**
     * patchset.
     */
    public static final String PATCHSET = "patchset";
    /**
     * patchSet.
     */
    public static final String PATCH_SET = "patchSet";

    /**
     * email.
     */
    public static final String EMAIL = "email";
    /**
     * name.
     */
    public static final String NAME = "name";
    /**
     * branch.
     */
    public static final String BRANCH = "branch";
    /**
     * topic.
     */
    public static final String TOPIC = "topic";
    /**
     * id.
     */
    public static final String ID = "id";
    /**
     * number.
     */
    public static final String NUMBER = "number";
    /**
     * owner.
     */
    public static final String OWNER = "owner";
    /**
     * project.
     */
    public static final String PROJECT = "project";
    /**
     * project name.
     */
    public static final String PROJECT_NAME = "projectName";
    /**
     * restorer.
     */
    public static final String RESTORER = "restorer";
    /**
     * subject.
     */
    public static final String SUBJECT = "subject";
    /**
     * commit message.
     */
    public static final String COMMIT_MESSAGE = "commitMessage";
    /**
     * url.
     */
    public static final String URL = "url";
    /**
     * revision.
     */
    public static final String REVISION = "revision";
    /**
     * parents.
     */
    public static final String PARENTS = "parents";
    /**
     * ref.
     */
    public static final String REF = "ref";
    /**
     * isDraft.
     */
    public static final String IS_DRAFT = "isDraft";
    /**
     * kind.
     */
    public static final String KIND = "kind";
    /**
     * uploader.
     */
    public static final String UPLOADER = "uploader";
    /**
     * submitter.
     */
    public static final String SUBMITTER = "submitter";
    /**
     * refupdate.
     */
    public static final String REFUPDATE = "refUpdate";
    /**
     * refname.
     */
    public static final String REFNAME = "refName";
    /**
     * HEAD reference name.
     */
    public static final String HEAD_NAME = "headName";
    /**
     * oldrev.
     */
    public static final String OLDREV = "oldRev";
    /**
     * newrev.
     */
    public static final String NEWREV = "newRev";
    /**
     * author.
     */
    public static final String AUTHOR = "author";
    /**
     * review approvals.
     */
    public static final String APPROVALS = "approvals";
    /**
     * approval category.
     */
    public static final String TYPE = "type";
    /**
     * approval value.
     */
    public static final String VALUE = "value";
    /**
     * approval updated.
     */
    public static final String UPDATED = "updated";
    /**
     * the approval's old (or previous) value.
     */
    public static final String OLD_VALUE = "oldValue";
    /**
     * approval by.
     */
    public static final String BY = "by";
    /**
     * approval username.
     */
    public static final String USERNAME = "username";
    /**
     * provider.
     */
    public static final String PROVIDER = "provider";
    /**
     * name.
     */
    public static final String HOST = "host";
    /**
     * port.
     */
    public static final String PORT = "port";
    /**
     * protocol (deprecated).
     */
    public static final String PROTOCOL = "proto";
    /**
     * scheme.
     */
    public static final String SCHEME = "scheme";
    /**
     * version.
     */
    public static final String VERSION = "version";
    /**
     * status.
     */
    public static final String STATUS = "status";
    /**
     * targetNode.
     */
    public static final String TARGET_NODE = "targetNode";
    /**
     * nodesCount.
     */
    public static final String NODES_COUNT = "nodesCount";
    /**
     * comments.
     */
    public static final String COMMENTS = "comments";
    /**
     * comment.
     */
    public static final String COMMENT = "comment";
    /**
     * message.
     */
    public static final String MESSAGE = "message";
    /**
     * reviewer.
     */
    public static final String REVIEWER = "reviewer";
    /**
     * createdOn.
     */
    public static final String CREATED_ON = "createdOn";
    /**
     * eventCreatedOn.
     */
    public static final String EVENTCREATED_ON = "eventCreatedOn";
    /**
     * lastUpdated.
     */
    public static final String LAST_UPDATED = "lastUpdated";
    /**
     * PLUGIN: notifier.
     */
    public static final String NOTIFIER = "notifier";
    /**
     * changer.
     */
    public static final String CHANGER = "changer";
    /**
     * oldTopic.
     */
    public static final String OLD_TOPIC = "oldTopic";
    /**
     * reason.
     */
    public static final String REASON = "reason";

    /**
     * Empty default constructor to hinder instantiation.
     */
    private GerritEventKeys() {
    }
}
