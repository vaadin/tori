package org.vaadin.tori.category;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.Presenter;

public class CategoryPresenter extends Presenter<CategoryView> {

    private final DataSource dataSource;
    private Category currentCategory;

    public CategoryPresenter(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setCurrentCategoryById(final String requestedDataId) {
        currentCategory = dataSource.getCategory(requestedDataId);
    }

    @Override
    public void init() {
        final CategoryView view = getView();
        view.displaySubCategories(dataSource.getSubCategories(currentCategory));
        view.displayThreads(dataSource.getThreads(currentCategory));
    }
}
