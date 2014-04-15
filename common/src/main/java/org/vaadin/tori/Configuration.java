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
    private boolean showThreadsOnDashboard;
    private Map<String, String> replacements;
    private String googleAnalyticsTrackerId;
    private String mayNotReplyNote;

    // Email
    private boolean useToriMailService;
    private String emailHeaderImageUrl;
    private String emailFromAddress;
    private String emailFromName;
    private String emailReplyToAddress;

    public String getMayNotReplyNote() {
        return mayNotReplyNote;
    }

    public void setMayNotReplyNote(final String mayNotReplyNote) {
        this.mayNotReplyNote = mayNotReplyNote;
    }

    /**
     * Custom regex-replacements applied before displaying posts.
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
     * Custom regex-replacements applied before displaying posts.
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

    public boolean isShowThreadsOnDashboard() {
        return showThreadsOnDashboard;
    }

    public void setShowThreadsOnDashboard(final boolean showThreadsOnDashboard) {
        this.showThreadsOnDashboard = showThreadsOnDashboard;
    }

    public boolean isUseToriMailService() {
        return useToriMailService;
    }

    public void setUseToriMailService(final boolean useToriMailService) {
        this.useToriMailService = useToriMailService;
    }

    public String getEmailHeaderImageUrl() {
        return emailHeaderImageUrl;
    }

    public void setEmailHeaderImageUrl(final String emailHeaderImageUrl) {
        this.emailHeaderImageUrl = emailHeaderImageUrl;
    }

    public String getEmailFromAddress() {
        return emailFromAddress;
    }

    public void setEmailFromAddress(final String emailFromAddress) {
        this.emailFromAddress = emailFromAddress;
    }

    public String getEmailFromName() {
        return emailFromName;
    }

    public void setEmailFromName(final String emailFromName) {
        this.emailFromName = emailFromName;
    }

    public String getEmailReplyToAddress() {
        return emailReplyToAddress;
    }

    public void setEmailReplyToAddress(final String emailReplyToAddress) {
        this.emailReplyToAddress = emailReplyToAddress;
    }

    public boolean isReplaceMessageBoardsLinks() {
        return replaceMessageBoardsLinks;
    }

    public void setReplaceMessageBoardsLinks(
            final boolean replaceMessageBoardsLinks) {
        this.replaceMessageBoardsLinks = replaceMessageBoardsLinks;
    }

}
