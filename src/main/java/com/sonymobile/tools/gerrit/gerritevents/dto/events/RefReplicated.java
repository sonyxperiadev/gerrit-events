/*
 *  The MIT License
 *
 *  Copyright 2013 Ericsson.
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

import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getString;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REF;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.STATUS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.TARGET_NODE;
import net.sf.json.JSONObject;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;

/**
 * A DTO representation of the ref-replicated Gerrit Event.
 *
 * @author Hugo Arès &lt;hugo.ares@ericsson.com&gt;
 */
public class RefReplicated extends GerritTriggeredEvent {

    /**
     * Project path in Gerrit.
     */
    private String project;
    /**
     * Ref name within project.
     */
    private String ref;
    /**
     * Status of the replication.
     */
    private String status;
    /**
     * Target node name of the replication.
     */
    private String targetNode;

    @Override
    public String getEventType() {
        return GerritEventType.REF_REPLICATED.getTypeValue();
    }

    @Override
    public boolean isScorable() {
        return false;
    }

    @Override
    public void fromJson(JSONObject json) {
        project = getString(json, PROJECT);
        ref = getString(json, REF);
        status = getString(json, STATUS);
        targetNode = getString(json, TARGET_NODE);
        super.fromJson(json);
    }

    /**
     * Project name.
     * @return the project
     */
    public String getProject() {
        return project;
    }

    /**
     * Project name.
     * @param project the project to set
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * Ref within the project.
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * Ref within the project.
     * @param ref the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Status of the replication.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Status of the replication.
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Target node name of the replication.
     * @return the targetNode
     */
    public String getTargetNode() {
        return targetNode;
    }

    /**
     * Target node name of the replication.
     * @param targetNode the targetNode to set
     */
    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        //CS IGNORE MagicNumber FOR NEXT 6 LINES. REASON: Autogenerated Code.
        //CS IGNORE AvoidInlineConditionals FOR NEXT 6 LINES. REASON: Autogenerated Code.
        final int prime = 31;
        int result = 1;
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        result = prime * result + ((ref == null) ? 0 : ref.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((targetNode == null) ? 0 : targetNode.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RefReplicated other = (RefReplicated)obj;
        if (project == null) {
            if (other.project != null) {
                return false;
            }
        } else if (!project.equals(other.project)) {
            return false;
        }
        if (ref == null) {
            if (other.ref != null) {
                return false;
            }
        } else if (!ref.equals(other.ref)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        if (targetNode == null) {
            if (other.targetNode != null) {
                return false;
            }
        } else if (!targetNode.equals(other.targetNode)) {
            return false;
        }
        return true;
    }
}
