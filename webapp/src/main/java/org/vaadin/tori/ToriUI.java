package org.vaadin.tori;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.GoogleAnalyticsTracker;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.edit.EditViewImpl;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.WrappedRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import edu.umd.cs.findbugs.annotations.CheckForNull;

@Theme("tori")
@SuppressWarnings("serial")
@Widgetset("org.vaadin.tori.widgetset.ToriWidgetset")
public class ToriUI extends UI {

    private static final String PATH_ACTION_SEPARATOR = "$";

    private ToriNavigator navigator;
    private VerticalLayout windowLayout;

    @CheckForNull
    private GoogleAnalyticsTracker analytics;

    private String lastPath = "";

    @Override
    protected void init(final WrappedRequest request) {
        getPage().setTitle("Tori");
        getSession().setAttribute(ToriApiLoader.class, new ToriApiLoader());
        navigator = new ToriNavigator(this);

        windowLayout = new VerticalLayout();
        windowLayout.setMargin(false);
        setContent(windowLayout);

        final String trackerId = getDataSource().getGoogleAnalyticsTrackerId();
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

        navigator.init(getPage().getFragment());

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
                editView.init(null);
                setContent(editView);
            }
        } else {
            if (getContent() != windowLayout && windowLayout != null) {
                setContent(windowLayout);
            }
        }
    }

    private void addControlPanelIfInDevelopment() {
        final AuthorizationService authorizationService = getAuthorizationService();
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

    public AuthorizationService getAuthorizationService() {
        return getApiLoader().getAuthorizationService();
    }

    public PostFormatter getPostFormatter() {
        return getApiLoader().getPostFormatter();
    }

    public SignatureFormatter getSignatureFormatter() {
        return getApiLoader().getSignatureFormatter();
    }

    public DataSource getDataSource() {
        return getApiLoader().getDs();
    }

    private ToriApiLoader getApiLoader() {
        final ToriApiLoader apiLoader = getSession().getAttribute(
                ToriApiLoader.class);
        if (apiLoader != null) {
            return apiLoader;
        } else {
            throw new IllegalStateException(ToriApiLoader.class.getName()
                    + " was not found in the state. This is bad...");
        }
    }

    void setRequest(final HttpServletRequest request) {
        getApiLoader().setRequest(request);
    }

    /*-
     * Not sure if we need this code anymore... it's quite hacky anyways.
    private void addAttachmentDownloadHandler() {
        addRequestHandler(new RequestHandler() {
            @Override
            public boolean handleRequest(final Application application,
                    final WrappedRequest request, final WrappedResponse response)
                    throws IOException {

                final String requestPathInfo = request.getRequestPathInfo();
                if (requestPathInfo
                        .startsWith(DebugDataSource.ATTACHMENT_PREFIX)) {
                    final String[] data = requestPathInfo.substring(
                            DebugDataSource.ATTACHMENT_PREFIX.length()).split(
                            "/");
                    final long dataId = Long.parseLong(data[0]);
                    final String fileName = data[1];

                    final byte[] attachmentData = ((DebugDataSource) ToriApplication
                            .getCurrent().getDataSource())
                            .getAttachmentData(dataId);

                    final ByteArrayInputStream is = new ByteArrayInputStream(
                            attachmentData);
                    final DownloadStream stream = new DownloadStream(is, null,
                            fileName);
                    stream.writeTo(response);
                    return true;
                }
                return false;
            }
        });
    }
     */

    public static ToriUI getCurrent() {
        return (ToriUI) UI.getCurrent();
    }

}
