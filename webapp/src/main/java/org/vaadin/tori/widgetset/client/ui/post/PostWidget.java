package org.vaadin.tori.widgetset.client.ui.post;

import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.widgetset.client.ui.post.PostData.PostAdditionalData;
import org.vaadin.tori.widgetset.client.ui.post.PostData.PostPrimaryData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class PostWidget extends Composite {

    @UiField
    public AnchorElement avatar;
    @UiField
    public AnchorElement authorName;
    @UiField
    public SpanElement badge;
    @UiField
    public DivElement prettyTime;
    @UiField
    public SimplePanel postEditorPlaceholder;
    @UiField
    public SimplePanel footer;
    @UiField
    public DivElement body;
    @UiField
    public DivElement bodyText;
    @UiField
    public FlowPanel attachments;
    @UiField
    public AnchorElement permaLink;
    @UiField
    public SimplePanel settings;

    private static PostWidgetUiBinder uiBinder = GWT
            .create(PostWidgetUiBinder.class);

    interface PostWidgetUiBinder extends UiBinder<Widget, PostWidget> {
    }

    public PostWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        setVisible(false);
    }

    public void updatePostData(final PostPrimaryData data) {
        authorName.setInnerText(data.authorName);
        authorName.setHref(data.authorLink);
        avatar.setHref(data.authorLink);
        if (data.authorLink == null) {
            authorName.addClassName("nolink");
        }

        bodyText.setInnerHTML(data.postBody);

        final Map<String, String> attachmentMap = data.attachments;
        attachments.setVisible(attachmentMap != null
                && !attachmentMap.isEmpty());

        attachments.clear();
        if (attachmentMap != null) {
            for (Entry<String, String> entry : attachmentMap.entrySet()) {
                attachments.add(new Anchor(SimpleHtmlSanitizer
                        .sanitizeHtml(entry.getValue()), entry.getKey()));
            }
        }

        if (data.authorAvatarUrl != null) {
            avatar.getStyle().setBackgroundImage(
                    "url(" + data.authorAvatarUrl + ")");
        } else {
            avatar.addClassName("anonymous");
        }

        setVisible(true);
    }

    public void updatePostData(final PostAdditionalData data) {
        prettyTime.setInnerText(data.prettyTime);
        badge.setInnerHTML(data.badgeHTML);
        permaLink.setHref(data.permaLink);

        footer.setWidget(((AbstractComponentConnector) data.footer).getWidget());
        settings.setWidget(((AbstractComponentConnector) data.settings)
                .getWidget());
    }

    public void addEditPostComponent(final Widget widget) {
        postEditorPlaceholder.setWidget(widget);
    }
}
