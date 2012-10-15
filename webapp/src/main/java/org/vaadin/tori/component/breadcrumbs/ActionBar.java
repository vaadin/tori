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
