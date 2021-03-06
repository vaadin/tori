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
import org.vaadin.tori.util.ComponentUtil;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
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
            final Long currentCategoryId, final List<Category> allCategories,
            final ThreadMoveComponentListener listener) {
        addStyleName("threadmove");
        setClosable(false);
        setResizable(false);
        setModal(true);

        setContent(layout);
        layout.setSizeFull();
        layout.setMargin(true);

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
        horizontalLayout.setSpacing(true);
        layout.addComponent(horizontalLayout);

        final Component moveButton = new Button("Move Thread",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        final Long newCategoryId = (Long) categoriesTree
                                .getValue();
                        listener.commit(threadId, newCategoryId);
                        close();
                    }
                });
        moveButton.setEnabled(false);
        horizontalLayout.addComponent(moveButton);
        categoriesTree.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                moveButton.setEnabled(event.getProperty().getValue() == null
                        || !event.getProperty().getValue()
                                .equals(currentCategoryId));
            }
        });

        horizontalLayout.addComponent(ComponentUtil.getSecondaryButton(
                "Cancel", new NativeButton.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        close();
                    }
                }));
    }

    private Tree createCategories(final List<Category> allCategories,
            final long threadId, final Long threadCategoryId) {
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
        tree.setImmediate(true);
        return tree;
    }

    public interface ThreadMoveComponentListener {
        void commit(long threadId, Long newCategoryId);
    }
}
