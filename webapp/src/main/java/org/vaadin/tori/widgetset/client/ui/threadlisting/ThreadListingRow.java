package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.ArrayList;
import java.util.Collection;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadAdditionalData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadPrimaryData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class ThreadListingRow extends Composite implements ClickHandler {

    private static final String ROW_CLASS_NAME = "threadlistingrow";

    @UiField
    public SpanElement topic;
    @UiField
    public Label follow;
    @UiField
    public AnchorElement topicName;

    @UiField
    public SpanElement started;
    @UiField
    public SpanElement startedBy;
    @UiField
    public SpanElement startedTime;

    @UiField
    public AnchorElement latest;
    @UiField
    public SpanElement replyCountWrapper;
    @UiField
    public SpanElement replyCount;
    @UiField
    public SpanElement latestBy;
    @UiField
    public SpanElement latestTime;

    @UiField
    public SimplePanel settings;

    private final long threadId;

    private final ThreadListingRowListener listener;
    private HandlerRegistration clickHandler;

    private final Collection<String> staticStyleNames = new ArrayList<String>();
    private String threadHash;
    private boolean following;

    private static ThreadListingRowUiBinder uiBinder = GWT
            .create(ThreadListingRowUiBinder.class);

    interface ThreadListingRowUiBinder extends
            UiBinder<Widget, ThreadListingRow> {
    }

    @UiHandler("follow")
    void handleFollowClick(final ClickEvent e) {
        listener.follow(threadId, !following);
        threadFollowed(!following);
    }

    public ThreadListingRow(final ThreadPrimaryData data,
            final ThreadListingRowListener listener) {
        this.threadId = Long.parseLong(data.threadId);
        this.listener = listener;
        initWidget(uiBinder.createAndBindUi(this));
        setWidth("100%");
        setStyleName(ROW_CLASS_NAME);

        topicName.setInnerText(data.topic);
        startedBy.setInnerText(data.author);
        startedTime.setInnerText(shortenPretty(data.firstPostPretty));
        started.setTitle("Topic created by " + data.author + " "
                + data.firstPostPretty);

        int replyCountValue = data.postCount - 1;
        replyCount.setInnerText(replyCountValue == 0 ? "" : String
                .valueOf(replyCountValue));

        String replyTitle = replyCountValue
                + (replyCountValue == 1 ? " reply" : " replies")
                + " to this topic";
        replyCountWrapper.setTitle(replyTitle);

        if (replyCountValue == 0) {
            staticStyleNames.add("noreplies");
            latestBy.setInnerText("No replies yet");
            latest.setTitle("No replies to this topic yet");
        } else {
            if (replyCountValue < 10) {
                staticStyleNames.add("shortthread");
            } else if (replyCountValue > 30) {
                staticStyleNames.add("longthread");
            }
            latestBy.setInnerText(data.latestAuthor);
            latestTime.setInnerText(shortenPretty(data.latestPostPretty));

            latest.setTitle("Latest reply by " + data.latestAuthor + " "
                    + data.latestPostPretty);
        }

        if (!data.isRead) {
            addStyleName("unread");
        }
        addStaticStyleNames();
    }

    private void addStaticStyleNames() {
        for (String style : staticStyleNames) {
            addStyleName(style);
        }
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

    private String shortenPretty(final String prettyTime) {
        String result = prettyTime;
        if (!result.startsWith("moments")) {
            result = result.replaceAll("ago", "").trim();
        }
        return result;
    }

    public void updateRowInfo(final ThreadAdditionalData data) {
        setStyleName(ROW_CLASS_NAME);
        threadFollowed(data.isFollowed);
        if (!data.mayFollow) {
            addStyleName("maynotfollow");
        }
        if (!data.isRead) {
            addStyleName("unread");
        }
        if (data.isLocked) {
            addStyleName("locked");
        }
        if (data.isSticky) {
            addStyleName("sticky");
        }
        addStaticStyleNames();

        topicName.setHref(data.url);
        threadHash = data.url;

        if (staticStyleNames.contains("noreplies")) {
            latest.removeAttribute("href");
        } else {
            latest.setHref(data.latestPostUrl);
        }

        if (data.settings != null) {
            Widget settings = ((AbstractComponentConnector) data.settings)
                    .getWidget();
            this.settings.setWidget(settings);
        }
    }

    @Override
    public void onClick(final ClickEvent event) {
        Element clicked = Element.as(event.getNativeEvent().getEventTarget());
        if (!follow.getElement().isOrHasChild(clicked)
                && !settings.getElement().isOrHasChild(clicked)
                && !topicName.isOrHasChild(clicked)) {
            if (!latest.isOrHasChild(clicked)
                    || latest.getAttribute("href").isEmpty()) {
                String threadUrl = Location.createUrlBuilder()
                        .setHash(threadHash).buildString();
                Window.Location.assign(threadUrl);
                event.stopPropagation();
            }
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        clickHandler = addDomHandler(this, ClickEvent.getType());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        clickHandler.removeHandler();
    }

    public interface ThreadListingRowListener {
        void follow(long threadId, boolean follow);
    }
}
