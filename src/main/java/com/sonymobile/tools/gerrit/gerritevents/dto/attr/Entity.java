package com.sonymobile.tools.gerrit.gerritevents.dto.attr;

import net.sf.json.JSONObject;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritJsonDTO;

/**
 * A base Entity object of gerrit JSON entities.
 *
 * @author f.visconte@criteo.com
 *
 */
public class Entity implements GerritJsonDTO {

    private JSONObject json;

    /**
     * Build an entity from a JSONObject instance.
     * @param rawJson a JSONObject instance.
     */
    @Override
    public void fromJson(JSONObject rawJson) {
        this.json = rawJson;
    }

    /**
     * @return the raw JSONObject instance
     */
    @Override
    public JSONObject getJson() {
        return this.json;
    }

}
