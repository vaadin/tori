package org.vaadin.tori.dashboard;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

public class DashboardPresenter extends Presenter<DashboardView> {

    public DashboardPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    @Override
    public void init() {
        getView().displayCategories(dataSource.getRootCategories());
    }

}
