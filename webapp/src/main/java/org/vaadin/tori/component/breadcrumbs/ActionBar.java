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

package org.vaadin.tori.component.breadcrumbs;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.category.SpecialCategory;
import org.vaadin.tori.data.DataSource;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;

/**
 * ActionBar displays additional controls on the header bar (which also contains
 * the breadcrumbs).
 */
@SuppressWarnings("serial")
public class ActionBar extends CustomComponent {

    public ActionBar() {
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        setStyleName("action-bar");
        setCompositionRoot(layout);
        setWidth(null);

        layout.addComponent(getLink(SpecialCategory.RECENT_POSTS));
        final DataSource ds = ToriUI.getCurrent().getDataSource();
        if (ds.isLoggedInUser()) {
            layout.addComponent(getLink(SpecialCategory.MY_POSTS));
        }
    }

    private Link getLink(final SpecialCategory category) {
        final Link recentLink = new Link(category.getInstance().getName(),
                new ExternalResource("#"
                        + ToriNavigator.ApplicationView.CATEGORIES.getUrl()
                        + "/" + category.getId()));
        recentLink.setIcon(new ThemeResource("images/icon-" + category.getId()
                + ".png"));
        return recentLink;
    }

}
