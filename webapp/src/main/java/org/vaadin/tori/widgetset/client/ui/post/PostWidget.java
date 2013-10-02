package org.vaadin.tori.widgetset.client.ui.post;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class PostWidget extends Composite {

    private PostWidgetListener listener;

    @UiField
    public ImageElement avatar;
    @UiField
    public DivElement authorName;
    @UiField
    public DivElement badge;
    @UiField
    public SpanElement prettyTime;
    @UiField
    public SpanElement timeStamp;
    @UiField
    public DivElement bodyText;
    @UiField
    public Label signature;
    @UiField
    public FlowPanel attachments;
    @UiField
    public AnchorElement permaLink;

    @UiField
    public Button upVote;
    @UiField
    public Button downVote;
    @UiField
    public DivElement score;
    @UiField
    public Label quote;
    @UiField
    public FocusPanel settings;
    @UiField
    public FocusPanel report;
    @UiField
    public FocusPanel edit;

    private static PostWidgetUiBinder uiBinder = GWT
            .create(PostWidgetUiBinder.class);

    interface PostWidgetUiBinder extends UiBinder<Widget, PostWidget> {
    }

    public PostWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        setVisible(false);
    }

    @UiHandler("quote")
    void handleQuoteClick(ClickEvent e) {
        listener.quoteForReply();
    }

    @UiHandler("upVote")
    void handleUpVoteClick(ClickEvent e) {
        listener.postVoted(true);
    }

    @UiHandler("downVote")
    void handleDownVoteClick(ClickEvent e) {
        listener.postVoted(false);
    }

    @UiHandler("settings")
    void settingsClick(ClickEvent e) {
        listener.settingsClicked();
    }

    @UiHandler("edit")
    void editClick(ClickEvent e) {
        listener.editClicked();
    }

    @UiHandler("report")
    void reportClick(ClickEvent e) {
        listener.reportClicked();
    }

    public void setListener(PostWidgetListener listener) {
        this.listener = listener;
    }

    public void updatePostData(PostComponentState state, String avatarUrl) {
        avatar.setSrc(avatarUrl);
        authorName.setInnerText(state.getAuthorName());
        prettyTime.setInnerText(state.getPrettyTime());
        timeStamp.setInnerText(state.getTimeStamp());
        badge.setInnerHTML(state.getBadgeHTML());

        quote.setVisible(state.isQuotingEnabled());

        if (state.isAllowHTML()) {
            bodyText.setInnerHTML(state.getPostBody());
        } else {
            bodyText.setInnerText(state.getPostBody());
        }
        permaLink.setHref(state.getPermaLink());

        signature.setVisible(state.getSignature() != null
                && !state.getSignature().trim().isEmpty());
        signature.setText(state.getSignature());

        upVote.setVisible(state.isVotingEnabled());
        downVote.setVisible(state.isVotingEnabled());
        upVote.setStyleName("upvote vote");
        downVote.setStyleName("downvote vote");
        if (state.getUpVoted() != null) {
            if (state.getUpVoted()) {
                upVote.addStyleName("done");
            } else {
                downVote.addStyleName("done");
            }
        }

        long newScore = state.getScore();
        score.setInnerText((newScore > 0 ? "+" : "") + String.valueOf(newScore));

        String scoreStyle = "zero";
        if (newScore > 0) {
            scoreStyle = "positive";
        } else if (newScore < 0) {
            scoreStyle = "negative";
        }
        score.setClassName(scoreStyle);

        edit.setVisible(state.isEditingEnabled());
        if (state.getEdit() != null) {
            edit.setWidget(((AbstractComponentConnector) state.getEdit())
                    .getWidget());
        }
        report.setVisible(state.isReportingEnabled());
        if (state.getReport() != null) {
            report.setWidget(((AbstractComponentConnector) state.getReport())
                    .getWidget());
        }
        settings.setVisible(state.isSettingsEnabled());
        if (state.getSettings() != null) {
            settings.setWidget(((AbstractComponentConnector) state
                    .getSettings()).getWidget());
        }

        Map<String, String> attachmentMap = state.getAttachments();
        attachments.setVisible(attachmentMap != null
                && !attachmentMap.isEmpty());

        attachments.clear();
        if (attachmentMap != null) {
            for (Entry<String, String> entry : attachmentMap.entrySet()) {
                Anchor link = new Anchor(SimpleHtmlSanitizer.sanitizeHtml(entry
                        .getValue()), entry.getKey());
                attachments.add(link);
            }
        }

        setVisible(true);
    }
}
