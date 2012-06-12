package org.vaadin.tori;

import javax.portlet.PortletMode;

import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.GoogleAnalyticsTracker;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.edit.EditViewImpl;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.ui.Root;
import com.vaadin.ui.VerticalLayout;

import edu.umd.cs.findbugs.annotations.CheckForNull;

@Theme("tori")
@SuppressWarnings("serial")
@Widgetset("org.vaadin.tori.widgetset.ToriWidgetset")
public class ToriRoot extends Root {

    private static final int KEEPALIVE_PING_INTERVAL_MILLIS = 60000;

    private final ToriNavigator navigator = new ToriNavigator(this);
    private VerticalLayout windowLayout;

    @CheckForNull
    private GoogleAnalyticsTracker analytics;

    @Override
    protected void init(final WrappedRequest request) {
        setCaption("Tori");
        windowLayout = new VerticalLayout();
        windowLayout.setMargin(false);
        setContent(windowLayout);

        final String trackerId = ToriApplication.getCurrent().getDataSource()
                .getGoogleAnalyticsTrackerId();
        if (trackerId != null) {
            analytics = new GoogleAnalyticsTracker(trackerId);
            analytics.setAllowAnchor(true);
            analytics.setIgnoreGetParameters(true);
            addComponent(analytics);
        } else {
            analytics = null;
        }

        final Breadcrumbs breadcrumbs = new Breadcrumbs(navigator);
        addControlPanelIfInDevelopment();
        addComponent(breadcrumbs);
        addComponent(navigator);

        navigator.init(request.getBrowserDetails().getUriFragment());

        navigator.addListener(new ViewChangeListener() {
            @Override
            public void navigatorViewChange(final View previous,
                    final View current) {
                // scroll to top when the view is changed
                if (getContent() == windowLayout) {
                    scrollIntoView(breadcrumbs);
                }
            }
        });

        /*
         * refresher to stop the session from expiring. SessionGuard would be
         * better, but it doesn't work in portlets
         */
        /*-
        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(KEEPALIVE_PING_INTERVAL_MILLIS);
        addComponent(refresher);
         */
    }

    public final void setPortletMode(final PortletMode portletMode) {
        if (portletMode == PortletMode.EDIT) {
            if (getContent() == windowLayout) {
                final EditViewImpl editView = new EditViewImpl();
                editView.init(null, getApplication());
                setContent(editView);
            }
        } else {
            if (getContent() != windowLayout && windowLayout != null) {
                setContent(windowLayout);
            }
        }
    }

    private void addControlPanelIfInDevelopment() {
        final AuthorizationService authorizationService = ToriApplication
                .getCurrent().getAuthorizationService();
        if (authorizationService instanceof DebugAuthorizationService) {
            addComponent(new DebugControlPanel(
                    (DebugAuthorizationService) authorizationService, navigator));
        }
    }

    public void trackAction(final String action) {
        if (analytics != null) {
            analytics.trackPageview(action);
        }
    }

}
