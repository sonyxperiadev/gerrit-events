/*
 *  The MIT License
 *
 *  Copyright 2017 rinrinne All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents.util;

// CS IGNORE AvoidStarImport FOR NEXT 2 LINES. REASON: Test code
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Before;
import org.junit.Test;

import java.nio.CharBuffer;

/**
 * A test class for LineQueue.
 *
 * @author rinrinne (rinrin.ne@gmail.com)
 *
 */
public class LineQueueTest {

    /**
     * Link queue.
     */
    private LineQueue lq = null;

    /**
     * Setup for each test.
     */
    @Before
    public void before() {
        // CS IGNORE MagicNumber FOR NEXT 1 LINES. REASON: Test code.
        lq = new LineQueue(21);
    }

    /**
     * Test method for {@link com.sonymobile.tools.gerrit.gerritevents.util.LineQueue#commit()}.
     */
    @Test
    public void testCommit() {
    }

    /**
     * Test one line.
     */
    @Test
    public void testOneLine() {
        CharBuffer cb = lq.getBuffer();
        cb.append("0123456789012345\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("0123456789012345")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test one line with delimiter just before boundary.
     */
    @Test
    public void testOneLineBoundary() {
        CharBuffer cb = lq.getBuffer();
        cb.append("01234567890123456789\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("01234567890123456789")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test two lines.
     */
    @Test
    public void testTwoLines() {
        CharBuffer cb = lq.getBuffer();
        cb.append("0123456789\n012345\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("0123456789")));
        assertThat(lq.poll(), is(equalTo("012345")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test three lines.
     */
    @Test
    public void testThreeLines() {
        CharBuffer cb = lq.getBuffer();
        cb.append("01234\n56789\n012345\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("01234")));
        assertThat(lq.poll(), is(equalTo("56789")));
        assertThat(lq.poll(), is(equalTo("012345")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test three lines but line 1 is empty.
     */
    @Test
    public void testThreeLinesWithEmpty1() {
        CharBuffer cb = lq.getBuffer();
        cb.append("\n0123456789\n012345\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("")));
        assertThat(lq.poll(), is(equalTo("0123456789")));
        assertThat(lq.poll(), is(equalTo("012345")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test three lines but line 2 is empty.
     */
    @Test
    public void testThreeLinesWithEmpty2() {
        CharBuffer cb = lq.getBuffer();
        cb.append("0123456789\n\n012345\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("0123456789")));
        assertThat(lq.poll(), is(equalTo("")));
        assertThat(lq.poll(), is(equalTo("012345")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test three lines but line 3 is empty.
     */
    @Test
    public void testThreeLinesWithEmpty3() {
        CharBuffer cb = lq.getBuffer();
        cb.append("0123456789\n012345\n\n");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("0123456789")));
        assertThat(lq.poll(), is(equalTo("012345")));
        assertThat(lq.poll(), is(equalTo("")));
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test one line but no delimiter.
     */
    @Test
    public void testOneOverCapacityLine() {
        CharBuffer cb = lq.getBuffer();
        cb.append("01234567890123456789");
        lq.commit();
        assertThat(lq.poll(), nullValue());
    }

    /**
     * Test one long line.
     */
    @Test
    public void testOneLongLine() {
        CharBuffer cb = lq.getBuffer();
        cb.append("01234567890123456789");
        lq.commit();
        assertThat(lq.poll(), nullValue());

        cb = lq.getBuffer();
        cb.append("abcdefghijklmnopqrst");
        lq.commit();
        assertThat(lq.poll(), nullValue());

        cb = lq.getBuffer();
        cb.append("012345\n0123456789");
        lq.commit();
        assertThat(lq.poll(), is(equalTo("01234567890123456789abcdefghijklmnopqrst012345")));
        assertThat(lq.poll(), nullValue());
    }
}
