package org.vaadin.tori.widgetset.client.ui.threadlisting;

import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.Connector;

@SuppressWarnings("serial")
public class ThreadListingState extends AbstractComponentState {

    public static final int UNINITIALIZED_ROWS = -1;
    public int rows = UNINITIALIZED_ROWS;

    public int loadedRows;

    public static class RowInfo {
        public long threadId;
        public boolean isSticky;
        public boolean isLocked;
        public boolean isFollowed;
        public String topic;
        public String author;
        public int postCount;
        public String latestPostPretty;
        public String url;
        public boolean isRead;
        public Connector settings;
        public String latestPostUrl;
        public String latestAuthor;
    }

}
