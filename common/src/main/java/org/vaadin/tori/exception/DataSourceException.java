package org.vaadin.tori.exception;

public class DataSourceException extends Exception {
    private static final long serialVersionUID = 1924584777047434430L;

    public DataSourceException() {
        super();
    }

    /**
     * @deprecated If you know enough about the exception to write a string
     *             description, consider subclassing {@link DataSourceException}
     *             instead.
     */
    @Deprecated
    public DataSourceException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @deprecated If you know enough about the exception to write a string
     *             description, consider subclassing {@link DataSourceException}
     *             instead.
     */
    @Deprecated
    public DataSourceException(final String arg0) {
        super(arg0);
    }

    public DataSourceException(final Throwable arg0) {
        super(arg0);
    }
}
