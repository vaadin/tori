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

import java.util.Map;

public class Configuration {

    private boolean updatePageTitle;
    private String pageTitlePrefix;
    private boolean replaceMessageBoardsLinks;
    private Map<String, String> replacements;
    private String googleAnalyticsTrackerId;
    private String pathRoot;
    private String mayNotReplyNote;

    public String getMayNotReplyNote() {
        return mayNotReplyNote;
    }

    public void setMayNotReplyNote(final String mayNotReplyNote) {
        this.mayNotReplyNote = mayNotReplyNote;
    }

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

    /**
     * This is the root path for Tori. E.g.
     * <code>http://localhost:8080/something/tori/</code> is represented by
     * <code>/something/tori</code>
     */
    public void setPathRoot(final String pathRoot) {
        this.pathRoot = pathRoot;
    }

    public String getPathRoot() {
        return pathRoot;
    }

    public boolean isUpdatePageTitle() {
        return updatePageTitle;
    }

    public void setUpdatePageTitle(final boolean updatePageTitle) {
        this.updatePageTitle = updatePageTitle;
    }

    public String getPageTitlePrefix() {
        return pageTitlePrefix;
    }

    public void setPageTitlePrefix(final String pageTitlePrefix) {
        this.pageTitlePrefix = pageTitlePrefix;
    }

}
