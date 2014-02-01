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

package org.vaadin.tori.view.listing.thread;

import java.util.List;

import org.vaadin.tori.data.entity.Category;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class ThreadMoveComponent extends Window {

    private final VerticalLayout layout = new VerticalLayout();
    private final Tree categoriesTree;

    public ThreadMoveComponent(final long threadId,
            final long currentCategoryId, List<Category> allCategories,
            final ThreadMoveComponentListener listener) {
        setClosable(false);
        setResizable(false);

        setContent(layout);
        layout.setSizeFull();

        setCaption("Move Thread to Category...");
        setWidth("300px");
        setHeight("300px");

        categoriesTree = createCategories(allCategories, threadId,
                currentCategoryId);
        final Panel panel = new Panel(categoriesTree);
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
                        final Long newCategoryId = (Long) categoriesTree
                                .getValue();
                        if (newCategoryId != currentCategoryId) {
                            listener.commit(threadId, newCategoryId);
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

    private Tree createCategories(List<Category> allCategories, long threadId,
            long threadCategoryId) {
        final Tree tree = new Tree();

        for (final Category category : allCategories) {
            tree.addItem(category.getId());
            tree.setItemCaption(category.getId(), category.getName());
            tree.setChildrenAllowed(category.getId(), false);

            Category parent = category.getParentCategory();
            if (parent != null) {
                tree.setChildrenAllowed(parent.getId(), true);
                tree.setParent(category.getId(), parent.getId());
                tree.expandItem(parent.getId());
            }
        }

        tree.setValue(threadCategoryId);
        tree.setNullSelectionAllowed(false);
        return tree;
    }

    public interface ThreadMoveComponentListener {
        void commit(long threadId, Long newCategoryId);
    }
}
