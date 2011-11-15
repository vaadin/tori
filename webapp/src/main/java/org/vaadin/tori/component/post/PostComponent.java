package org.vaadin.tori.component.post;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.ContextMenu.Builder;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.service.post.PostReportReceiver;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class PostComponent extends CustomComponent {

    private static final String EXAMPLE_IMAGE_URL = "http://cache.ohinternet.com/images/thumb/2/2d/Trollface_HD.png/618px-Trollface_HD.png";

    private final CustomLayout root;
    private final Post post;

    private final ClickListener editListener = new ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            getApplication().getMainWindow().showNotification(
                    "Editing not implemented yet.");
        }
    };

    private final ClickListener replyListener = new ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            getApplication().getMainWindow().showNotification(
                    "Replying not implemented yet");
        }
    };

    /**
     * @throws IllegalArgumentException
     *             if <code>post</code> is <code>null</code>.
     */
    public PostComponent(final Post post,
            final PostReportReceiver reportReceiver) {
        ToriUtil.checkForNull(post, "post may not be null");
        ToriUtil.checkForNull(reportReceiver,
                "post report receiver may not be null");
        this.post = post;

        root = new CustomLayout("../../../layouts/postlayout");
        setCompositionRoot(root);
        setStyleName("post");

        root.addComponent(getAvatarImage(post), "avatar");
        root.addComponent(new Label(post.getAuthor().getDisplayedName()),
                "authorname");
        root.addComponent(new Label(getPostedAgoText(post)), "postedtime");
        root.addComponent(getPermaLink(post), "permalink");
        root.addComponent(new Label(getFormattedXhtmlBody(post),
                Label.CONTENT_XHTML), "body");
        root.addComponent(new Label("0"), "score");
        root.addComponent(buildReportPostComponent(post, reportReceiver),
                "report");
        root.addComponent(buildContextMenu(), "settings");
        root.addComponent(new NativeButton("Edit Post", editListener), "edit");
        root.addComponent(new NativeButton("Quote for Reply", replyListener),
                "quote");
    }

    private Component buildReportPostComponent(final Post post,
            final PostReportReceiver reportReciever) {
        final Button button = new Button("Report Post");
        button.setStyleName(Reindeer.BUTTON_LINK);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                final int x = event.getClientX();
                final int y = event.getClientY();
                getApplication().getMainWindow().addWindow(
                        new ReportWindow(post, reportReciever, x, y));
            }
        });
        return button;
    }

    private ContextMenu buildContextMenu() {
        final Builder builder = new ContextMenu.Builder();
        builder.add(null, "[TODO]", new ContextMenu.ContextAction() {
            @Override
            public void contextClicked() {
                getApplication().getMainWindow().showNotification("...");
            }
        });
        return builder.build();
    }

    private String getFormattedXhtmlBody(final Post post) {
        return ToriApplication.getCurrent().getPostFormatter()
                .format(post.getBodyRaw());
    }

    private static Component getPermaLink(final Post post) {
        final String linkString = String.format(
                "<a href=\"#%s/%s/%s\">Permalink</a>", //
                ToriNavigator.ApplicationView.THREADS.getUrl(), //
                post.getThread().getId(), //
                post.getId() //
                );

        final Label label = new Label(linkString, Label.CONTENT_XHTML);
        return label;
    }

    private static String getPostedAgoText(final Post post) {
        return "posted " + new PrettyTime().format(post.getTime());
    }

    private Embedded getAvatarImage(final Post post) {
        final String avatarUrl = post.getAuthor().getAvatarUrl();

        final Resource imageResource;
        if (avatarUrl != null) {
            imageResource = new ExternalResource(avatarUrl);
        } else {
            imageResource = new ThemeResource(
                    "images/icon-placeholder-avatar.gif");
        }

        final Embedded image = new Embedded(null, imageResource);
        image.setType(Embedded.TYPE_IMAGE);
        image.setWidth("100px");
        image.setHeight("100px");
        return image;
    }
}
