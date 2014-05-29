/*
 *  The MIT License
 *
 *  Copyright 2014 Sony Mobile Communications AB. All rights reserved.
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
 * Contains constants that represent the kinds of changes as returned in Gerrit
 * JSON PatchSet types.
 * @author Doug Kelly &lt;dougk.ff7@gmail.com&gt;
 */
public enum GerritChangeKind {
    /**
     * Rework of change (currently the default type).
     */
    REWORK("REWORK"),
    /**
     * Trivial rebase (no merge conflicts).
     */
    TRIVIAL_REBASE("TRIVIAL_REBASE"),
    /**
     * No code changes (same author, same commit parent, possibly different
     * commit message).
     */
    NO_CODE_CHANGE("NO_CODE_CHANGE"),
    /**
     * Catch-all type if Gerrit adds a new ChangeKind we don't know about.
     */
    UNKNOWN("UNKNOWN");

    private final String kind;

    /**
     * Internal constructor for GerritChangeKind enum.
     * @param kind string value returned in JSON
     */
    GerritChangeKind(String kind) {
        this.kind = kind;
    }

    /**
     * Look up the ChangeKind from a string representation.
     * @param kind the ChangeKind returned in JSON
     * @return GerritChangeKind enum value
     */
    public static GerritChangeKind fromString(String kind) {
        for (GerritChangeKind k : GerritChangeKind.values()) {
            if (k.kind.equals(kind)) {
                return k;
            }
        }
        return UNKNOWN;
    }
}
