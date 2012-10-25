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

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;

public class VGoogleAnalyticsTracker extends Widget {

    private static final int POLLING_INTERVAL_MILLIS = 1000;
    private static final int GIVE_UP_AFTER_MILLIS = 10000;
    private final JsArrayString QUEUE;

    private String trackerId;
    private String domainName;
    private boolean allowAnchor;
    private boolean ignoreGetParameters;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VGoogleAnalyticsTracker() {
        QUEUE = getEmptyArray();
        setElement(Document.get().createDivElement());
        loadGoogleJavascript(RootPanel.getBodyElement());
    }

    private native JsArrayString getEmptyArray()
    /*-{
       return [];
    }-*/;

    private native void loadGoogleJavascript(Element element)
    /*-{
      if (!!($wnd._gat)) {
          return;
      }

      var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
      var gaJsURL = gaJsHost + 'google-analytics.com/ga.js';
      
      var googleScriptTag = $doc.createElement('script');
      googleScriptTag.type = 'text/javascript';
      googleScriptTag.src = gaJsURL;
      
      element.appendChild(googleScriptTag);
      
      this.@org.vaadin.tori.widgetset.client.ui.tracker.VGoogleAnalyticsTracker::pollForLodadedScript()();
    }-*/;

    private void pollForLodadedScript() {
        final long startTime = System.currentTimeMillis();
        new Timer() {
            @Override
            public void run() {
                if (trackerIsLoaded()) {
                    VConsole.log("Sending " + QUEUE.length()
                            + " pageviews to google analytics");
                    while (QUEUE.length() > 0) {
                        _trackPageview(QUEUE.shift());
                    }
                } else if ((System.currentTimeMillis() - startTime) < GIVE_UP_AFTER_MILLIS) {
                    schedule(POLLING_INTERVAL_MILLIS);
                } else {
                    VConsole.error("Google Analytics Tracker wasn't loaded in "
                            + GIVE_UP_AFTER_MILLIS + "ms.");
                    if (QUEUE.length() > 0) {
                        VConsole.error("Discarding analytics queue of "
                                + QUEUE.length() + " entries.");
                    }
                }
            }
        }.schedule(POLLING_INTERVAL_MILLIS);
    }

    private native boolean trackerIsLoaded()
    /*-{
        return !!($wnd._gat);
    }-*/;

    public String trackPageview(final String pageId) {
        if (trackerIsLoaded()) {
            return _trackPageview(pageId);
        } else {
            QUEUE.push(pageId);
            return "not ready, put into queue";
        }
    }

    private String _trackPageview(final String pageId) {
        return _trackPageview(trackerId, pageId, domainName, allowAnchor,
                ignoreGetParameters);
    }

    /**
     * Native JS call to invoke _trackPageview from ga.js.
     * 
     * @param trackerId
     * @param pageId
     * @param domainName
     * @param allowAnchors
     * @return
     */
    private native String _trackPageview(String trackerId, String pageId,
            String domainName, boolean allowAnchor, boolean ignoreGetParams)
    /*-{
        if (!$wnd._gat) {
            return "Tracker not found (running offline?)";
        }
        try {
            var pageTracker = $wnd._gat._getTracker(trackerId);
            if (!pageTracker) {
                return "Failed to get tracker for "+trackerId;
            }

            if (domainName) {
                pageTracker._setDomainName(domainName);
            }

            pageTracker._setAllowAnchor(allowAnchor);

            if (pageId) {
                var location = "";
                if (pageId.indexOf('#') === 0) {
                    location = window.location.pathname; 
                    if (!ignoreGetParams) {
                        location += window.location.search;
                    }
                    location += pageId;
                } else {
                    location = pageId;
                }
                pageTracker._trackPageview(location);
            } else {
                if (ignoreGetParams) {
                    pageTracker._trackPageview(window.location.pathname + window.location.search);
                } else {
                    pageTracker._trackPageview();
                }
            }
            return null;
        } catch(err) {
            return ""+err;
        }
    }-*/;

    public void setTrackerId(final String trackerId) {
        this.trackerId = trackerId;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setAllowAnchor(final boolean allowAnchor) {
        this.allowAnchor = allowAnchor;
    }

    public void setIgnoreGetParameters(final boolean ignoreGetParameters) {
        this.ignoreGetParameters = ignoreGetParameters;
    }

}
