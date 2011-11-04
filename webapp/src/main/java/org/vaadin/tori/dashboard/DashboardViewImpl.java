package org.vaadin.tori.dashboard;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DashboardViewImpl extends
        AbstractView<DashboardView, DashboardPresenter> implements
        DashboardView {

    private VerticalLayout layout;
    private CategoryListing categoryListing;

    @Override
    protected Component createCompositionRoot() {
        layout = new VerticalLayout();
        return layout;
    }

    @Override
    public void initView() {
        layout.addComponent(new Label("Pick a Category"));

        categoryListing = new CategoryListing();
        layout.addComponent(categoryListing);
    }

    @Override
    public void displayCategories(final List<Category> categories) {
        categoryListing.setCategories(categories);
    }

    @Override
    protected DashboardPresenter createPresenter() {
        return new DashboardPresenter(ToriApplication.getCurrent()
                .getDataSource());
    }

}
