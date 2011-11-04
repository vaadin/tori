package org.vaadin.tori.dashboard;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.mvp.Presenter;

public class DashboardPresenter extends Presenter<DashboardView> {

    private final DataSource dataSource;

    public DashboardPresenter(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        getView().displayCategories(dataSource.getRootCategories());
    }

}
