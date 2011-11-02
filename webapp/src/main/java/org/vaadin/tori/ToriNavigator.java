package org.vaadin.tori;

import org.vaadin.navigator.Navigator;
import org.vaadin.tori.dashboard.DashboardViewImpl;

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

}