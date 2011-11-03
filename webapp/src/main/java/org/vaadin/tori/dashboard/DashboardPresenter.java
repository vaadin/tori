package org.vaadin.tori.dashboard;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.DataSource;

import com.github.peholmst.mvp4vaadin.Presenter;

@SuppressWarnings("serial")
public class DashboardPresenter extends Presenter<DashboardView> {

    @Override
    public void init() {
        final DataSource ds = ToriApplication.getCurrent().getDataSource();
        getView().displayCategories(ds.getAllCategories());
    }

}
