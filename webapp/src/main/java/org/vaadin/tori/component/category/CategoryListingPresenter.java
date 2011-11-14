package org.vaadin.tori.component.category;

import java.util.List;
import java.util.Set;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.Presenter;

class CategoryListingPresenter extends Presenter<CategoryListingView> {

    private final DataSource dataSource;
    private List<Category> categories;

    public CategoryListingPresenter(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        getView().setAdminControlsVisible(
                dataSource.isAdministrator(currentUser));
    }

    public long getThreadCount(final Category category) {
        return dataSource.getThreadCount(category);
    }

    public long getUnreadThreadCount(final Category category) {
        // TODO implement actual unread thread logic
        return 0;
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
            setCategories(dataSource.getRootCategories());
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
        getView().displayCategories(categories);
    }

    public List<Category> getSubCategories(final Category category) {
        return dataSource.getSubCategories(category);
    }

}
