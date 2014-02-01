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

import javax.portlet.PortletMode;

import org.apache.log4j.Logger;
import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;
import org.vaadin.tori.component.DebugControlPanel;
import org.vaadin.tori.component.breadcrumbs.Breadcrumbs;
import org.vaadin.tori.edit.EditViewImpl;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.DebugAuthorizationService;

import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinPortletRequest;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Widgetset("org.vaadin.tori.widgetset.ToriWidgetset")
public class ToriUI extends UI {

    public static final int DEFAULT_POLL_INTERVAL = 1000 * 10;

    private ToriNavigator navigator;
    private VerticalLayout mainLayout;

    private GoogleAnalyticsTracker analytics;

    @Override
    protected void init(final VaadinRequest request) {
        setPollInterval(DEFAULT_POLL_INTERVAL);
        ToriApiLoader.init(request);
        URLUtil.fixUrl();

        final String trackerId = ToriApiLoader.getCurrent().getDataSource()
                .getGoogleAnalyticsTrackerId();
        if (trackerId != null) {
            analytics = new GoogleAnalyticsTracker(trackerId);
            analytics.setAllowAnchor(true);
            analytics.extend(this);
        }

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        setContent(mainLayout);

        VerticalLayout navigatorContent = new VerticalLayout();
        navigator = new ToriNavigator(this, navigatorContent);
        final Breadcrumbs breadcrumbs = new Breadcrumbs(navigator);

        addControlPanelIfInDevelopment();
        mainLayout.addComponent(breadcrumbs);
        mainLayout.addComponent(navigatorContent);

        if (request instanceof VaadinPortletRequest) {
            final VaadinPortletRequest r = (VaadinPortletRequest) request;
            setPortletMode(r.getPortletRequest().getPortletMode());
        }
    }

    public final void setPortletMode(final PortletMode portletMode) {
        if (portletMode == PortletMode.EDIT) {
            final EditViewImpl editView = new EditViewImpl(ToriApiLoader
                    .getCurrent().getDataSource(), ToriApiLoader.getCurrent()
                    .getAuthorizationService());
            editView.init();
            setContent(editView);
        } else {
            setContent(mainLayout);
        }
    }

    private void addControlPanelIfInDevelopment() {
        final AuthorizationService authorizationService = ToriApiLoader
                .getCurrent().getAuthorizationService();
        if (authorizationService instanceof DebugAuthorizationService) {
            mainLayout
                    .addComponent(new DebugControlPanel(
                            (DebugAuthorizationService) authorizationService,
                            navigator));
        }
    }

    /**
     * Send data to Google Analytics about what the user is doing.
     * 
     * @param action
     *            the action performed in the path. <code>null</code> to ignore.
     *            E.g. "reply"
     */
    public void trackAction(final String action) {
        if (analytics != null) {
            String fragment = Page.getCurrent().getUriFragment();
            StringBuilder sb = new StringBuilder("#");
            sb.append(fragment != null ? fragment : "");
            if (action != null) {
                sb.append("/" + action);
            }
            analytics.trackPageview(sb.toString());
        } else {
            getLogger()
                    .debug("Can't track an action - no analytics configured");
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriUI.class);
    }

    public static ToriUI getCurrent() {
        return (ToriUI) UI.getCurrent();
    }

}
