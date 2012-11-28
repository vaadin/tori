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

package org.vaadin.tori.component.breadcrumbs;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.category.CategoryView;
import org.vaadin.tori.component.breadcrumbs.CategoryCrumb.CategorySelectionListener;
import org.vaadin.tori.component.breadcrumbs.ThreadCrumb.ThreadSelectionListener;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.thread.ThreadView;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent implements
        CategorySelectionListener, ThreadSelectionListener {

    static final String STYLE_CRUMB = "crumb";
    static final String STYLE_THREAD = "thread";
    static final String STYLE_CATEGORY = "category";
    static final String STYLE_UNCLICKABLE = "unclickable";

    private final CustomLayout layout;
    private final Label viewCaption;

    private transient final ViewChangeListener viewListener = new ViewChangeListener() {
        @Override
        public void navigatorViewChange(final View previous, final View current) {
            renderBreadCrumb();
        }
    };

    private final ToriNavigator navigator;

    public Breadcrumbs(final ToriNavigator navigator) {
        setStyleName("breadcrumbs");
        this.navigator = navigator;
        navigator.addListener(viewListener);

        viewCaption = new Label("");
        layout = new CustomLayout("breadcrumbslayout");
        layout.setStyleName("breadcrumbs-layout");

        final HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setWidth("100%");
        wrapper.addComponent(layout);
        wrapper.setExpandRatio(layout, 1.0f);
        wrapper.addComponent(new ActionBar());

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(wrapper);
        mainLayout.addComponent(viewCaption);
        setCompositionRoot(mainLayout);

        renderBreadCrumb();
    }

    private void renderBreadCrumb() {
        layout.removeAllComponents();
        final Link dashboardLink = new Link("Dashboard", new ExternalResource(
                "#" + ToriNavigator.ApplicationView.DASHBOARD.getUrl() + "/"));
        layout.addComponent(dashboardLink, "dashboard");

        final View currentView = navigator.getCurrentView();
        if (currentView instanceof CategoryView) {
            final CategoryView categoryView = (CategoryView) currentView;
            paint(categoryView);
        } else if (currentView instanceof ThreadView) {
            final ThreadView threadView = (ThreadView) currentView;
            paint(threadView);
        } else {
            hideViewCaption();
        }
    }

    private void showViewCaption(final String caption) {
        viewCaption.setValue(caption);
        viewCaption.setVisible(true);
    }

    private void hideViewCaption() {
        viewCaption.setValue("");
        viewCaption.setVisible(false);
    }

    private void paint(final ThreadView threadView) {
        final DiscussionThread currentThread = threadView.getCurrentThread();
        final Category currentCategory = threadView.getCurrentCategory();

        if (currentThread != null) {
            final Link categoryCrumb = new Link(currentCategory.getName(),
                    new ExternalResource("#"
                            + ToriNavigator.ApplicationView.CATEGORIES.getUrl()
                            + "/" + currentCategory.getId()));
            layout.addComponent(categoryCrumb, "category");

            final Link threadCrumb = new Link(currentThread.getTopic(),
                    new ExternalResource("#"
                            + ToriNavigator.ApplicationView.THREADS.getUrl()
                            + "/" + currentThread.getId()));
            threadCrumb.addStyleName("last");
            layout.addComponent(threadCrumb, "thread");
            showViewCaption(currentThread.getTopic());
        } else if (currentCategory != null) {
            final Link crumb = new Link(currentCategory.getName(),
                    new ExternalResource("#"
                            + ToriNavigator.ApplicationView.CATEGORIES.getUrl()
                            + "/" + currentCategory.getId()));
            crumb.addStyleName("last");
            layout.addComponent(crumb, "category");
            showViewCaption(currentCategory.getName());
        } else {
            hideViewCaption();
        }
    }

    private void paint(final CategoryView categoryView) {
        final Category currentCategory = categoryView.getCurrentCategory();
        if (currentCategory != null) {
            final Link crumb = new Link(currentCategory.getName(),
                    new ExternalResource("#"
                            + ToriNavigator.ApplicationView.CATEGORIES.getUrl()
                            + "/" + currentCategory.getId()));
            crumb.addStyleName("last");
            layout.addComponent(crumb, "category");
            showViewCaption(currentCategory.getName());
        } else {
            hideViewCaption();
        }
    }

    @Override
    public void selectCategory(final Category selectedCategory) {
        final String catIdAsString = String.valueOf(selectedCategory.getId());
        navigator.navigateTo(ToriNavigator.ApplicationView.CATEGORIES,
                catIdAsString);
    }

    @Override
    public void selectThread(final DiscussionThread selectedThread) {
        final String threadIdAsString = String.valueOf(selectedThread.getId());
        navigator.navigateTo(ToriNavigator.ApplicationView.THREADS,
                threadIdAsString);
    }
}
