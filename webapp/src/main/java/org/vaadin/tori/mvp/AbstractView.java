package org.vaadin.tori.mvp;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public abstract class AbstractView<V extends View, P extends Presenter<V>>
        extends CustomComponent implements View {

    protected final Logger log = Logger.getLogger(getClass());

    private P presenter;
    private Class<V> viewClass;

    /**
     * Creates the composition root for this View, do the actual initialization
     * of the view components in {@link #initView()} method.
     * 
     * @return composition root for this View
     */
    protected abstract Component createCompositionRoot();

    /**
     * Instantiates the Presenter for this View.
     * 
     * @return Presenter for this View.
     */
    protected abstract P createPresenter();

    @Override
    public void init(final ToriNavigator navigator,
            final Application application) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing view " + getClass().getName());
        }

        resolveViewClass();

        // initialize the view first
        setCompositionRoot(createCompositionRoot());
        initView();

        // then initialize the presenter
        presenter = createPresenter();
        presenter.setView(viewClass.cast(this));
        if (log.isDebugEnabled()) {
            log.debug("Initializing presenter "
                    + presenter.getClass().getName());
        }
        presenter.init();
    }

    @SuppressWarnings("unchecked")
    private void resolveViewClass() {
        final Type[] actualTypeArguments = ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments();
        this.viewClass = (Class<V>) actualTypeArguments[0];
    }

    protected P getPresenter() {
        return presenter;
    }

    @Override
    public void navigateTo(final String requestedDataId) {
        if (log.isDebugEnabled()) {
            log.debug("Activating view "
                    + getClass().getName()
                    + (requestedDataId != null ? " with params: "
                            + requestedDataId : ""));
        }
    }

    @Override
    public String getWarningForNavigatingFrom() {
        return null;
    }

}
