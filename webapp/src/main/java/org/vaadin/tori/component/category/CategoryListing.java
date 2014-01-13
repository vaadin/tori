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

package org.vaadin.tori.component.category;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.component.category.EditCategoryForm.EditCategoryListener;
import org.vaadin.tori.component.category.RearrangeControls.RearrangeListener;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

/**
 * UI component for displaying a vertical hierarchical list of categories.
 */
@SuppressWarnings("serial")
public class CategoryListing extends
        AbstractView<CategoryListingView, CategoryListingPresenter> implements
        CategoryListingView {

    public enum Mode {

        /**
         * Displays only a single column with all details inside it.
         */
        SINGLE_COLUMN,

        /**
         * Displays normally with a separate column for each detail.
         */
        NORMAL;

    }

    private final Mode mode;
    private CategoryTreeTable categoryTree;
    private VerticalLayout layout;
    private Component adminControls;
    private PopupButton createCategoryButton;
    private Button rearrangeCategoriesButton;
    private RearrangeControls rearrangeControls;
    private HorizontalLayout buttonWrapper;

    public CategoryListing(final Mode listingMode) {
        this.mode = listingMode;

        // Must call init here as this View is not
        // instantiated by ToriNavigator.
        init();
    }

    @Override
    protected Component createCompositionRoot() {
        layout = new VerticalLayout();
        return layout;
    }

    @Override
    public void initView() {
        categoryTree = new CategoryTreeTable(mode);
        layout.addComponent(categoryTree);

        adminControls = createAdminControls();
        layout.addComponent(adminControls);
    }

    @Override
    public void setRearrangeVisible(final boolean visible) {
        rearrangeCategoriesButton.setVisible(visible);
    }

    @Override
    public void setCreateVisible(final boolean visible) {
        createCategoryButton.setVisible(visible);
    }

    public void setCategories(final List<Category> categories,
            final Category root) {
        getPresenter().setCategories(categories, root);
    }

    @Override
    public void displayCategories(final List<Category> categories) {
        categoryTree.removeAllItems();
        for (final Category category : categories) {
            categoryTree.addCategory(category, null);
        }
    }

    private Component createAdminControls() {
        rearrangeCategoriesButton = new Button("Rearrange categories",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        setRearranging(true);
                    }
                });
        rearrangeCategoriesButton.setIcon(new ThemeResource(
                "images/icon-rearrange.png"));
        createCategoryButton = new PopupButton("Create a new category");
        createCategoryButton.addStyleName("createcategorybutton");
        createCategoryButton.setIcon(new ThemeResource("images/icon-add.png"));
        final EditCategoryForm editCategoryForm = new EditCategoryForm(
                new EditCategoryListener() {
                    @Override
                    public void commit(final String name,
                            final String description) {
                        try {
                            getPresenter().createNewCategory(name, description);

                            /*
                             * this is an ugly piece of code. Maybe the edit
                             * listener could be propagated outside of
                             * CategoryListing. It's either this code, or giving
                             * CategoryViewImpl access to the edit event.
                             */
                            replaceCreateCategoryButton();
                            setVisible(true);
                        } catch (final DataSourceException e) {
                            /*
                             * FIXME: make sure that no categories were added to
                             * the UI.
                             */
                            Notification.show("Couldn't save "
                                    + "your modifications :(");
                        }
                    }
                });
        createCategoryButton.setComponent(editCategoryForm);
        createCategoryButton
                .addPopupVisibilityListener(new PopupVisibilityListener() {
                    @Override
                    public void popupVisibilityChange(
                            final PopupVisibilityEvent event) {
                        if (event.isPopupVisible()) {
                            editCategoryForm.getTitleField().focus();
                        }
                    }
                });

        rearrangeControls = new RearrangeControls(new RearrangeListener() {
            @Override
            public void applyRearrangement() {
                setRearranging(false);

                try {
                    getPresenter().applyRearrangement();
                } catch (final DataSourceException e) {
                    /* FIXME: refresh view to show that nothing was rearranged? */
                    Notification.show("Couldn't save new arrangement :(");
                }
            }

            @Override
            public void cancelRearrangement() {
                setRearranging(false);
                getPresenter().cancelRearrangement();
            }
        });
        rearrangeControls.setVisible(false);

        buttonWrapper = new HorizontalLayout();
        buttonWrapper.setSpacing(true);
        buttonWrapper.addComponent(createCategoryButton);
        buttonWrapper.addComponent(rearrangeCategoriesButton);

        final HorizontalLayout adminControls = new HorizontalLayout();
        adminControls.setWidth("100%");
        adminControls.addComponent(buttonWrapper);
        adminControls.addComponent(rearrangeControls);
        adminControls.setComponentAlignment(rearrangeControls,
                Alignment.TOP_RIGHT);
        // adminControls.setMargin(true, false, true, false);
        return adminControls;
    }

    @Override
    public void hideCreateCategoryForm() {
        createCategoryButton.setPopupVisible(false);
    }

    private void setRearranging(final boolean rearranging) {
        createCategoryButton.setEnabled(!rearranging);
        categoryTree.setDraggingEnabled(rearranging);
        rearrangeCategoriesButton.setVisible(!rearranging);
        rearrangeControls.setVisible(rearranging);
    }

    @Override
    public Set<Category> getModifiedCategories() {
        return getModifiedCategories(categoryTree.rootItemIds());
    }

    private Set<Category> getModifiedCategories(
            final Collection<?> itemIdsToCheck) {
        final Set<Category> changed = new HashSet<Category>();

        int index = 0;
        for (final Object itemId : itemIdsToCheck) {
            if (itemId instanceof Category) {
                // check the display order
                final Category category = (Category) itemId;
                if (category.getDisplayOrder() != index) {
                    // update the displayOrder property if reordered
                    category.setDisplayOrder(index);
                    changed.add(category);
                }

                // check the parent
                final Object parent = categoryTree.getParent(itemId);
                if (parent == null) {
                    final Category currentRoot = getPresenter()
                            .getCurrentRoot();
                    if ((currentRoot == null && category.getParentCategory() != null)
                            || (currentRoot != null && !currentRoot
                                    .equals(category.getParentCategory()))) {
                        category.setParentCategory(currentRoot);
                        changed.add(category);
                    }
                } else if (parent instanceof Category) {
                    final Category parentCategory = (Category) parent;
                    if (!parentCategory.equals(category.getParentCategory())) {
                        category.setParentCategory(parentCategory);
                        changed.add(category);
                    }
                }
                index++;
            }

            // recursively add changes from sub categories
            final Collection<?> subCategoryItemIds = categoryTree
                    .getChildren(itemId);
            if (subCategoryItemIds != null && !subCategoryItemIds.isEmpty()) {
                changed.addAll(getModifiedCategories(subCategoryItemIds));
            }
        }
        return changed;
    }

    @Override
    protected CategoryListingPresenter createPresenter() {
        final CategoryListingPresenter presenter = new CategoryListingPresenter(
                this);
        categoryTree.setPresenter(presenter);
        return presenter;
    }

    @Override
    protected void navigationTo(final String[] arguments) {
        // NOP
    }

    /**
     * <p>
     * Remove the create category button, and return the button component.
     * </p>
     * <p>
     * If it's already removed, it just returns the button component
     * </p>
     * 
     * @return createCategoryButton
     * @see #replaceCreateCategoryButton()
     */
    public Component removeAndGetCreateCategoryButton() {
        if (createCategoryButton.getParent() == buttonWrapper) {
            buttonWrapper.removeComponent(createCategoryButton);
        }
        return createCategoryButton;
    }

    /**
     * <p>
     * This method undoes what {@link #removeAndGetCreateCategoryButton()} does:
     * it re-inserts the categorybutton where it should be within
     * {@link CategoryListing} and removes it from wherever it was before.
     * </p>
     * 
     * <p>
     * If the button is already in place, this method does nothing.
     * </p>
     * 
     * @see #removeAndGetCreateCategoryButton()
     */
    public void replaceCreateCategoryButton() {
        if (createCategoryButton.getParent() != buttonWrapper) {
            buttonWrapper.addComponent(createCategoryButton, 0);
        }
    }

    @Override
    public String getTitle() {
        return getPresenter().getCategoryName();
    }
}
