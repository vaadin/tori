package org.vaadin.tori.dashboard;

import java.util.List;

import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.category.CategoryListing;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
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
        layout.addComponent(new HeadingLabel("Pick a Category", HeadingLevel.H2));

        categoryListing = new CategoryListing(Mode.NORMAL);
        layout.addComponent(categoryListing);
    }

    @Override
    public void displayCategories(final List<Category> categories) {
        categoryListing.setCategories(categories, null);
    }

    @Override
    protected DashboardPresenter createPresenter() {
        final ToriUI ui = ToriUI.getCurrent();
        return new DashboardPresenter(ui.getDataSource(),
                ui.getAuthorizationService());
    }

    @Override
    protected void navigationTo(final String[] arguments) {
        // NOP
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

}
