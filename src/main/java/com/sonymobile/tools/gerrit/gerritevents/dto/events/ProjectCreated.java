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

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.RepositoryModifiedEvent;
import net.sf.json.JSONObject;

import static com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory.getString;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROJECT_NAME;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HEAD_NAME;

/**
 * A DTO representation of the project-created Gerrit Event.
 *
 * @author Sven Selberg &lt;sven.selberg@sonymobile.com&gt;
 */
public class ProjectCreated extends GerritTriggeredEvent implements RepositoryModifiedEvent {

    /**
     * Name of the created project.
     */
    private String projectName;

    /**
     * HEAD reference name.
     */
    private String headName;

    /**
     *
     * @return the name of HEAD.
     */
    public String getHeadName() {
        return headName;
    }

    /**
     * Sets the name of HEAD.
     * @param headName the name of HEAD.
     */
    public void setHeadName(String headName) {
        this.headName = headName;
    }

    /**
     *
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * sets the project name.
     * @param projectName the name of the project.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        projectName = getString(json, PROJECT_NAME);
        headName = getString(json, HEAD_NAME);
    }

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.PROJECT_CREATED;
    }

    @Override
    public boolean isScorable() {
        return false;
    }

    @Override
    public String getModifiedProject() {
        return projectName;
    }

    @Override
    public String getModifiedRef() {
        return headName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ProjectCreated that = (ProjectCreated)o;
        return projectName.equals(that.projectName);
    }

    @Override
    public int hashCode() {
        return projectName.hashCode();
    }
}
