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
import com.google.gwt.uibinder.client.LazyDomElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class PostWidget extends Composite {

    @UiField
    public AnchorElement avatar;
    @UiField
    public AnchorElement authorName;
    @UiField
    public DivElement bodyText;

    @UiField
    public LazyDomElement<SpanElement> badge;
    @UiField
    public LazyDomElement<DivElement> prettyTime;
    @UiField
    public LazyDomElement<DivElement> postEditorPlaceholder;
    private SimplePanel postEditorPlaceholderWidget;
    @UiField
    public LazyDomElement<DivElement> footer;
    private Widget footerWidget;
    @UiField
    public LazyDomElement<DivElement> attachments;
    private FlowPanel attachmentsWidget;
    @UiField
    public LazyDomElement<AnchorElement> permaLink;
    @UiField
    public LazyDomElement<DivElement> settings;
    private Widget settingsWidget;

    private final HTMLPanel panel;

    private static PostWidgetUiBinder uiBinder = GWT
            .create(PostWidgetUiBinder.class);

    interface PostWidgetUiBinder extends UiBinder<HTMLPanel, PostWidget> {
    }

    public PostWidget() {
        panel = uiBinder.createAndBindUi(this);
        initWidget(panel);
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

        updateAttachments(data.attachments);

        if (data.authorAvatarUrl != null) {
            avatar.getStyle().setBackgroundImage(
                    "url(" + data.authorAvatarUrl + ")");
        } else {
            avatar.addClassName("anonymous");
        }

        setVisible(true);
    }

    private void updateAttachments(final Map<String, String> attachmentMap) {
        boolean hasAttachments = attachmentMap != null
                && !attachmentMap.isEmpty();
        if (attachmentsWidget != null) {
            attachmentsWidget.clear();
            attachmentsWidget.setVisible(hasAttachments);
        }

        if (hasAttachments) {
            if (attachmentsWidget == null) {
                attachmentsWidget = new FlowPanel();
                attachmentsWidget.setStyleName("attachments");
                panel.addAndReplaceElement(attachmentsWidget,
                        this.attachments.get());
            }
            for (Entry<String, String> entry : attachmentMap.entrySet()) {
                attachmentsWidget.add(new Anchor(SimpleHtmlSanitizer
                        .sanitizeHtml(entry.getValue()), entry.getKey()));
            }
        }
    }

    public void updatePostData(final PostAdditionalData data) {
        prettyTime.get().setInnerText(data.prettyTime);
        badge.get().setInnerHTML(data.badgeHTML);
        permaLink.get().setHref(data.permaLink);

        if (footerWidget == null && data.footer != null) {
            footerWidget = ((AbstractComponentConnector) data.footer)
                    .getWidget();
            panel.addAndReplaceElement(footerWidget, footer.get());
        }

        if (settingsWidget == null && data.settings != null) {
            settingsWidget = ((AbstractComponentConnector) data.settings)
                    .getWidget();
            panel.addAndReplaceElement(settingsWidget, settings.get());
        }
    }

    public void addEditPostComponent(final Widget widget) {
        if (postEditorPlaceholderWidget == null) {
            postEditorPlaceholderWidget = new SimplePanel();
            postEditorPlaceholderWidget.setStyleName("posteditorplaceholder");
            panel.addAndReplaceElement(postEditorPlaceholderWidget,
                    postEditorPlaceholder.get());
        }
        postEditorPlaceholderWidget.setWidget(widget);
    }
}
