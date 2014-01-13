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

import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.category.CategoryView;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.thread.ThreadView;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent {

    static final String STYLE_CRUMB = "crumb";
    static final String STYLE_THREAD = "thread";
    static final String STYLE_CATEGORY = "category";
    static final String STYLE_UNCLICKABLE = "unclickable";

    private final HorizontalLayout layout;
    private final Label viewCaption;

    private transient final ViewChangeListener viewListener = new ViewChangeListener() {
        @Override
        public boolean beforeViewChange(ViewChangeEvent event) {
            return true;
        }

        @Override
        public void afterViewChange(ViewChangeEvent event) {
            renderBreadCrumb(event.getNewView());
        }
    };

    private final ToriNavigator navigator;

    public Breadcrumbs(final ToriNavigator navigator) {
        setStyleName("breadcrumbs");
        this.navigator = navigator;
        navigator.addViewChangeListener(viewListener);

        viewCaption = new Label("");
        viewCaption.addStyleName("viewcaption");
        layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setStyleName("breadcrumbs-layout");

        final HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setStyleName("breadcrumbs-wrapper");
        wrapper.setWidth(100.0f, Unit.PERCENTAGE);
        wrapper.setHeight(32.0f, Unit.PIXELS);
        wrapper.addComponent(layout);
        wrapper.setExpandRatio(layout, 1.0f);
        wrapper.addComponent(new ActionBar());

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(wrapper);
        mainLayout.addComponent(viewCaption);
        setCompositionRoot(mainLayout);

        renderBreadCrumb(null);
    }

    private void renderBreadCrumb(View currentView) {
        layout.removeAllComponents();
        final Link dashboardLink = new Link("Dashboard", new ExternalResource(
                "#" + ToriNavigator.ApplicationView.DASHBOARD.getUrl() + "/"));
        layout.addComponent(dashboardLink);
        layout.setComponentAlignment(dashboardLink, Alignment.MIDDLE_LEFT);
        layout.addComponent(getSeparator());

        if (currentView instanceof CategoryView) {
            final CategoryView categoryView = (CategoryView) currentView;
            paint(categoryView);
        } else if (currentView instanceof ThreadView) {
            final ThreadView threadView = (ThreadView) currentView;
            paint(threadView);
        } else {
            hideViewCaption();
        }

        Component last = layout.getComponent(layout.getComponentCount() - 1);
        layout.setExpandRatio(last, 1.0f);
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
            inputCategoryCrumb(currentCategory);

            final Link threadCrumb = new Link(currentThread.getTopic(),
                    new ExternalResource("#"
                            + ToriNavigator.ApplicationView.THREADS.getUrl()
                            + "/" + currentThread.getId()));
            threadCrumb.addStyleName("threadcrumb");
            threadCrumb.setWidth(100.0f, Unit.PERCENTAGE);
            layout.addComponent(getSeparator());
            layout.addComponent(threadCrumb);
            layout.setComponentAlignment(threadCrumb, Alignment.MIDDLE_LEFT);
            showViewCaption(currentThread.getTopic());
        } else if (currentCategory != null) {
            inputCategoryCrumb(currentCategory);
            showViewCaption(currentCategory.getName());
        } else {
            hideViewCaption();
        }
    }

    private void paint(final CategoryView categoryView) {
        final Category currentCategory = categoryView.getCurrentCategory();
        if (currentCategory != null) {
            inputCategoryCrumb(currentCategory);
            showViewCaption(currentCategory.getName());
        } else {
            hideViewCaption();
        }
    }

    private void inputCategoryCrumb(final Category currentCategory) {
        final Link crumb = new Link(currentCategory.getName(),
                new ExternalResource("#"
                        + ToriNavigator.ApplicationView.CATEGORIES.getUrl()
                        + "/" + currentCategory.getId()));
        layout.addComponent(crumb);
        layout.setComponentAlignment(crumb, Alignment.MIDDLE_LEFT);

        final PopupButton categoryPopup = new PopupButton();
        categoryPopup.addStyleName("categorypopup");
        categoryPopup.setWidth(30.0f, Unit.PIXELS);
        categoryPopup.addPopupVisibilityListener(new PopupVisibilityListener() {
            @Override
            public void popupVisibilityChange(
                    final org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent event) {
                if (event.isPopupVisible()) {
                    categoryPopup.setContent(getCategoryPopup(currentCategory,
                            categoryPopup));
                }
            }
        });
        layout.addComponent(categoryPopup);
        layout.setComponentAlignment(categoryPopup, Alignment.MIDDLE_LEFT);
    }

    public void selectCategory(final Category selectedCategory) {
        final String catIdAsString = String.valueOf(selectedCategory.getId());
        navigator.navigateTo(ToriNavigator.ApplicationView.CATEGORIES
                .getNavigatorUrl() + "/" + catIdAsString);
    }

    private Component getCategoryPopup(final Category currentCategory,
            final PopupButton popupButton) {
        final Tree tree = new Tree();
        tree.setImmediate(true);
        tree.setWidth(250.0f, Unit.PIXELS);
        AuthorizationService authorizationService = ToriApiLoader.getCurrent()
                .getAuthorizationService();

        try {
            final List<Category> rootCategories = ToriApiLoader.getCurrent()
                    .getDataSource().getRootCategories();
            for (final Category category : rootCategories) {
                if (authorizationService.mayView(category)) {
                    addCategory(tree, category, null);
                }
            }
        } catch (final DataSourceException e) {
            getLogger().error(e);
            e.printStackTrace();
            return new Label("Something went wrong :(");
        }

        tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(final ItemClickEvent event) {
                selectCategory((Category) event.getItemId());
                popupButton.setPopupVisible(false);
            }
        });

        highlight(tree, currentCategory);

        return tree;
    }

    private static void highlight(final Tree tree,
            final Category currentCategory) {
        for (final Object itemId : tree.getItemIds()) {
            if (itemId.equals(currentCategory)) {
                /* make sure all the parents to this item are expanded */
                Object parentId = tree.getParent(itemId);
                while (parentId != null) {
                    tree.expandItem(parentId);
                    parentId = tree.getParent(parentId);
                }

                final String itemCaption = tree.getItemCaption(itemId);
                tree.setItemCaption(itemId, "> " + itemCaption);
                return;
            }
        }
    }

    private static void addCategory(final Tree tree, final Category category,
            final Category parent) throws DataSourceException {
        tree.addItem(category);
        tree.setItemCaption(category, category.getName());

        if (parent != null) {
            tree.setParent(category, parent);
        }

        final List<Category> subCategories = ToriApiLoader.getCurrent()
                .getDataSource().getSubCategories(category);
        if (subCategories.isEmpty()) {
            tree.setChildrenAllowed(category, false);
        } else {
            for (final Category subCategory : subCategories) {
                addCategory(tree, subCategory, category);
            }
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(Breadcrumbs.class);
    }

    private Component getSeparator() {
        Label label = new Label();
        label.addStyleName("breadcrumbs-separator");
        label.setHeight(100.0f, Unit.PERCENTAGE);
        label.setWidth(15.0f, Unit.PIXELS);
        return label;
    }
}
