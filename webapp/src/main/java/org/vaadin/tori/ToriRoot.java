package org.vaadin.tori;

import javax.portlet.PortletMode;

import org.apache.log4j.Logger;
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

    private static final String PATH_ACTION_SEPARATOR = "$";

    private final ToriNavigator navigator = new ToriNavigator(this);
    private VerticalLayout windowLayout;

    @CheckForNull
    private GoogleAnalyticsTracker analytics;

    private String lastPath = "";

    @Override
    protected void init(final WrappedRequest request) {
        getPage().setTitle("Tori");
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

    /**
     * Send data to Google Analytics about what the user is doing.
     * 
     * @param path
     *            the current logical path to the view. <code>null</code> to use
     *            the last known path. E.g. "#/foo/bar"
     * @param action
     *            the action performed in the path. <code>null</code> to ignore.
     *            E.g. "reply"
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NULL_ON_SOME_PATH", justification = "false positive")
    public void trackAction(final String path, final String action) {
        if (analytics != null) {

            final String pageString;
            if (path != null && action != null) {
                pageString = path + PATH_ACTION_SEPARATOR + action;
            } else if (action != null) {
                pageString = lastPath + PATH_ACTION_SEPARATOR + action;
            } else if (path != null) {
                pageString = path;
            } else {
                pageString = null;
                logger().warn(
                        "tracking an action with null path and "
                                + "null action");
            }

            if (path != null) {
                lastPath = path;
            }

            analytics.trackPageview(pageString);
        } else {
            logger().debug("Can't track an action - no analytics configured");
        }
    }

    private Logger logger() {
        return Logger.getLogger(getClass());
    }
}
