package org.vaadin.tori.widgetset.client.ui.tracker;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class VGoogleAnalyticsTracker extends Widget {

    private static final int POLLING_INTERVAL_MILLIS = 1000;
    private static final int GIVE_UP_AFTER_MILLIS = 10000;
    private static final JsArrayString QUEUE = (JsArrayString) JsArrayString
            .createArray();

    private static String trackerId;
    private static String domainName;
    private static boolean allowAnchor;
    private static boolean ignoreGetParameters;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VGoogleAnalyticsTracker() {
        setElement(Document.get().createDivElement());
        loadGoogleJavascript(RootPanel.getBodyElement());
    }

    private static native void loadGoogleJavascript(Element element)
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
      
      @org.vaadin.tori.widgetset.client.ui.tracker.VGoogleAnalyticsTracker::pollForLodadedScript()();
    }-*/;

    private static void pollForLodadedScript() {
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
                            + GIVE_UP_AFTER_MILLIS + "ms. Giving up.");
                }
            }
        }.schedule(POLLING_INTERVAL_MILLIS);
    }

    private static native boolean trackerIsLoaded()
    /*-{
        return !!($wnd._gat);
    }-*/;

    public static String trackPageview(final String pageId) {
        if (trackerIsLoaded()) {
            return _trackPageview(pageId);
        } else {
            QUEUE.push(pageId);
            return "not ready, put into queue";
        }
    }

    private static String _trackPageview(final String pageId) {
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
    private native static String _trackPageview(String trackerId,
            String pageId, String domainName, boolean allowAnchor,
            boolean ignoreGetParams)
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

    public static void setTrackerId(final String trackerId) {
        VGoogleAnalyticsTracker.trackerId = trackerId;
    }

    public static void setDomainName(final String domainName) {
        VGoogleAnalyticsTracker.domainName = domainName;
    }

    public static void setAllowAnchor(final boolean allowAnchor) {
        VGoogleAnalyticsTracker.allowAnchor = allowAnchor;
    }

    public static void setIgnoreGetParameters(final boolean ignoreGetParameters) {
        VGoogleAnalyticsTracker.ignoreGetParameters = ignoreGetParameters;
    }

}
