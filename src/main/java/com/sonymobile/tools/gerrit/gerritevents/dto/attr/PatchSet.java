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
package com.sonymobile.tools.gerrit.gerritevents.dto.attr;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritChangeKind;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritJsonDTO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getDate;
import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getString;
import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getBoolean;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NUMBER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REVISION;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REF;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.IS_DRAFT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.KIND;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.UPLOADER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.AUTHOR;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.APPROVALS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PARENTS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.CREATED_ON;

/**
 * Represents a Gerrit JSON Patchset DTO.
 * Refers to a specific patchset within a change.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class PatchSet implements GerritJsonDTO {

    /**
     * The patchset number.
     */
    private String number;
    /**
     * Git commit-ish for this patchset.
     */
    private String revision;
    /**
     * The refspec
     */
    private String ref;
    /**
     * The flag for draft patch.
     */
    private boolean draft;
    /**
     * The kind of change uploaded.
     */
    private GerritChangeKind kind;
    /**
     * The one who uploaded the patch-set.
     */
    private Account uploader;
    /**
     * The patch author.
     */
    private Account author;
    /**
     * The list of approvals.
     */
    private List<Approval> approvals;
    /**
     * The list of parent ids.
     */
    private List<String> parents;
    /**
     * The Date when this change was created.
     */
    private Date createdOn;
    /**
     * Default constructor.
     */
    public PatchSet() {
    }

    /**
     * Constructor that fills with data directly.
     * @param json the JSON object with corresponding data.
     */
    public PatchSet(JSONObject json) {
        this.fromJson(json);
    }

    @Override
    public void fromJson(JSONObject json) {
        number = getString(json, NUMBER);
        revision = getString(json, REVISION);
        draft = getBoolean(json, IS_DRAFT);
        createdOn = getDate(json, CREATED_ON);
        if (json.containsKey(KIND)) {
            kind = GerritChangeKind.fromString(getString(json, KIND));
        }
        ref = getString(json, REF);
        if (json.containsKey(UPLOADER)) {
            uploader = new Account(json.getJSONObject(UPLOADER));
        }
        if (json.containsKey(AUTHOR)) {
            author = new Account(json.getJSONObject(AUTHOR));
        }
        if (json.containsKey(APPROVALS)) {
            approvals = new ArrayList<Approval>();
            JSONArray eventApprovals = json.getJSONArray(APPROVALS);
            for (int i = 0; i < eventApprovals.size(); i++) {
                approvals.add(new Approval(eventApprovals.getJSONObject(i)));
            }
        }
        if (json.containsKey(PARENTS)) {
            parents = new ArrayList<String>();
            JSONArray eventParents = json.getJSONArray(PARENTS);
            for (int i = 0; i < eventParents.size(); i++) {
                parents.add(eventParents.getString(i));
            }
        }
    }

    /**
     * @return the List of parent dependency hashes
     */
    public List<String> getParents() {
        return parents;
    }

    /**
     * The patchset number.
     * @return the number.
     */
    public String getNumber() {
        return number;
    }

    /**
     * The list of approvals for this patch set.
     *
     * @return the list of approvals
     */
    public List<Approval> getApprovals() {
        return approvals;
    }


    /**
     * The patchset number.
     * @param number the number.
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Git commit-ish for this patchset.
     * @return the revision commit-ish.
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Git commit-ish for this patchset.
     * @param revision the revision commit-ish.
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Gets the refspec, if it exists.
     * This value in the JSON stream-events data was introduced some version after the stream-events command was,
     * so it might not exist for all stream-events versions of Gerrit.
     * @return the refspec.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the refspec.
     * @param ref the refspec.
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Draft patchset or not.
     * @return true if draft patchset.
     */
    public boolean isDraft() {
        return draft;
    }

    /**
     * Sets the isDraft.
     * @param draft the isDraft.
     */
    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    /**
     * Kind of change uploaded.
     * @return String of the change kind.
     */
    public GerritChangeKind getKind() {
        return kind;
    }

    /**
     * Sets the result of getKind().
     * @param kind the kind of the change.
     */
    public void setKind(GerritChangeKind kind) {
        this.kind = kind;
    }

    /**
     * The one who uploaded the patch-set.
     *
     * @return the account of the uploader.
     */
    public Account getUploader() {
        return uploader;
    }

    /**
     * The patch set author (may not be the owner).
     *
     * @return the author
     */
    public Account getAuthor() {
        return author;
    }

    /**
     * The one who uploaded the patch-set.
     * @param uploader the account of the uploader.
     */
    public void setUploader(Account uploader) {
        this.uploader = uploader;
    }

    /**
     * The Date this change was created on.
     * @return the Date.
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * The Date this change was created on.
     * @param date the Date.
     */
    public void setCreatedOn(Date date) {
        this.createdOn = date;
    }

    @Override
    public boolean equals(Object obj) {
        //CS IGNORE MagicNumber FOR NEXT 15 LINES. REASON: Autogenerated Code.
        //CS IGNORE AvoidInlineConditionals FOR NEXT 15 LINES. REASON: Autogenerated Code.
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PatchSet other = (PatchSet)obj;
        if ((this.number == null) ? (other.number != null) : !this.number.equals(other.number)) {
            return false;
        }
        if ((this.revision == null) ? (other.revision != null) : !this.revision.equals(other.revision)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        //CS IGNORE MagicNumber FOR NEXT 5 LINES. REASON: Autogenerated Code.
        //CS IGNORE AvoidInlineConditionals FOR NEXT 5 LINES. REASON: Autogenerated Code.
        int hash = 7;
        hash = 79 * hash + (this.number != null ? this.number.hashCode() : 0);
        hash = 79 * hash + (this.revision != null ? this.revision.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "PatchSet: " + getNumber();
    }


}
