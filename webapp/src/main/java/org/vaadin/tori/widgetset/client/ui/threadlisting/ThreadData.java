package org.vaadin.tori.widgetset.client.ui.threadlisting;

import com.vaadin.shared.Connector;

public class ThreadData {

    public static class ThreadPrimaryData {
        public long threadId;
        public String topic;
        public String author;
        public int postCount;
        public String latestPostPretty;
    }

    public static class ThreadAdditionalData {
        public long threadId;
        public boolean isSticky;
        public boolean isLocked;
        public boolean mayFollow;
        public boolean isFollowed;
        public String url;
        public boolean isRead;
        public Connector settings;
        public String latestPostUrl;
        public String latestAuthor;
    }
}
