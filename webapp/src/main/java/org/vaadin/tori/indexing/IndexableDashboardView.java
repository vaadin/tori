package org.vaadin.tori.indexing;

import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;

public class IndexableDashboardView extends IndexableView {

    public IndexableDashboardView(final List<String> arguments,
            final ToriIndexableApplication application) {
        super(arguments, application);
    }

    @Override
    public String getHtml() {
        return "<h1>Forum</h1>"
                + getCategoriesXhtml(application.getDataSource(), getLogger(),
                        null);
    }

    public static String getCategoriesXhtml(final DataSource ds,
            final Logger logger, final Category currentCategory) {
        final StringBuilder sb = new StringBuilder();
        try {
            final List<Category> subCategories = ds
                    .getSubCategories(currentCategory);

            if (!subCategories.isEmpty()) {
                sb.append("<h2>Categories</h2>");
                sb.append("<ul>");
                for (final Category category : subCategories) {
                    sb.append(String.format("<li><a href=\"#%s\">%s</a>",
                            getLink(category), getDescription(category)));
                }
                sb.append("</ul>");
            }
        } catch (final DataSourceException e) {
            logger.error(e);
            sb.append("There was an error when trying to fetch categories.");
        }
        return sb.toString();
    }

    private static String getDescription(final Category category) {
        return ToriUtil.escapeXhtml(category.getName()) + "<br>"
                + ToriUtil.escapeXhtml(category.getDescription());
    }

    private static String getLink(final Category category) {
        return ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                + category.getId();
    }

}
