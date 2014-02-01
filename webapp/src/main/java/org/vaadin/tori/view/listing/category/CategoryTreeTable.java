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

package org.vaadin.tori.view.listing.category;

import java.util.List;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.ConfirmationDialog;
import org.vaadin.tori.component.ConfirmationDialog.ConfirmationListener;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.view.listing.category.CategoryListingView.CategoryData;
import org.vaadin.tori.view.listing.category.EditCategoryForm.EditCategoryListener;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

@SuppressWarnings("serial")
public class CategoryTreeTable extends TreeTable {

    private static final String PROPERTY_ID_THREADS = "Threads";
    private static final String PROPERTY_ID_UNREAD = "Unread Threads";
    private static final String PROPERTY_ID_CATEGORY = "Category";

    private final CategoryListingPresenter presenter;

    public CategoryTreeTable(CategoryListingPresenter presenter) {
        this.presenter = presenter;
        setStyleName("categoryTree");

        setContainerDataSource(new BeanItemContainer<CategoryData>(
                CategoryData.class));

        addGeneratedColumn(PROPERTY_ID_CATEGORY, new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                return new CategoryLayout((CategoryData) itemId);
            }
        });

        addGeneratedColumn(PROPERTY_ID_UNREAD, new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                Object result = null;
                int count = ((CategoryData) itemId).getUnreadThreadCount();
                if (count > 0 || source.getCurrentPageFirstItemId() == itemId) {
                    result = count;
                }
                return result;

            }
        });

        addGeneratedColumn(PROPERTY_ID_THREADS, new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                Object result = null;
                int count = ((CategoryData) itemId).getThreadCount();
                if (count > 0 || source.getCurrentPageFirstItemId() == itemId) {
                    result = count;
                }
                return result;
            }
        });

        setColumnExpandRatio(PROPERTY_ID_CATEGORY, 1.0f);
        setColumnWidth(PROPERTY_ID_UNREAD, 120);
        setColumnWidth(PROPERTY_ID_THREADS, 100);

        setColumnAlignment(PROPERTY_ID_UNREAD, Align.RIGHT);
        setColumnAlignment(PROPERTY_ID_THREADS, Align.RIGHT);

        setVisibleColumns(PROPERTY_ID_CATEGORY, PROPERTY_ID_UNREAD,
                PROPERTY_ID_THREADS);

        // set visual properties
        setWidth("100%");
        setPageLength(0);
        setSortEnabled(false);
        setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
    }

    /**
     * Simple layout displaying the category name as a link and the category
     * description. If the {@code CategoryListing} mode is
     * {@link Mode#SINGLE_COLUMN}, this layout also includes additional details.
     */
    private class CategoryLayout extends CssLayout {

        private final String CATEGORY_URL = "#"
                + ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/";

        public CategoryLayout(final CategoryData category) {
            final long id = category.getId();
            final String name = category.getName();
            final String description = category.getDescription();

            setStyleName("category");
            addComponent(createCategoryLink(id, name));
            addComponent(createDescriptionLabel(description));
            addComponent(createSettingsMenu(category));
        }

        private Component createDescriptionLabel(
                final String categoryDescription) {
            final Label description = new Label(categoryDescription);
            description.setStyleName("description");
            description.setWidth(null);
            return description;
        }

        private Component createCategoryLink(final long id, final String name) {
            final Link categoryLink = new Link();
            categoryLink.setCaption(name);
            categoryLink.setResource(new ExternalResource(CATEGORY_URL + id));
            categoryLink.setStyleName("categoryLink");
            categoryLink.setWidth(null);
            return categoryLink;
        }

        private Component createSettingsMenu(final CategoryData categoryData) {
            final ContextMenu contextMenu = new ContextMenu();
            if (categoryData.mayEditCategory()) {
                contextMenu.add("icon-edit", "Edit category",
                        new ContextComponentSwapper() {
                            @Override
                            public Component swapContextComponent() {
                                final EditCategoryListener listener = new EditCategoryListener() {
                                    @Override
                                    public void cancel() {
                                        contextMenu.close();
                                    }

                                    @Override
                                    public void commit(String name,
                                            String description) {
                                        categoryData.setName(name);
                                        categoryData
                                                .setDescription(description);
                                        CategoryTreeTable.this
                                                .markAsDirtyRecursive();
                                        presenter.updateCategory(
                                                categoryData.getId(), name,
                                                description);
                                    }
                                };
                                return new EditCategoryForm(listener,
                                        categoryData);
                            }
                        });
            }
            if (categoryData.mayDeleteCategory()) {
                contextMenu.add("icon-delete", "Delete category",
                        new ContextComponentSwapper() {
                            @Override
                            public Component swapContextComponent() {
                                final String title = String.format(String
                                        .format("Really delete category \"%s\" and all of its contents?",
                                                categoryData.getName()));
                                final String confirmCaption = "Yes, Delete";
                                final String cancelCaption = "No, Cancel!";
                                final ConfirmationListener listener = new ConfirmationListener() {

                                    @Override
                                    public void onConfirmed() {
                                        presenter.deleteCategory(categoryData
                                                .getId());
                                        contextMenu.close();
                                    }

                                    @Override
                                    public void onCancel() {
                                        contextMenu.close();
                                    }
                                };
                                return new ConfirmationDialog(title,
                                        confirmCaption, cancelCaption, listener);

                            }
                        });
            }
            return contextMenu;
        }
    }

    public void setCategories(List<CategoryData> categories) {
        removeAllItems();
        for (final CategoryData category : categories) {
            addCategory(category, null);
        }
    }

    private void addCategory(final CategoryData category,
            final CategoryData parent) {
        addItem(category);
        if (parent != null) {
            setParent(category, parent);
        }

        List<CategoryData> subCategories = category.getSubCategories();

        if (subCategories.isEmpty()) {
            setChildrenAllowed(category, false);
        } else {
            // all categories are collapsed by default
            setCollapsed(category, true);

            // recursively add all sub categories
            for (final CategoryData subCategory : subCategories) {
                addCategory(subCategory, category);
            }
        }
    }

}