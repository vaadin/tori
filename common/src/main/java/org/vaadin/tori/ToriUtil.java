/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public final class ToriUtil {
    private ToriUtil() {
        // not instantiable
    }

    /**
     * Checks whether the given <code>object</code> is <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *             if <code>object</code> is <code>null</code>.
     */
    public static void checkForNull(final Object object,
            final String errorMessage) throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void checkForNullAndEmpty(final Collection<?> collection,
            final String nullErrorMessage, final String emptyErrorMessage)
            throws IllegalArgumentException {
        checkForNull(collection, nullErrorMessage);
        if (collection.isEmpty()) {
            throw new IllegalArgumentException(emptyErrorMessage);
        }
    }

    public static void checkForNullAndEmpty(final String string,
            final String nullErrorMessage, final String emptyErrorMessage)
            throws IllegalArgumentException {
        checkForNull(string, nullErrorMessage);
        if (string.isEmpty()) {
            throw new IllegalArgumentException(emptyErrorMessage);
        }
    }

    /** Remove the first object in an array */
    @java.lang.SuppressWarnings("unchecked")
    @SuppressWarnings(justification = "Java doesn't support generics in this case")
    public static <T extends Object> T[] tail(final T[] array) {
        ToriUtil.checkForNull(array, "array must not be null");

        final List<T> list = new ArrayList<T>();
        for (int i = 1; i < array.length; i++) {
            list.add(array[i]);
        }

        return (T[]) list.toArray();
    }

    public static String escapeXhtml(final String xhtml) {
        return xhtml.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
