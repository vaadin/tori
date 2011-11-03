package org.vaadin.tori;

import org.vaadin.navigator.Navigator;
import org.vaadin.tori.dashboard.DashboardViewImpl;
import org.vaadin.tori.view.AbstractToriView;

@SuppressWarnings("serial")
public class ToriNavigator extends Navigator {

    /**
     * All the views of Tori application that can be navigated to.
     */
    public enum ApplicationView {
        DASHBOARD("dashboard", DashboardViewImpl.class);

        private String url;
        private Class<? extends View> viewClass;

        private ApplicationView(final String url,
                final Class<? extends View> viewClass) {
            this.url = url;
            this.viewClass = viewClass;
        }

        public String getUrl() {
            return url;
        }

        private static ApplicationView getDefault() {
            return DASHBOARD;
        }
    }

    public ToriNavigator() {
        // Register all views of the application
        for (final ApplicationView appView : ApplicationView.values()) {
            addView(appView.getUrl(), appView.viewClass);
        }

        setMainView(ApplicationView.getDefault().getUrl());
    }

    /**
     * Instantiate the views with Guice to enable dependency injection.
     */
    @Override
    protected View getOrCreateView(final String uri) {
        @SuppressWarnings("unchecked")
        final Class<? extends View> newViewClass = uriToClass.get(uri);
        if (!classToView.containsKey(newViewClass)) {
            @SuppressWarnings("rawtypes")
            final AbstractToriView view = (AbstractToriView) GuiceInjector
                    .getInstance().getInstance(newViewClass);
            view.init(this, getApplication());
            classToView.put(newViewClass, view);
        }
        final View v = classToView.get(newViewClass);
        return v;
    }

}
