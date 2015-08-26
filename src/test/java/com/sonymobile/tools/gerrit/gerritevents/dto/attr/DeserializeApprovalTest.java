/*
 * The MIT License
 *
 * Copyright 2013 Sony Mobile Communications AB. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonymobile.tools.gerrit.gerritevents.dto.attr;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Serialization handling test for {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Approval}.
 */
public class DeserializeApprovalTest {

    /**
     * Verifies old Approval gets deserialized correctly
     * (Approval::username String was replaced by Approval::by Account).
     *
     * @throws IOException if so.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testApprovalUsernameMigration() throws IOException {
        XStream x = new XStream();
        Approval approval = (Approval)x.fromXML(getClass().getResourceAsStream("DeserializeApprovalTest.xml"));
        assertNotNull(approval.getBy());
        assertEquals("uname", approval.getBy().getUsername());
        assertEquals("uname", approval.getUsername());
        assertNull(Whitebox.getInternalState(approval, "username"));
    }
}
