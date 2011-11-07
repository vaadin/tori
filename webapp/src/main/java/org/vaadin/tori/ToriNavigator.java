package org.vaadin.tori;

import org.vaadin.navigator.Navigator;
import org.vaadin.tori.category.CategoryViewImpl;
import org.vaadin.tori.dashboard.DashboardViewImpl;
import org.vaadin.tori.thread.ThreadViewImpl;

@SuppressWarnings("serial")
public class ToriNavigator extends Navigator {

    /**
     * All the views of Tori application that can be navigated to.
     */
    public enum ApplicationView {
        DASHBOARD("!dashboard", DashboardViewImpl.class), CATEGORIES(
                "!category",
                CategoryViewImpl.class), THREADS(
                "!thread",
                ThreadViewImpl.class);

        private String url;
        private Class<? extends org.vaadin.tori.mvp.View> viewClass;

        private ApplicationView(final String url,
                final Class<? extends org.vaadin.tori.mvp.View> viewClass) {
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
