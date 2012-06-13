package org.vaadin.tori.widgetset.client.ui.tracker;

import org.vaadin.tori.component.GoogleAnalyticsTracker;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.AbstractComponentConnector;
import com.vaadin.terminal.gwt.client.ui.Connect;

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
        return (VGoogleAnalyticsTracker) super.createWidget();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "non-issue, readibility")
    public void updateFromUIDL(final UIDL uidl,
            final ApplicationConnection client) {
        final String trackerId = uidl.getStringAttribute("trackerid");
        final String[] pageIds = uidl.getStringArrayAttribute("pageids");
        final String domainName = uidl.getStringAttribute("domain");
        final boolean ignoreGetParameters = uidl.hasAttribute("ignoreget") ? uidl
                .getBooleanAttribute("ignoreget") : false;
        final boolean allowAnchor = uidl.hasAttribute("allowAnchor") ? uidl
                .getBooleanAttribute("allowAnchor") : false;

        VGoogleAnalyticsTracker.setTrackerId(trackerId);
        VGoogleAnalyticsTracker.setDomainName(domainName);
        VGoogleAnalyticsTracker.setAllowAnchor(allowAnchor);
        VGoogleAnalyticsTracker.setIgnoreGetParameters(ignoreGetParameters);

        for (final String pageId : pageIds) {
            final String res = VGoogleAnalyticsTracker.trackPageview(pageId);
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
