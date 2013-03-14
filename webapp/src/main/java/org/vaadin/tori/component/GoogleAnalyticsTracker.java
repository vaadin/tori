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

package org.vaadin.tori.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;

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
        LegacyComponent {

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
        markAsDirty();
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
