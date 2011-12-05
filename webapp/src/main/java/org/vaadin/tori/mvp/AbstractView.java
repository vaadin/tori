package org.vaadin.tori.mvp;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

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

    private ToriNavigator navigator;

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
        this.navigator = navigator;
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
    public final void navigateTo(final String[] arguments) {
        if (log.isDebugEnabled()) {
            log.debug("Activating view "
                    + getClass().getName()
                    + (arguments != null ? " with params: "
                            + Arrays.toString(arguments) : ""));
        }
        navigationTo(arguments);
    }

    /**
     * This method is called on each visit of this view.
     * <p/>
     * <strong>Tip:</string> use this method to pass the viewed object id to the
     * presenter for parsing.
     * 
     * @param arguments
     *            the {@link String} parameter passed to this view.
     */
    protected abstract void navigationTo(String[] arguments);

    @Override
    public String getWarningForNavigatingFrom() {
        return null;
    }

    protected ToriNavigator getNavigator() {
        return navigator;
    }
}
