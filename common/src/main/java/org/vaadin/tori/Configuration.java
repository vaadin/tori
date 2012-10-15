package org.vaadin.tori;

import java.util.Map;

import javax.annotation.CheckForNull;

public class Configuration {

    private boolean replaceMessageBoardsLinks;
    private Map<String, String> replacements;
    private String googleAnalyticsTrackerId;

    /**
     * Whether or not messages' links are replaced with what is in
     * {@link #getReplacements()}
     */
    public boolean replaceMessageBoardsLinks() {
        return replaceMessageBoardsLinks;
    }

    /**
     * How links are replaced in posts
     * 
     * @see #replaceMessageBoardsLinks()
     */
    public Map<String, String> getReplacements() {
        return replacements;
    }

    /**
     * The Google Analytics tracker to be used to log activity.
     * <code>null</code> for not to log.
     */
    @CheckForNull
    public String getGoogleAnalyticsTrackerId() {
        return googleAnalyticsTrackerId;
    }

    /**
     * Whether or not messages' links are replaced with what is in
     * {@link #getReplacements()}
     */
    public void setReplaceMessageBoardsLinks(
            final boolean replaceMessageBoardsLinks) {
        this.replaceMessageBoardsLinks = replaceMessageBoardsLinks;
    }

    /**
     * How links are replaced in posts
     * 
     * @see #replaceMessageBoardsLinks()
     */
    public void setReplacements(final Map<String, String> replacements) {
        this.replacements = replacements;
    }

    /**
     * The Google Analytics tracker to be used to log activity.
     * <code>null</code> for not to log.
     */
    public void setGoogleAnalyticsTrackerId(
            final String googleAnalyticsTrackerId) {
        this.googleAnalyticsTrackerId = googleAnalyticsTrackerId;
    }

}
