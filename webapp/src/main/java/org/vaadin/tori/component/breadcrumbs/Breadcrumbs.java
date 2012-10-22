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

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent implements
        CategorySelectionListener, ThreadSelectionListener {

    static final String STYLE_CRUMB = "crumb";
    static final String STYLE_THREAD = "thread";
    static final String STYLE_CATEGORY = "category";
    static final String STYLE_UNCLICKABLE = "unclickable";

    private final HorizontalLayout layout;
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
        layout = new HorizontalLayout();
        layout.setStyleName("breadcrumbs-layout");

        final CssLayout wrapper = new CssLayout();
        wrapper.setWidth("100%");
        wrapper.addComponent(layout);
        wrapper.addComponent(new ActionBar());

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(wrapper);
        mainLayout.addComponent(viewCaption);
        setCompositionRoot(mainLayout);

        renderBreadCrumb();
    }

    private void renderBreadCrumb() {
        layout.removeAllComponents();
        final Button dashboardButton = new Button("Dashboard");
        dashboardButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                navigator.navigateTo(ToriNavigator.ApplicationView.DASHBOARD);
            }
        });
        layout.addComponent(dashboardButton);

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
            final Component categoryCrumb = new CategoryCrumb.Clickable(
                    currentCategory, this);
            final Component threadCrumb = new ThreadCrumb(currentThread, this);
            threadCrumb.setStyleName("last");
            layout.addComponent(categoryCrumb);
            layout.addComponent(threadCrumb);
            showViewCaption(currentThread.getTopic());
        } else if (currentCategory != null) {
            final Component categoryCrumb = new CategoryCrumb.Clickable(
                    currentCategory, this);
            categoryCrumb.setStyleName("last");
            layout.addComponent(categoryCrumb);
            showViewCaption(currentCategory.getName());
        } else {
            hideViewCaption();
        }
    }

    private void paint(final CategoryView categoryView) {
        final Category currentCategory = categoryView.getCurrentCategory();
        if (currentCategory != null) {
            final CategoryCrumb crumb = new CategoryCrumb.UnClickable(
                    currentCategory, this);
            crumb.setStyleName("last");
            layout.addComponent(crumb);
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
