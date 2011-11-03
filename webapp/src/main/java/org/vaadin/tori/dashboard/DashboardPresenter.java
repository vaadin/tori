package org.vaadin.tori.dashboard;

import javax.inject.Inject;

import org.vaadin.tori.data.DataSource;

import com.github.peholmst.mvp4vaadin.Presenter;

@SuppressWarnings("serial")
public class DashboardPresenter extends Presenter<DashboardView> {

    @Inject
    private DataSource toriDataSource;

    @Override
    public void init() {
        getView().displayCategories(toriDataSource.getAllCategories());
    }

}
