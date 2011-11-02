package org.vaadin.tori.dashboard;

import java.util.List;

import org.vaadin.tori.data.TestDataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.view.AbstractToriView;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DashboardViewImpl extends
        AbstractToriView<DashboardView, DashboardPresenter> implements
        DashboardView {

    private VerticalLayout layout;
    private CategoryListing categoryListing;

    @Override
    public DashboardPresenter createPresenter() {
        // TODO better way to inject the correct DataSource implementation
        return new DashboardPresenter(this, new TestDataSource());
    }

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

}
