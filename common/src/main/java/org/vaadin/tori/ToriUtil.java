package org.vaadin.tori;

import java.util.Collection;

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
}
