package org.vaadin.tori.dashboard.category;

import org.vaadin.tori.data.DataSource;

import com.github.peholmst.mvp4vaadin.Presenter;

@SuppressWarnings("serial")
public class CategoryPresenter extends Presenter<CategoryView> {

    private final DataSource toriDataSource;

    public CategoryPresenter(final CategoryView view,
            final DataSource toriDataSource) {
        super(view);
        this.toriDataSource = toriDataSource;
    }

    @Override
    public void init() {
        getView().displayCategories(toriDataSource.getAllCategories());
    }

}
