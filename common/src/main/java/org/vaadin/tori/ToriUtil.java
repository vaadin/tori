package org.vaadin.tori;

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
}
