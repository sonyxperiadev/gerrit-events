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

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A queue for line in text.
 *
 * @author rinrinne (rinrin.ne@gmail.com)
 *
 */
public class LineQueue {

    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private static final char LINE_SEPARATOR = "\n".charAt(0);


    private static final Logger logger = LoggerFactory.getLogger(LineQueue.class);

    /**
     * Line Queue.
     */
    protected final Queue<String> lineQueue;
    /**
     * Charactor Buffer to get data from reader.
     */
    protected final CharBuffer charBuffer;
    /**
     * List for remaining data.
     */
    protected final List<String> remainingList;

    /**
     * Default constructor.
     */
    public LineQueue() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructor.
     *
     * @param bufSize the size of CharBuffer
     */
    public LineQueue(int bufSize) {
        lineQueue = new LinkedList<String>();
        charBuffer = CharBuffer.allocate(bufSize);
        remainingList = new LinkedList<String>();
    }

    /**
     * Gets buffer.
     *
     * @return the buffer
     */
    public CharBuffer getBuffer() {
        if (!charBuffer.hasRemaining()) {
            charBuffer.clear();
        }
        return charBuffer;
    }

    /**
     * Commits buffer to line queue.
     *
     * Commited buffer is no longer readable.
     *
     * @return the committed line count
     */
    public int commit() {
        int cnt = 0;
        String line;
        charBuffer.flip();
        while ((line = getLine()) != null) {
            lineQueue.offer(line);
            cnt++;
        }
        if (charBuffer.hasRemaining()) {
            remainingList.add(charBuffer.toString());
            charBuffer.position(charBuffer.limit());
        }
        return cnt;
    }

    /**
     * Polls line from queue.
     *
     * Polled line is removed from queue.
     *
     * @return the line string in the top of queue
     */
    public String poll() {
        return lineQueue.poll();
    }

    /**
     * Checks queue is empty.
     *
     * @return true if queue is empty.
     */
    public boolean isEmpty() {
        return lineQueue.isEmpty();
    }

    /**
     * Checks that has remaining.
     *
     * @return true if has remaining.
     */
    public boolean hasRemainings() {
        return !remainingList.isEmpty();
    }

    /**
     * Gets the list of remainings.
     *
     * Remainings are not removed.
     *
     * @return the list of remainings
     */
    public List<String> getRemainings() {
       return Collections.unmodifiableList(remainingList);
    }

    /**
     * Gets a line string.
     *
     * @return the line string
     */
    protected String getLine() {
        String line = null;
        for (int i = 0; i < charBuffer.length(); i++) {
            if (charBuffer.charAt(i) == LINE_SEPARATOR) {
                line = getSubSequence(charBuffer, 0, i).toString().trim();
                charBuffer.position(charBuffer.position() + i);
                charBuffer.get();
                break;
            }
        }
        if (line != null) {
            if (!remainingList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String data : remainingList) {
                    sb.append(data);
                }
                line = sb.append(line).toString();
                remainingList.clear();
            }
        }
        return line;
    }

    /**
     *  Get sub sequence of buffer.
     *
     *  This method avoids error in java-api-check.
     *  animal-sniffer is confused by the signature of CharBuffer.subSequence()
     *  due to declaration of this method has been changed since Java7.
     *  (abstract -> non-abstract)
     *
     * @param cb a buffer
     * @param start start of sub sequence
     * @param end end of sub sequence
     * @return sub sequence.
     */
    @IgnoreJRERequirement
    private CharSequence getSubSequence(CharBuffer cb, int start, int end) {
        return cb.subSequence(start, end);
    }
}
