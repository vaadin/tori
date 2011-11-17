package org.vaadin.tori.component.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.tori.component.ContextMenu.ContextAction;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;

class CategoryListingPresenter extends Presenter<CategoryListingView> {

    private List<Category> categories;
    private Category currentRoot;

    public CategoryListingPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    @Override
    public void init() {
        getView().setAdminControlsVisible(
                authorizationService.isCategoryAdministrator());
    }

    public long getThreadCount(final Category category) {
        return dataSource.getThreadCount(category);
    }

    public long getUnreadThreadCount(final Category category) {
        return dataSource.getUnreadThreadCount(category);
    }

    public void applyRearrangement() {
        final Set<Category> modifiedCategories = getView()
                .getModifiedCategories();
        if (log.isDebugEnabled()) {
            log.debug("Saving " + modifiedCategories.size()
                    + " modified categories.");
        }
        if (!modifiedCategories.isEmpty()) {
            dataSource.saveCategories(modifiedCategories);

            // reload the new order from database
            reloadCategoriesFromDataSource();
        }
    }

    private void reloadCategoriesFromDataSource() {
        if (currentRoot == null) {
            setCategories(dataSource.getRootCategories());
        } else {
            setCategories(dataSource.getSubCategories(currentRoot));
        }
    }

    public void cancelRearrangement() {
        if (!getView().getModifiedCategories().isEmpty()) {
            // restore the original categories
            getView().displayCategories(categories);
        }
    }

    public void setCategories(final List<Category> categories) {
        this.categories = categories;
        if (!categories.isEmpty()) {
            currentRoot = categories.get(0).getParentCategory();
        }
        getView().displayCategories(categories);
    }

    public List<Category> getSubCategories(final Category category) {
        return dataSource.getSubCategories(category);
    }

    public Category getCurrentRoot() {
        return currentRoot;
    }

    public void createNewCategory(final String name, final String description) {
        final Category newCategory = new Category();
        newCategory.setName(name);
        newCategory.setDescription(description);
        newCategory.setParentCategory(currentRoot);
        newCategory.setDisplayOrder(getMaxDisplayOrder() + 1);

        // TODO validation, error handling
        dataSource.saveCategory(newCategory);
        getView().hideCreateCategoryForm();

        // refresh the categories
        reloadCategoriesFromDataSource();
    }

    /**
     * Returns the maximum display order within the {@link Category Categories}
     * currently displayed under the {@code currentRoot}.
     * 
     * @return the maximum display order under the {@code currentRoot}.
     */
    int getMaxDisplayOrder() {
        int max = 0;
        if (categories != null) {
            for (final Category category : categories) {
                if (category.getDisplayOrder() > max) {
                    max = category.getDisplayOrder();
                }
            }
        }
        return max;
    }

    public List<ContextMenuItem> getContextMenuItems(final Category category) {
        final List<ContextMenuItem> items = new ArrayList<ContextMenuItem>();
        if (authorizationService.mayFollowCategory(category)) {
            items.add(new ContextMenuItem(new ThemeResource(
                    "images/icon-pin.png"), "Follow category",
                    new ContextAction() {
                        @Override
                        public void contextClicked() {
                            if (log.isDebugEnabled()) {
                                log.debug("Following " + category.getName());
                            }
                        }
                    }));
        }
        if (authorizationService.mayMoveCategory(category)) {
            items.add(new ContextMenuItem(new ThemeResource(
                    "images/icon-rearrange.png"), "Move category",
                    new ContextAction() {
                        @Override
                        public void contextClicked() {
                            if (log.isDebugEnabled()) {
                                log.debug("Moving " + category.getName());
                            }
                        }
                    }));
        }
        if (authorizationService.mayDeleteCategory(category)) {
            items.add(new ContextMenuItem(new ThemeResource(
                    "images/icon-delete.png"), "Delete category",
                    new ContextAction() {
                        @Override
                        public void contextClicked() {
                            if (log.isDebugEnabled()) {
                                log.debug("Deleting " + category.getName());
                            }
                        }
                    }));
        }
        if (authorizationService.mayEditCategory(category)) {
            items.add(new ContextMenuItem(new ThemeResource(
                    "images/icon-edit.png"), "Edit category",
                    new ContextAction() {
                        @Override
                        public void contextClicked() {
                            if (log.isDebugEnabled()) {
                                log.debug("Editing " + category.getName());
                            }
                        }
                    }));
        }
        return items;
    }

    public static class ContextMenuItem {
        Resource icon;
        String caption;
        ContextAction action;

        public ContextMenuItem(final Resource icon, final String caption,
                final ContextAction action) {
            this.icon = icon;
            this.caption = caption;
            this.action = action;
        }
    }

}
