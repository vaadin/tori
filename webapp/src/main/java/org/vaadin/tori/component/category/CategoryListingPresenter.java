package org.vaadin.tori.component.category;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.Presenter;

class CategoryListingPresenter extends Presenter<CategoryListingView> {

    private final DataSource dataSource;

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

}
