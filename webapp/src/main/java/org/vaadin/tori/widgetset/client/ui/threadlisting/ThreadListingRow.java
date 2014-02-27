package org.vaadin.tori.widgetset.client.ui.threadlisting;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class ThreadListingRow extends Composite {

    private static final String ROW_CLASS_NAME = "threadlistingrow";

    @UiField
    public AnchorElement topic;
    @UiField
    public SpanElement topicName;
    @UiField
    public SpanElement follow;
    @UiField
    public DivElement startedBy;
    @UiField
    public DivElement postCount;
    @UiField
    public AnchorElement latest;
    @UiField
    public SpanElement latestPretty;
    @UiField
    public SpanElement latestAuthor;
    @UiField
    public FocusPanel settings;

    private static ThreadListingRowUiBinder uiBinder = GWT
            .create(ThreadListingRowUiBinder.class);

    interface ThreadListingRowUiBinder extends
            UiBinder<Widget, ThreadListingRow> {
    }

    public ThreadListingRow(final RowInfo rowInfo) {
        initWidget(uiBinder.createAndBindUi(this));
        setWidth("100%");
        updateRowInfo(rowInfo);
    }

    public void updateRowInfo(final RowInfo rowInfo) {
        setStyleName(ROW_CLASS_NAME);
        if (rowInfo.isLocked) {
            addStyleName("locked");
        }
        if (rowInfo.isSticky) {
            addStyleName("sticky");
        }
        if (rowInfo.isFollowed) {
            follow.setTitle("I'm following this thread");
            addStyleName("following");
        }
        if (!rowInfo.isRead) {
            addStyleName("unread");
        }

        topic.setHref(rowInfo.url);
        topicName.setInnerText(rowInfo.topic);

        startedBy.setInnerText(rowInfo.author);

        String postCount = rowInfo.postCount != 0 ? String
                .valueOf(rowInfo.postCount) : "";
        this.postCount.setInnerText(postCount);

        latest.setHref(rowInfo.latestPostUrl);
        latestAuthor.setInnerText(rowInfo.latestAuthor);
        latestPretty.setInnerText(rowInfo.latestPostPretty);

        if (rowInfo.settings != null) {
            Widget settings = ((AbstractComponentConnector) rowInfo.settings)
                    .getWidget();
            this.settings.setWidget(settings);
        }

    }
}
