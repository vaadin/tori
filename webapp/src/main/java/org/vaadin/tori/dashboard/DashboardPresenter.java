package org.vaadin.tori.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

public class DashboardPresenter extends Presenter<DashboardView> {

    public DashboardPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    @Override
    public void init() {

        try {
            final List<Category> categories = dataSource.getRootCategories();
            for (final Category category : new ArrayList<Category>(categories)) {
                if (!authorizationService.mayView(category)) {
                    categories.remove(category);
                }
            }
            getView().displayCategories(categories);
        } catch (final DataSourceException e) {
            getView().panic();
        }
    }

}
