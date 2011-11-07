package org.vaadin.tori.component;

import java.util.List;

import org.vaadin.tori.ToriNavigator;
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

        int i = 0;
        for (final Category category : categories) {
            final String categoryUrl = ToriNavigator.ApplicationView.CATEGORIES
                    .getUrl();
            final long id = category.getId();
            final String name = category.getName();

            layout.addComponent(new Label(String
                    .format("<h3><a href=\"#%s/%s\">%s</a></h3>", categoryUrl,
                            id, name), Label.CONTENT_XHTML));
            layout.addComponent(new Label(category.getDescription()));
            i++;
        }
    }
}
