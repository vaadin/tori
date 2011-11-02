package org.vaadin.tori.dashboard;

import org.vaadin.tori.data.DataSource;

import com.github.peholmst.mvp4vaadin.Presenter;

@SuppressWarnings("serial")
public class DashboardPresenter extends Presenter<DashboardView> {

    private final DataSource toriDataSource;

    public DashboardPresenter(final DashboardView view,
            final DataSource toriDataSource) {
        super(view);
        this.toriDataSource = toriDataSource;
    }

    @Override
    public void init() {
        getView().displayCategories(toriDataSource.getAllCategories());
    }

}
