package org.vaadin.tori.indexing;

import java.util.List;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;

public class IndexableDashboardView extends IndexableView {

    public IndexableDashboardView(final List<String> arguments,
            final DataSource ds) {
        super(arguments, ds);
    }

    @Override
    public String getXhtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<h1>Categories</h1>");
        try {
            sb.append("<ul>");
            for (final Category category : ds.getRootCategories()) {
                sb.append(String.format("<li><a href=\"#%s\">%s</a>",
                        getLink(category), getDescription(category)));
            }
            sb.append("</ul>");
        } catch (final DataSourceException e) {
            getLogger().error(e);
            sb.append("There was an error when trying to fetch categories.");
        }
        return sb.toString();
    }

    private String getDescription(final Category category) {
        return category.getName() + "<br>" + category.getDescription();
    }

    private String getLink(final Category category) {
        return ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                + category.getId();
    }

}
