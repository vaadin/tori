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

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.listing.category.CategoryListingView.CategoryData;
import org.vaadin.tori.view.listing.category.EditCategoryForm.EditCategoryListener;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

@SuppressWarnings("serial")
public class CategoryTreeTable extends TreeTable {

    private static final String PROPERTY_ID_THREADS = "Threads";
    private static final String PROPERTY_ID_UNREAD = "Unread Threads";
    private static final String PROPERTY_ID_CATEGORY = "Category";
    private static final String EDIT_CAPTION = "Edit Category";
    private static final String DELETE_CAPTION = "Delete Category";

    private final CategoryListingPresenter presenter;

    @Override
    protected String formatPropertyValue(Object rowId, Object colId,
            Property<?> property) {
        String result = super.formatPropertyValue(rowId, colId, property);
        if (rowId != getCurrentPageFirstItemId()) {
            if (PROPERTY_ID_THREADS.equals(colId)
                    || PROPERTY_ID_UNREAD.equals(colId)) {
                if ("0".equals(result)) {
                    result = "";
                }
            }
        }
        return result;
    }

    public CategoryTreeTable(CategoryListingPresenter presenter) {
        this.presenter = presenter;
        setStyleName("categoryTree");

        getContainerDataSource().addContainerProperty(PROPERTY_ID_THREADS,
                Integer.class, 0);
        getContainerDataSource().addContainerProperty(PROPERTY_ID_UNREAD,
                Integer.class, 0);

        addGeneratedColumn(PROPERTY_ID_CATEGORY, new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                return new CategoryLayout((CategoryData) itemId);
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
            ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    addComponent(createSettingsMenu(category));
                }
            });

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
            CssLayout result = new CssLayout();
            result.addStyleName("settingsmenu");
            MenuBar settingsMenuBar = ComponentUtil.getDropdownMenu();
            MenuItem root = settingsMenuBar.getMoreMenuItem();
            final PopupButton editPopup = createEditPopup(categoryData);

            Command command = new Command() {
                @Override
                public void menuSelected(MenuItem selectedItem) {
                    if (EDIT_CAPTION.equals(selectedItem.getText())) {
                        editPopup.setPopupVisible(true);
                    } else if (DELETE_CAPTION.equals(selectedItem.getText())) {
                        confirmDelete(categoryData);
                    }
                }
            };

            if (categoryData.mayEditCategory()) {
                root.addItem(EDIT_CAPTION, command);
            }
            if (categoryData.mayDeleteCategory()) {
                root.addItem(DELETE_CAPTION, command);
            }

            if (root.hasChildren()) {
                result.addComponent(settingsMenuBar);
                result.addComponent(editPopup);
            }
            return result;
        }

    }

    protected void confirmDelete(final CategoryData categoryData) {
        final String title = String.format(String.format(
                "Really delete category \"%s\" and all of its contents?",
                categoryData.getName()));
        ConfirmDialog.show(getUI(), title, new ConfirmDialog.Listener() {
            @Override
            public void onClose(ConfirmDialog arg0) {
                if (arg0.isConfirmed()) {
                    presenter.deleteCategory(categoryData.getId());
                }
            }
        });
    }

    public PopupButton createEditPopup(final CategoryData categoryData) {
        final PopupButton editPopup = new PopupButton("");
        final EditCategoryListener listener = new EditCategoryListener() {
            @Override
            public void cancel() {
                editPopup.setPopupVisible(false);
            }

            @Override
            public void commit(String name, String description) {
                editPopup.setPopupVisible(false);
                categoryData.setName(name);
                categoryData.setDescription(description);
                CategoryTreeTable.this.markAsDirtyRecursive();
                presenter.updateCategory(categoryData.getId(), name,
                        description);
            }
        };
        editPopup.setContent(new EditCategoryForm(listener, categoryData));
        return editPopup;
    }

    public void setCategories(List<CategoryData> categories) {
        removeAllItems();
        for (final CategoryData category : categories) {
            addCategory(category, null);
        }
    }

    private void addCategory(final CategoryData category,
            final CategoryData parent) {
        final Item item = addItem(category);
        setChildrenAllowed(category, false);
        if (parent != null) {
            setChildrenAllowed(parent, true);
            setParent(category, parent);
        }

        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                item.getItemProperty(PROPERTY_ID_THREADS).setValue(
                        category.getThreadCount());
                item.getItemProperty(PROPERTY_ID_UNREAD).setValue(
                        category.getUnreadThreadCount());

                List<CategoryData> subCategories = category.getSubCategories();
                if (!subCategories.isEmpty()) {
                    // recursively add all sub categories
                    for (final CategoryData subCategory : subCategories) {
                        addCategory(subCategory, category);
                    }
                }
            }
        });

    }

}