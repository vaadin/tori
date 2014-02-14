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

package org.vaadin.tori.widgetset.client.ui.post;

import java.util.Map;

import com.vaadin.shared.Connector;

public class PostData {

    public static class PostPrimaryData {
        private String authorName;
        private String authorLink;
        private String postBody;
        private String authorAvatarUrl;
        private Map<String, String> attachments;
        private boolean allowHTML;

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getAuthorLink() {
            return authorLink;
        }

        public void setAuthorLink(String authorLink) {
            this.authorLink = authorLink;
        }

        public String getPostBody() {
            return postBody;
        }

        public void setPostBody(String postBody) {
            this.postBody = postBody;
        }

        public Map<String, String> getAttachments() {
            return attachments;
        }

        public void setAttachments(Map<String, String> attachments) {
            this.attachments = attachments;
        }

        public boolean isAllowHTML() {
            return allowHTML;
        }

        public void setAllowHTML(boolean allowHTML) {
            this.allowHTML = allowHTML;
        }

        public String getAuthorAvatarUrl() {
            return authorAvatarUrl;
        }

        public void setAuthorAvatarUrl(String authorAvatarUrl) {
            this.authorAvatarUrl = authorAvatarUrl;
        }

    }

    public static class PostAdditionalData {
        private String prettyTime;
        private String permaLink;
        private Boolean upVoted;
        private long score;
        private boolean quotingEnabled;
        private boolean votingEnabled;
        private String badgeHTML;

        private Connector settings;
        private Connector report;

        public String getPrettyTime() {
            return prettyTime;
        }

        public void setPrettyTime(String prettyTime) {
            this.prettyTime = prettyTime;
        }

        public String getPermaLink() {
            return permaLink;
        }

        public void setPermaLink(String permaLink) {
            this.permaLink = permaLink;
        }

        public Boolean getUpVoted() {
            return upVoted;
        }

        public void setUpVoted(Boolean upVoted) {
            this.upVoted = upVoted;
        }

        public long getScore() {
            return score;
        }

        public void setScore(long score) {
            this.score = score;
        }

        public boolean isQuotingEnabled() {
            return quotingEnabled;
        }

        public void setQuotingEnabled(boolean quotingEnabled) {
            this.quotingEnabled = quotingEnabled;
        }

        public boolean isVotingEnabled() {
            return votingEnabled;
        }

        public void setVotingEnabled(boolean votingEnabled) {
            this.votingEnabled = votingEnabled;
        }

        public String getBadgeHTML() {
            return badgeHTML;
        }

        public void setBadgeHTML(String badgeHTML) {
            this.badgeHTML = badgeHTML;
        }

        public Connector getSettings() {
            return settings;
        }

        public void setSettings(Connector settings) {
            this.settings = settings;
        }

        public Connector getReport() {
            return report;
        }

        public void setReport(Connector report) {
            this.report = report;
        }

    }
}
