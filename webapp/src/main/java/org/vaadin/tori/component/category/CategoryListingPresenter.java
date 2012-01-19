package org.vaadin.tori.component.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

class CategoryListingPresenter extends Presenter<CategoryListingView> {

    public static enum ContextMenuOperation {
        EDIT, FOLLOW, DELETE
    }

    private List<Category> categories;
    private Category currentRoot;

    public CategoryListingPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    @Override
    public void init() {
        getView().setRearrangeVisible(
                authorizationService.mayRearrangeCategories());
        getView().setCreateVisible(authorizationService.mayEditCategories());
    }

    long getThreadCount(final Category category) throws DataSourceException {
        try {
            return dataSource.getThreadCountRecursively(category);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

    long getUnreadThreadCount(final Category category)
            throws DataSourceException {
        try {
            return dataSource.getUnreadThreadCount(category);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    void applyRearrangement() throws DataSourceException {
        try {
            final Set<Category> modifiedCategories = getView()
                    .getModifiedCategories();
            if (log.isDebugEnabled()) {
                log.debug("Saving " + modifiedCategories.size()
                        + " modified categories.");
            }
            if (!modifiedCategories.isEmpty()) {
                dataSource.save(modifiedCategories);

                // reload the new order from database
                reloadCategoriesFromDataSource();
            }
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    private void reloadCategoriesFromDataSource() throws DataSourceException {
        try {
            if (currentRoot == null) {
                setCategories(dataSource.getRootCategories());
            } else {
                setCategories(dataSource.getSubCategories(currentRoot));
            }
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    void cancelRearrangement() {
        if (!getView().getModifiedCategories().isEmpty()) {
            // restore the original categories
            getView().displayCategories(categories);
        }
    }

    void setCategories(final List<Category> categories) {
        this.categories = categories;
        if (!categories.isEmpty()) {
            currentRoot = categories.get(0).getParentCategory();
        }
        getView().displayCategories(categories);
    }

    List<Category> getSubCategories(final Category category)
            throws DataSourceException {
        try {
            return dataSource.getSubCategories(category);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    Category getCurrentRoot() {
        return currentRoot;
    }

    void createNewCategory(final String name, final String description)
            throws DataSourceException {
        try {
            final Category newCategory = new Category();
            newCategory.setName(name);
            newCategory.setDescription(description);
            newCategory.setParentCategory(currentRoot);
            newCategory.setDisplayOrder(getMaxDisplayOrder() + 1);

            dataSource.save(newCategory);
            getView().hideCreateCategoryForm();

            // refresh the categories
            reloadCategoriesFromDataSource();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

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

    List<ContextMenuOperation> getContextMenuOperations(final Category category) {
        final List<ContextMenuOperation> items = new ArrayList<ContextMenuOperation>();
        if (authorizationService.mayFollow(category)) {
            items.add(ContextMenuOperation.FOLLOW);
        }
        if (authorizationService.mayDelete(category)) {
            items.add(ContextMenuOperation.DELETE);
        }
        if (authorizationService.mayEdit(category)) {
            items.add(ContextMenuOperation.EDIT);
        }
        return items;
    }

    void delete(final Category category) throws DataSourceException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting " + category.getName());
            }
            dataSource.delete(category);
            reloadCategoriesFromDataSource();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    void edit(final Category category, final String name,
            final String description) throws DataSourceException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Editing " + category.getName() + " -> " + name
                        + ", " + category.getDescription() + " -> "
                        + description);
            }
            category.setName(name);
            category.setDescription(description);
            dataSource.save(category);
            reloadCategoriesFromDataSource();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    void follow(final Category category) {
        if (log.isDebugEnabled()) {
            log.debug("Following " + category.getName());
        }
    }

}
