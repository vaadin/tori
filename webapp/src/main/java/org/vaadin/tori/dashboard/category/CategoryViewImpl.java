package org.vaadin.tori.dashboard.category;

import java.util.List;

import org.vaadin.tori.data.TestDataSource;
import org.vaadin.tori.data.entity.Category;

import com.github.peholmst.mvp4vaadin.AbstractViewComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CategoryViewImpl extends
        AbstractViewComponent<CategoryView, CategoryPresenter> implements
        CategoryView {

    private VerticalLayout layout;

    public CategoryViewImpl() {
        init();
    }

    @Override
    public CategoryPresenter createPresenter() {
        // TODO better way to inject the correct DataSource implementation
        return new CategoryPresenter(this, new TestDataSource());
    }

    @Override
    protected Component createCompositionRoot() {
        layout = new VerticalLayout();
        return layout;
    }

    @Override
    public void initView() {
        layout.addComponent(new Label("Pick a Category"));
    }

    @Override
    public void displayCategories(final List<Category> categories) {
        for (final Category category : categories) {
            layout.addComponent(new Label(
                    "<h3>" + category.getName() + "</h3>", Label.CONTENT_XHTML));
            layout.addComponent(new Label(category.getDescription()));
        }
    }

}
