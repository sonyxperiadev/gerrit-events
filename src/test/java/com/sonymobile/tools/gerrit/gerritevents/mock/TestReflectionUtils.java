/*
 * The MIT License
 *
 * Copyright 2026 Robert Sandell
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
package com.sonymobile.tools.gerrit.gerritevents.mock;

import java.lang.reflect.Field;

/**
 * Minimal reflection helper for tests, replacing the small subset of
 * {@code org.powermock.reflect.Whitebox} that this project used after PowerMock was dropped.
 */
public final class TestReflectionUtils {

    /**
     * Utility class, no instances.
     */
    private TestReflectionUtils() {
    }

    /**
     * Reads the value of a private (or otherwise inaccessible) field, searching up the class
     * hierarchy for the first field with the given name.
     *
     * @param object    the instance to read the field from.
     * @param fieldName the name of the field.
     * @param <T>       the expected type of the field value.
     * @return the field value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInternalState(Object object, String fieldName) {
        Class<?> type = object.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T)field.get(object);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not read field '" + fieldName + "'", e);
            }
        }
        throw new IllegalArgumentException("No field named '" + fieldName
                + "' found in " + object.getClass());
    }
}
