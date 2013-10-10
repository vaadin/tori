/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
                    sb.append(String.format("<li><a href=\"%s\">%s</a>",
                            getLink(category, ds), getDescription(category)));
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

    private static String getLink(final Category category, final DataSource ds) {
        return ds.getPathRoot() + "#"
                + ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                + category.getId();
    }

}
