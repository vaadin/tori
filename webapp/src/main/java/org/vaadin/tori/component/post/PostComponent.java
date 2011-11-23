package org.vaadin.tori.component.post;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.thread.ThreadPresenter;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "We don't bother us with serialization.")
public class PostComponent extends CustomComponent {

    // trying a new pattern here
    private static class Util {
        private static Component newConfirmBanComponent(
                final ThreadPresenter presenter, final User user,
                final ContextMenu menu) {

            final VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            layout.setWidth("200px");
            layout.addComponent(new HeadingLabel("Ban "
                    + user.getDisplayedName() + "?", HeadingLevel.H2));

            final HorizontalLayout buttonBar = new HorizontalLayout();
            layout.addComponent(buttonBar);
            layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);

            final NativeButton ban = new NativeButton("Yes, Ban",
                    new ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            presenter.ban(user);
                            menu.close();
                        }
                    });
            buttonBar.addComponent(ban);

            final NativeButton cancel = new NativeButton("No, Cancel!",
                    new ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            menu.close();
                        }
                    });
            buttonBar.addComponent(cancel);

            return layout;
        }

        public static Component newConfirmDeleteComponent(
                final ThreadPresenter presenter, final Post post,
                final ContextMenu menu) {

            final VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            layout.setWidth("200px");
            layout.addComponent(new HeadingLabel("Delete Post?",
                    HeadingLevel.H2));

            final HorizontalLayout buttonBar = new HorizontalLayout();
            layout.addComponent(buttonBar);
            layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);

            final NativeButton ban = new NativeButton("Yes, Delete",
                    new ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            presenter.delete(post);
                            menu.close();
                        }
                    });
            buttonBar.addComponent(ban);

            final NativeButton cancel = new NativeButton("No, Cancel!",
                    new ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            menu.close();
                        }
                    });
            buttonBar.addComponent(cancel);

            return layout;
        }
    }

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

    private final Component reportComponent;
    private final NativeButton editButton;
    private final NativeButton quoteButton;
    private final ContextMenu contextMenu;
    private final ThreadPresenter presenter;
    private final PostScoreComponent score;

    /**
     * @throws IllegalArgumentException
     *             if any argument is <code>null</code>.
     */
    public PostComponent(final Post post, final ThreadPresenter presenter) {

        ToriUtil.checkForNull(post, "post may not be null");
        ToriUtil.checkForNull(presenter, "presenter may not be null");

        this.presenter = presenter;
        this.post = post;

        editButton = new NativeButton("Edit Post", editListener);
        editButton.setVisible(false);

        quoteButton = new NativeButton("Quote for Reply", replyListener);
        quoteButton.setVisible(false);

        root = new CustomLayout(ToriApplication.CUSTOM_LAYOUT_PATH
                + "postlayout");
        setCompositionRoot(root);
        setStyleName("post");

        contextMenu = new ContextMenu();
        score = new PostScoreComponent(post, presenter);

        root.addComponent(getAvatarImage(post), "avatar");
        root.addComponent(new Label(post.getAuthor().getDisplayedName()),
                "authorname");
        root.addComponent(new Label(getPostedAgoText(post)), "postedtime");
        root.addComponent(getPermaLink(post), "permalink");
        root.addComponent(new Label(getFormattedXhtmlBody(post),
                Label.CONTENT_XHTML), "body");
        root.addComponent(score, "score");
        root.addComponent(
                reportComponent = buildReportPostComponent(post, presenter),
                "report");
        root.addComponent(contextMenu, "settings");
        root.addComponent(editButton, "edit");
        root.addComponent(quoteButton, "quote");
    }

    public void enableReporting() {
        reportComponent.setVisible(true);
    }

    public void enableEditing() {
        editButton.setVisible(true);
    }

    public void enableQuoting() {
        quoteButton.setVisible(true);
    }

    public void enableThreadFollowing() {
        contextMenu.add(new ThemeResource("images/icon-follow.png"),
                "Follow Thread", new ContextMenu.ContextAction() {
                    @Override
                    public void contextClicked() {
                        presenter.followThread();
                    }
                });
    }

    public void enableThreadUnFollowing() {
        contextMenu.add(new ThemeResource("images/icon-unfollow.png"),
                "Unfollow Thread", new ContextMenu.ContextAction() {
                    @Override
                    public void contextClicked() {
                        presenter.unFollowThread();
                    }
                });
    }

    public void enableBanning() {
        contextMenu.add(new ThemeResource("images/icon-ban.png"), "Ban Author",
                new ContextMenu.ContextComponentSwapper() {
                    @Override
                    public Component swapContextComponent() {
                        return Util.newConfirmBanComponent(presenter,
                                post.getAuthor(), contextMenu);
                    }
                });
    }

    public void enableDeleting() {
        contextMenu.add(new ThemeResource("images/icon-delete.png"),
                "Delete Post", new ContextMenu.ContextComponentSwapper() {
                    @Override
                    public Component swapContextComponent() {
                        return Util.newConfirmDeleteComponent(presenter, post,
                                contextMenu);
                    }
                });
    }

    public void enableUpDownVoting(final PostVote postVote) {
        score.enableUpDownVoting(postVote);
    }

    private Component buildReportPostComponent(final Post post,
            final ThreadPresenter presenter) {
        final Button button = new Button("Report Post");
        button.setStyleName(Reindeer.BUTTON_LINK);
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                final int x = event.getClientX();
                final int y = event.getClientY();
                getApplication().getMainWindow().addWindow(
                        new ReportWindow(post, presenter, x, y));
            }
        });
        button.setVisible(false);
        return button;
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
