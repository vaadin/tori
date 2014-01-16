/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori;

import org.vaadin.tori.category.CategoryViewImpl;
import org.vaadin.tori.dashboard.DashboardViewImpl;
import org.vaadin.tori.indexing.IndexableCategoryView;
import org.vaadin.tori.indexing.IndexableDashboardView;
import org.vaadin.tori.indexing.IndexableThreadView;
import org.vaadin.tori.indexing.IndexableView;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.thread.ThreadViewImpl;
import org.vaadin.tori.util.PageTitleUpdater;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ToriNavigator extends Navigator {

    public ToriNavigator(UI ui, ComponentContainer container) {
        super(ui, container);
        for (ApplicationView view : ApplicationView.values()) {
            addView(view.getNavigatorUrl(), view.getViewClass());
        }
        setErrorView(ApplicationView.getDefault().getViewClass());

        addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                ((AbstractView<?, ?>) event.getNewView()).init();
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {
                final View newView = event.getNewView();
                if (newView instanceof AbstractView<?, ?>) {
                    String title = ((AbstractView) newView).getTitle();
                    PageTitleUpdater pageTitleUpdater = ToriApiLoader
                            .getCurrent().getPageTitleUpdater();
                    if (pageTitleUpdater != null) {
                        pageTitleUpdater.updatePageTitle(title);
                    }
                }

                String currentFragment = Page.getCurrent().getUriFragment();
                ToriUI.getCurrent().trackAction("#" + currentFragment, null);
            }
        });

    }

    /**
     * All the views of Tori application that can be navigated to.
     */
    public enum ApplicationView {
        // @formatter:off
		DASHBOARD("dashboard", DashboardViewImpl.class,
				IndexableDashboardView.class), CATEGORIES("category",
				CategoryViewImpl.class, IndexableCategoryView.class), THREADS(
				"thread", ThreadViewImpl.class, IndexableThreadView.class);
		// @formatter:on

        private String viewName;
        private Class<? extends View> viewClass;
        private final Class<? extends IndexableView> indexView;

        private static final String NAVIGATOR_URL_PREFIX = "/";
        private static final String URL_PREFIX = "!" + NAVIGATOR_URL_PREFIX;

        private ApplicationView(final String url,
                final Class<? extends View> viewClass,
                final Class<? extends IndexableView> indexView) {
            this.viewName = url;
            this.viewClass = viewClass;
            this.indexView = indexView;
        }

        public String getViewName() {
            return viewName;
        }

        public String getUrl() {
            return URL_PREFIX + viewName;
        }

        public String getNavigatorUrl() {
            return NAVIGATOR_URL_PREFIX + viewName;
        }

        public static ApplicationView getDefault() {
            return DASHBOARD;
        }

        public Class<? extends IndexableView> getIndexableView() {
            return indexView;
        }

        public Class<? extends View> getViewClass() {
            return viewClass;
        }
    }
}
