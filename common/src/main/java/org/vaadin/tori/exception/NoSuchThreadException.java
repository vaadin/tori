package org.vaadin.tori.exception;

public class NoSuchThreadException extends DataSourceException {
    private static final long serialVersionUID = 2621833110049591242L;
    private final long threadId;

    public NoSuchThreadException(final long threadId2, final Throwable e) {
        super(e);
        this.threadId = threadId2;
    }

    public long getThreadId() {
        return threadId;
    }
}
