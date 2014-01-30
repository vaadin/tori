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

package org.vaadin.tori.widgetset.client.ui.tracker;

import org.vaadin.tori.component.GoogleAnalyticsTracker;

import com.google.gwt.core.client.GWT;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.VConsole;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings({ "serial", "deprecation" })
@Connect(GoogleAnalyticsTracker.class)
public class GoogleAnalyticsTrackerConnector extends AbstractComponentConnector
        implements Paintable {
    @Override
    public VGoogleAnalyticsTracker getWidget() {
        return (VGoogleAnalyticsTracker) super.getWidget();
    }

    @Override
    protected VGoogleAnalyticsTracker createWidget() {
        return GWT.create(VGoogleAnalyticsTracker.class);
    }

    @Override
    public void updateFromUIDL(final UIDL uidl,
            final ApplicationConnection client) {
        final String trackerId = uidl.getStringAttribute("trackerid");
        final String domainName = uidl.getStringAttribute("domain");
        final boolean ignoreGetParameters = uidl.hasAttribute("ignoreget") ? uidl
                .getBooleanAttribute("ignoreget") : false;
        final boolean allowAnchor = uidl.hasAttribute("allowAnchor") ? uidl
                .getBooleanAttribute("allowAnchor") : false;

        final String[] pageIds;
        if (uidl.hasAttribute("pageids")) {
            pageIds = uidl.getStringArrayAttribute("pageids");
        } else {
            pageIds = new String[] {};
        }

        getWidget().setTrackerId(trackerId);
        getWidget().setDomainName(domainName);
        getWidget().setAllowAnchor(allowAnchor);
        getWidget().setIgnoreGetParameters(ignoreGetParameters);

        for (final String pageId : pageIds) {
            final String res = getWidget().trackPageview(pageId);
            String message = "VGoogleAnalyticsTracker.trackPageview("
                    + trackerId + "," + pageId + "," + domainName + ","
                    + allowAnchor + ") ";
            if (null != res) {
                message += "FAILED: " + res;
            } else {
                message += "SUCCESS.";
            }
            VConsole.log(message);
        }
    }
}
