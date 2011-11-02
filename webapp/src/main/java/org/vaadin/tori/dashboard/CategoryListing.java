package org.vaadin.tori.dashboard;

import java.util.List;

import org.vaadin.tori.data.entity.Category;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CategoryListing extends CustomComponent {

    private final VerticalLayout layout;

    public CategoryListing() {
        layout = new VerticalLayout();
        setCompositionRoot(layout);
    }

    public void setCategories(final List<Category> categories) {
        layout.removeAllComponents();
        for (final Category category : categories) {
            layout.addComponent(new Label(
                    "<h3>" + category.getName() + "</h3>", Label.CONTENT_XHTML));
            layout.addComponent(new Label(category.getDescription()));
        }
    }

}
