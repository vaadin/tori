package org.vaadin.tori.mvp;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;

public abstract class Presenter<V extends View> {

    protected final Logger log = Logger.getLogger(getClass());
    private V view;

    protected final DataSource dataSource;
    protected final AuthorizationService authorizationService;

    public Presenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        this.dataSource = dataSource;
        this.authorizationService = authorizationService;
    }

    public void setView(final V view) {
        this.view = view;
    }

    public V getView() {
        if (view == null) {
            throw new IllegalStateException("View has not been set yet.");
        }
        return view;
    }

    /**
     * When this method is called, the view has already been initialized and can
     * be obtained by calling {@link #getView()} method.
     */
    public void init() {
        // NOP, subclasses may override
    }

}
