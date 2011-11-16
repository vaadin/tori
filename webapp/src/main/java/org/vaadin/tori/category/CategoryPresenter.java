package org.vaadin.tori.category;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

public class CategoryPresenter extends Presenter<CategoryView> {

    private Category currentCategory;

    public CategoryPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentCategoryById(final String categoryIdString) {
        Category requestedCategory = null;
        try {
            final long categoryId = Long.valueOf(categoryIdString);
            requestedCategory = dataSource.getCategory(categoryId);
        } catch (final NumberFormatException e) {
            log.error("Invalid category id format: " + categoryIdString);
        }

        if (requestedCategory != null) {
            currentCategory = requestedCategory;

            final CategoryView view = getView();
            view.displaySubCategories(dataSource
                    .getSubCategories(currentCategory));
            view.displayThreads(dataSource.getThreads(currentCategory));
        } else {
            getView().displayCategoryNotFoundError(categoryIdString);
        }
    }

    public Category getCurrentCategory() {
        return currentCategory;
    }

}
