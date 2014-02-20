package org.vaadin.tori.widgetset.client.ui.post;

import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.widgetset.client.ui.post.PostData.PostAdditionalData;
import org.vaadin.tori.widgetset.client.ui.post.PostData.PostPrimaryData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class PostWidget extends Composite {

    private PostWidgetListener listener;

    @UiField
    public AnchorElement avatar;
    @UiField
    public AnchorElement authorName;
    @UiField
    public SpanElement badge;
    @UiField
    public DivElement prettyTime;
    @UiField
    public FocusPanel postEditorPlaceholder;
    @UiField
    public DivElement body;
    @UiField
    public DivElement bodyText;
    @UiField
    public FlowPanel attachments;
    @UiField
    public AnchorElement permaLink;

    @UiField
    public Label upVote;
    @UiField
    public Label downVote;
    @UiField
    public SpanElement score;
    @UiField
    public Label quote;
    @UiField
    public FocusPanel settings;
    @UiField
    public FocusPanel flag;

    private static PostWidgetUiBinder uiBinder = GWT
            .create(PostWidgetUiBinder.class);

    interface PostWidgetUiBinder extends UiBinder<Widget, PostWidget> {
    }

    public PostWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        setVisible(false);
    }

    @UiHandler("quote")
    void handleQuoteClick(final ClickEvent e) {
        listener.quoteForReply();
    }

    @UiHandler("upVote")
    void handleUpVoteClick(final ClickEvent e) {
        listener.postVoted(true);
    }

    @UiHandler("downVote")
    void handleDownVoteClick(final ClickEvent e) {
        listener.postVoted(false);
    }

    public void setListener(final PostWidgetListener listener) {
        this.listener = listener;
    }

    public void updatePostData(final PostPrimaryData data) {
        authorName.setInnerText(data.getAuthorName());
        authorName.setHref(data.getAuthorLink());
        avatar.setHref(data.getAuthorLink());
        if (data.getAuthorLink() == null) {
            authorName.addClassName("nolink");
        }

        if (data.isAllowHTML()) {
            bodyText.setInnerHTML(data.getPostBody());
        } else {
            bodyText.setInnerText(data.getPostBody());
        }

        Map<String, String> attachmentMap = data.getAttachments();
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

        if (data.getAuthorAvatarUrl() != null) {
            avatar.getStyle().setBackgroundImage(
                    "url(" + data.getAuthorAvatarUrl() + ")");
        } else {
            avatar.addClassName("anonymous");
        }

        setVisible(true);
    }

    public void updatePostData(final PostAdditionalData data) {
        prettyTime.setInnerText(data.getPrettyTime());
        badge.setInnerHTML(data.getBadgeHTML());
        quote.setVisible(data.isQuotingEnabled());
        permaLink.setHref(data.getPermaLink());

        upVote.setVisible(data.isVotingEnabled());
        downVote.setVisible(data.isVotingEnabled());
        upVote.setStyleName("upvote vote");
        downVote.setStyleName("downvote vote");
        if (data.getUpVoted() != null) {
            if (data.getUpVoted()) {
                upVote.addStyleName("done");
            } else {
                downVote.addStyleName("done");
            }
        }

        long newScore = data.getScore();
        score.setInnerText((newScore > 0 ? "+" : "") + String.valueOf(newScore));
        String scoreStyle = "zero";
        if (newScore > 0) {
            scoreStyle = "positive";
        } else if (newScore < 0) {
            scoreStyle = "negative";
        }
        score.setClassName(scoreStyle);

        flag.setWidget(null);
        if (data.getReport() != null) {
            flag.setWidget(((AbstractComponentConnector) data.getReport())
                    .getWidget());
        }

        settings.setWidget(null);
        if (data.getReport() != null) {
            settings.setWidget(((AbstractComponentConnector) data.getSettings())
                    .getWidget());
        }
    }

    public void addEditPostComponent(final Widget widget) {
        postEditorPlaceholder.setWidget(widget);
    }
}
