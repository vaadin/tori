package org.vaadin.tori.widgetset.client.ui.threadlisting;

import com.vaadin.shared.Connector;

public class ThreadData {

    public static class ThreadPrimaryData {
        public String threadId;
        public String topic;
        public String author;
        public String latestAuthor;
        public int postCount;
        public String latestPostPretty;
        public String firstPostPretty;
        public boolean isRead;
    }

    public static class ThreadAdditionalData {
        public String threadId;
        public boolean isSticky;
        public boolean isLocked;
        public boolean mayFollow;
        public boolean isFollowed;
        public boolean isRead;
        public String url;
        public Connector settings;
        public String latestPostUrl;
        public int replyCount;
    }
}
