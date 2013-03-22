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

package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class ThreadMoveComponent2 extends Window {

    private final VerticalLayout layout = new VerticalLayout();
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "We ignore serialization")
    private final DiscussionThread thread;
    private Tree categories;

    public ThreadMoveComponent2(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        this.thread = thread;

        try {
            setContent(layout);
            layout.setSizeFull();

            setCaption("Move Thread to Category...");
            setWidth("300px");
            setHeight("300px");

            // panel scrolls, that's why we're using it here.
            categories = createCategories(presenter);
            final Panel panel = new Panel(categories);
            panel.setStyleName(Reindeer.PANEL_LIGHT);
            panel.setSizeFull();
            layout.addComponent(panel);
            layout.setExpandRatio(panel, 1.0f);

            final HorizontalLayout horizontalLayout = new HorizontalLayout();
            layout.addComponent(horizontalLayout);

            horizontalLayout.addComponent(new NativeButton("Move Thread",
                    new NativeButton.ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            final Category newCategory = (Category) categories
                                    .getValue();

                            try {
                                presenter.move(thread, newCategory);
                            } catch (final DataSourceException e) {
                                Notification
                                        .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                            }

                            close();
                        }
                    }));
            horizontalLayout.addComponent(new NativeButton("Cancel",
                    new NativeButton.ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            close();
                        }
                    }));
        }

        catch (final DataSourceException e1) {
            e1.printStackTrace();
            setContent(new Label(
                    DataSourceException.BORING_GENERIC_ERROR_MESSAGE));
        }
    }

    private Tree createCategories(final CategoryPresenter presenter)
            throws DataSourceException {
        final Tree tree = new Tree();

        for (final Category rootCategory : presenter.getRootCategories()) {
            addCategory(tree, rootCategory, presenter);
            tree.expandItemsRecursively(rootCategory);
        }

        tree.setValue(thread.getCategory());
        tree.setNullSelectionAllowed(false);
        return tree;
    }

    private void addCategory(final Tree tree, final Category category,
            final CategoryPresenter presenter) throws DataSourceException {
        tree.addItem(category);
        tree.setItemCaption(category, category.getName());

        final List<Category> subCategories = presenter
                .getSubCategories(category);
        if (!subCategories.isEmpty()) {
            for (final Category subCategory : subCategories) {
                addCategory(tree, subCategory, presenter);
                tree.setParent(subCategory, category);
            }
        } else {
            tree.setChildrenAllowed(category, false);
        }
    }
}
