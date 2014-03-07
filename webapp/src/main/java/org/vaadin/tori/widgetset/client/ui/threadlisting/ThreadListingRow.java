package org.vaadin.tori.widgetset.client.ui.threadlisting;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadAdditionalData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadPrimaryData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.LazyDomElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class ThreadListingRow extends Composite {

    private static final String ROW_CLASS_NAME = "threadlistingrow";

    @UiField
    public SpanElement topic;
    @UiField
    public AnchorElement topicName;
    @UiField
    public Label follow;
    @UiField
    public DivElement startedBy;
    @UiField
    public DivElement postCount;
    @UiField
    public AnchorElement latest;
    @UiField
    public SpanElement latestPretty;
    @UiField
    public LazyDomElement<SpanElement> latestAuthor;
    @UiField
    public SimplePanel settings;

    private final long threadId;

    private final ThreadListingRowListener listener;

    private boolean following;

    private static ThreadListingRowUiBinder uiBinder = GWT
            .create(ThreadListingRowUiBinder.class);

    interface ThreadListingRowUiBinder extends
            UiBinder<Widget, ThreadListingRow> {
    }

    @UiHandler("follow")
    void handleQuoteClick(final ClickEvent e) {
        listener.follow(threadId, !following);
        threadFollowed(!following);
    }

    public ThreadListingRow(final ThreadPrimaryData rowInfo,
            final ThreadListingRowListener listener) {
        this.threadId = rowInfo.threadId;
        this.listener = listener;
        initWidget(uiBinder.createAndBindUi(this));
        setWidth("100%");
        setStyleName(ROW_CLASS_NAME);
        latest.setHref(Window.Location.getHref());
        updateRowInfo(rowInfo);
    }

    public void threadFollowed(final boolean followed) {
        this.following = followed;
        final String followingStyleName = "following";
        removeStyleName(followingStyleName);
        if (followed) {
            follow.setTitle("I'm following this topic. Click to unfollow");
            addStyleName(followingStyleName);
        } else {
            follow.setTitle("Follow topic");
        }
    }

    public void updateRowInfo(final ThreadPrimaryData data) {
        topicName.setInnerText(data.topic);
        startedBy.setInnerText(data.author);
        latestPretty.setInnerText(data.latestPostPretty);

        String postCount = data.postCount != 0 ? String.valueOf(data.postCount)
                : "";
        this.postCount.setInnerText(postCount);
    }

    public void updateRowInfo(final ThreadAdditionalData data) {
        setStyleName(ROW_CLASS_NAME);
        threadFollowed(data.isFollowed);
        if (!data.mayFollow) {
            addStyleName("maynotfollow");
        }
        if (data.isLocked) {
            addStyleName("locked");
        }
        if (data.isSticky) {
            addStyleName("sticky");
        }
        if (!data.isRead) {
            addStyleName("unread");
        }

        topicName.setHref(data.url);
        latest.setHref(data.latestPostUrl);
        latestAuthor.get().setInnerText(data.latestAuthor);

        if (data.settings != null) {
            Widget settings = ((AbstractComponentConnector) data.settings)
                    .getWidget();
            this.settings.setWidget(settings);
        }
    }

    public interface ThreadListingRowListener {
        void follow(long threadId, boolean follow);
    }
}
