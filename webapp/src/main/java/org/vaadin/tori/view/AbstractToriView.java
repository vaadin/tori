package org.vaadin.tori.view;

import org.apache.log4j.Logger;
import org.vaadin.navigator.Navigator;

import com.github.peholmst.mvp4vaadin.AbstractViewComponent;
import com.github.peholmst.mvp4vaadin.Presenter;
import com.github.peholmst.mvp4vaadin.View;
import com.vaadin.Application;

@SuppressWarnings("serial")
public abstract class AbstractToriView<V extends View, P extends Presenter<V>>
        extends AbstractViewComponent<V, P> implements
        org.vaadin.navigator.Navigator.View {

    protected Logger log = Logger.getLogger(getClass());

    public AbstractToriView() {
        // Call the init() here as we are not running in a container that would
        // support the @PostConstruct annotation.
        init();
    }

    @Override
    public void init(final Navigator navigator, final Application application) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing view: " + getClass().getName());
        }
    }

    @Override
    public void navigateTo(final String requestedDataId) {
        // NOP - override in subclasses if needed
    }

    @Override
    public String getWarningForNavigatingFrom() {
        return null;
    }

}
