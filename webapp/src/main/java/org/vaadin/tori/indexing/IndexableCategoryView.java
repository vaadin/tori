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

import org.vaadin.tori.ToriNavigator.ApplicationView;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

public class IndexableCategoryView extends IndexableView {

    public IndexableCategoryView(final List<String> arguments,
            final ToriIndexableApplication application) {
        super(arguments, application);
    }

    @Override
    public String getHtml() {

        if (arguments.isEmpty()) {
            return "No category given";
        }

        try {
            final long categoryId = Long.parseLong(arguments.get(0));
            final Category category = application.getDataSource().getCategory(
                    categoryId);

            if (category == null) {
                return "No such category";
            }

            final StringBuilder sb = new StringBuilder();
            sb.append(getParentCategoryLink(category));
            sb.append("<h1>" + category.getName() + "</h1>");
            sb.append(IndexableDashboardView.getCategoriesXhtml(
                    application.getDataSource(), getLogger(), category));
            sb.append(getThreadsXhtml(category));
            return sb.toString();

        } catch (final NumberFormatException e) {
            getLogger().error(e);
            return "The category id is not properly formatted";
        } catch (final DataSourceException e) {
            getLogger().error(e);
            return "There was a database error when trying to fetch the category";
        }
    }

    private String getParentCategoryLink(final Category category) {
        final Category parentCategory = category.getParentCategory();
        if (parentCategory != null) {
            return String.format("<a href=\"#%s\">Parent category</a>",
                    parentCategory.getId());
        } else {
            return String.format("<a href=\"#%s\">Dashboard</a>",
                    ApplicationView.DASHBOARD.getUrl());
        }
    }

    private String getThreadsXhtml(final Category category) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<h2>Threads</h2>");

        try {
            final List<DiscussionThread> threads = application.getDataSource()
                    .getThreads(category);
            if (!threads.isEmpty()) {
                sb.append("<table>");
                sb.append("<tr><th>Thread</th><th>Author</th><th>Posts</th></tr>");
                for (final DiscussionThread thread : threads) {
                    sb.append(getThreadRow(thread));
                }
                sb.append("</table>");
            } else {
                sb.append("There are no threads in this category");
            }
        } catch (final DataSourceException e) {
            getLogger().error(e);
            sb.append("There was a problem when trying to fetch threads");
        }

        return sb.toString();
    }

    private String getThreadRow(final DiscussionThread thread) {
        final String threadUrl = getThreadUrl(thread);
        final String topic = ToriUtil.escapeXhtml(thread.getTopic());
        final String authorName = ToriUtil.escapeXhtml(thread
                .getOriginalPoster().getDisplayedName());
        final int postCount = thread.getPostCount();
        return String
                .format("<tr><td><a href=\"#%s\">%s</a></td><td>%s</td><td>%s</td></tr>",
                        threadUrl, topic, authorName, postCount);
    }

    private String getThreadUrl(final DiscussionThread thread) {
        return ApplicationView.THREADS.getUrl() + "/" + thread.getId();
    }

}
