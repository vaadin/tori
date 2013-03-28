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

package org.vaadin.tori;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator.ViewChangeListener;
import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.GoogleAnalyticsTracker;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.DataSource.UrlInfo;
import org.vaadin.tori.data.DataSource.UrlInfo.Destination;
import org.vaadin.tori.edit.EditViewImpl;
import org.vaadin.tori.mvp.View;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.SignatureFormatter;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinPortletRequest;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
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

    private String contextPath;

    @Override
    protected void init(final VaadinRequest request) {

        contextPath = request.getContextPath();
        fixUrl(contextPath);

        getPage().setTitle("Tori");
        navigator = new ToriNavigator(this);

        windowLayout = new VerticalLayout();
        windowLayout.setMargin(false);
        setContent(windowLayout);

        final String trackerId = getDataSource().getGoogleAnalyticsTrackerId();
        if (trackerId != null) {
            analytics = new GoogleAnalyticsTracker(trackerId);
            analytics.setAllowAnchor(true);
            analytics.setIgnoreGetParameters(true);
            windowLayout.addComponent(analytics);
        } else {
            analytics = null;
        }

        final Breadcrumbs breadcrumbs = new Breadcrumbs(navigator);
        addControlPanelIfInDevelopment();
        windowLayout.addComponent(breadcrumbs);
        windowLayout.addComponent(navigator);

        navigator.init(getPage().getUriFragment());

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

        if (request instanceof VaadinPortletRequest) {
            final VaadinPortletRequest r = (VaadinPortletRequest) request;
            setPortletMode(r.getPortletRequest().getPortletMode());
        }
    }

    void initApiLoader(final VaadinRequest request) {
        getSession().setAttribute(ToriApiLoader.class, new ToriApiLoader());

        /*
         * this hack is specially reserved for Liferay. Maybe the servlet
         * request can be put in here too...
         */
        if (request instanceof VaadinPortletRequest) {
            final VaadinPortletRequest vpRequest = (VaadinPortletRequest) request;
            setRequest(vpRequest.getPortletRequest());
        }
    }

    public final void setPortletMode(final PortletMode portletMode) {
        if (portletMode == PortletMode.EDIT) {
            final EditViewImpl editView = new EditViewImpl(getDataSource(),
                    getAuthorizationService());
            editView.init(null);
            setContent(editView);
        } else {
            setContent(windowLayout);
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriUI.class);
    }

    private void addControlPanelIfInDevelopment() {
        final AuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService instanceof DebugAuthorizationService) {
            windowLayout
                    .addComponent(new DebugControlPanel(
                            (DebugAuthorizationService) authorizationService,
                            navigator));
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
        final VaadinSession session = getSession();
        final ToriApiLoader apiLoader = session
                .getAttribute(ToriApiLoader.class);
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

    void setRequest(final PortletRequest request) {
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

    @SuppressWarnings("deprecation")
    private void fixUrl(final String contextPath) {
        final URI uri = getPage().getLocation();
        final String path = uri.getPath();
        final String pathRoot = getDataSource().getPathRoot();

        if (pathRoot == null) {
            getLogger().info(
                    "Tori's path root is undefined. You probably "
                            + "should set it. Skipping URL parsing.");
            return;
        }

        if (hasInvalidPath(path, pathRoot)) {
            getLogger().warn(
                    "Path mismatch: Tori is configured for the path "
                            + pathRoot + ", but current path is " + path
                            + ". Skipping URL parsing.");
            return;
        }

        final String pathPart;
        if (path.length() > pathRoot.length() && !path.equals(pathRoot + "/")) {
            pathPart = pathRoot;
        } else {
            pathPart = null;
        }

        String fragment = null;
        final String relativePath = path.substring(pathRoot.length());
        try {
            final String query = getQueryString(uri);
            final UrlInfo toriFragment = getDataSource().getToriFragment(
                    relativePath, query);

            if (toriFragment != null) {
                fragment = "";

                if (toriFragment.getDestination() == Destination.DASHBOARD) {
                    fragment = ToriNavigator.ApplicationView.DASHBOARD.getUrl()
                            + "/";
                } else {
                    switch (toriFragment.getDestination()) {
                    case CATEGORY:
                        fragment = ToriNavigator.ApplicationView.CATEGORIES
                                .getUrl();
                        break;
                    case THREAD:
                        fragment = ToriNavigator.ApplicationView.THREADS
                                .getUrl();
                        break;
                    default:
                        Notification.show("You found a URL the coders "
                                + "didn't remember to handle correctly. "
                                + "Terribly sorry about that.",
                                Notification.Type.HUMANIZED_MESSAGE);
                        throw new UnsupportedOperationException("Support for "
                                + toriFragment.getDestination()
                                + " is not implemented");
                    }

                    fragment += "/" + toriFragment.getId();
                }
            }
        } catch (final UnsupportedEncodingException e) {
            logger().error("URI is not in UTF-8 format");
        }

        if (pathPart != null && fragment != null) {
            getPage().setLocation(pathPart + "#" + fragment);
        } else if (pathPart != null) {
            getPage().setLocation(uri);
        } else if (fragment != null) {
            getPage().setUriFragment(fragment);
        }
    }

    /**
     * Get the decoded query part ("GET parameters") of the URI. Returns empty
     * string if <code>null</code>
     */
    private static String getQueryString(final URI uri)
            throws UnsupportedEncodingException {
        final String query;
        if (uri.getQuery() != null) {
            query = URLDecoder.decode(uri.getQuery(), "UTF-8");
        } else {
            query = "";
        }
        return query;
    }

    private boolean hasInvalidPath(final String path, final String pathRoot) {
        final boolean pathStartsWithRoot = path.startsWith(pathRoot);

        // to avoid pathroot being "/foo" and path being "/foobar"
        final boolean pathIsCorrectFormat;
        if (path.length() > pathRoot.length()) {
            pathIsCorrectFormat = path.startsWith(pathRoot + "/");
        } else {
            pathIsCorrectFormat = true;
        }

        return !(pathStartsWithRoot && pathIsCorrectFormat);
    }
}
