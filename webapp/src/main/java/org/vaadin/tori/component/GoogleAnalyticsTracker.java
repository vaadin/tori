package org.vaadin.tori.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Vaadin6Component;
import com.vaadin.ui.AbstractComponent;

/**
 * Component for triggering Google Analytics page views. Usage:
 * 
 * <pre>
 * GoogleAnalyticsTracker tracker = new GoogleAnalyticsTracker("UA-658457-8", "vaadin.com");
 * window.addComponent(tracker);
 *   ....
 * tracker.trackPageview("/samplecode/googleanalytics");
 * </pre>
 * 
 * @author Sami Ekblad / Marc Englund / IT Mill
 * 
 */
public class GoogleAnalyticsTracker extends AbstractComponent implements
        Vaadin6Component {

    private static final long serialVersionUID = 2973391903850855532L;

    private final String trackerId;
    private final List<String> pageIds = new ArrayList<String>();
    private String domainName;
    private boolean allowAnchor;

    private boolean ignoreGetParams;

    /**
     * Instantiate new Google Analytics tracker by id.
     * 
     * @param trackerId
     *            The tracking id from Google Analytics. Something like
     *            'UA-658457-8'.
     */
    public GoogleAnalyticsTracker(final String trackerId) {
        this.trackerId = trackerId;
    }

    /**
     * Instantiate new Google Analytics tracker by id and domain.
     * 
     * @param trackerId
     *            The tracking id from Google Analytics. Something like
     *            'UA-658457-8'.
     * @param domainName
     *            The name of the domain to be tracked. Something like
     *            'vaadin.com'.
     */

    public GoogleAnalyticsTracker(final String trackerId,
            final String domainName) {
        this(trackerId);
        this.domainName = domainName;
    }

    /**
     * Get the Google Analytics tracking id.
     * 
     * @return Tracking id like 'UA-658457-8'.
     */
    public String getTrackerId() {
        return trackerId;
    }

    /**
     * Get the domain name associated with this tracker.
     * 
     * @return
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Track a single page view. This effectively invokes the 'trackPageview' in
     * ga.js file.
     * 
     * Note that when ever the component is repainted (for example during
     * explicit page reload), a new track event is generated.
     * 
     * @param pageId
     *            The page id. Use a scheme like '/topic/page' or
     *            '/view/action'.
     */
    public void trackPageview(final String pageId) {
        this.pageIds.add(pageId);
        requestRepaint();
    }

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        target.addAttribute("trackerid", trackerId);
        target.addAttribute("allowAnchor", allowAnchor);
        target.addAttribute("ignoreget", ignoreGetParams);
        if (!pageIds.isEmpty()) {
            target.addAttribute("pageids",
                    pageIds.toArray(new String[pageIds.size()]));
            pageIds.clear();
        }
        if (domainName != null) {
            target.addAttribute("domain", domainName);
        }
    }

    /**
     * Allow anchors in tracked URLs. As specified in <a href=
     * "http://code.google.com/apis/analytics/docs/gaJS/gaJSApiCampaignTracking.html#_gat.GA_Tracker_._setAllowAnchor"
     * >specs</a>
     * 
     * @param allowAnchor
     */
    public void setAllowAnchor(final boolean allowAnchor) {
        this.allowAnchor = allowAnchor;
    }

    /**
     * Allow anchors in tracked URLs. As specified in
     * http://code.google.com/apis
     * /analytics/docs/gaJS/gaJSApiCampaignTracking.html
     * #_gat.GA_Tracker_._setAllowAnchor
     * 
     */
    public boolean isAllowAnchor() {
        return allowAnchor;
    }

    @Override
    public void changeVariables(final Object source,
            final Map<String, Object> variables) {
    }

    public void setIgnoreGetParameters(final boolean b) {
        ignoreGetParams = b;
    }
}
