package org.vaadin.tori.exception;

public class NoSuchCategoryException extends DataSourceException {

    private static final long serialVersionUID = -5438302164700870493L;
    private final long categoryId;

    public NoSuchCategoryException(final long categoryId, final Throwable e) {
        super(e);
        this.categoryId = categoryId;
    }

    public long getCategoryId() {
        return categoryId;
    }

}
