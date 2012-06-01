package org.vaadin.tori.thread;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.FloatingBar;
import org.vaadin.tori.component.FloatingBar.DisplayEvent;
import org.vaadin.tori.component.FloatingBar.FloatingAlignment;
import org.vaadin.tori.component.FloatingBar.HideEvent;
import org.vaadin.tori.component.FloatingBar.VisibilityListener;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.LazyLayout;
import org.vaadin.tori.component.NewThreadComponent;
import org.vaadin.tori.component.NewThreadComponent.NewThreadListener;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.ReplyComponent;
import org.vaadin.tori.component.ReplyComponent.ReplyListener;
import org.vaadin.tori.component.post.PostComponent;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.AbstractView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.gwt.client.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("serial")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD_STORE", justification = "We're ignoring serialization")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private static final int RENDER_DELAY_MILLIS = 1000;
    private static final double RENDER_DISTANCE_MULTIPLIER = 2.0d;
    private static final String PLACEHOLDER_WIDTH = "100%";
    private static final String PLACEHOLDER_HEIGHT = "300px";

    /** The amount of posts to preload from the beginning and the end. */
    private static final int PRELOAD_THRESHHOLD = 4;

    private CssLayout layout;
    private final ReplyListener replyListener = new ReplyListener() {
        @Override
        public void submit(final String rawBody) {
            if (!rawBody.trim().isEmpty()) {
                try {
                    getPresenter().sendReply(rawBody);
                } catch (final DataSourceException e) {
                    getRoot().showNotification(
                            DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                }
            }
        }

        @Override
        public void addAttachment(final String attachmentFileName,
                final byte[] data) {
            getPresenter().addAttachment(attachmentFileName, data);
        }

        @Override
        public void resetInput() {
            getPresenter().resetInput();
        }

        @Override
        public void removeAttachment(final String fileName) {
            getPresenter().removeAttachment(fileName);
        }
    };

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "We don't care about serialization")
    private final Map<Post, PostComponent> postsToComponents = new HashMap<Post, PostComponent>();
    private final LazyLayout postsLayout;
    // private final CssLayout postsLayout;
    private ReplyComponent reply;
    private NewThreadComponent newThreadComponent;

    public ThreadViewImpl() {
        setStyleName("threadview");
        // postsLayout = new CssLayout();
        postsLayout = new LazyLayout();
        postsLayout.setRenderDistanceMultiplier(RENDER_DISTANCE_MULTIPLIER);
        postsLayout.setPlaceholderSize(PLACEHOLDER_HEIGHT, PLACEHOLDER_WIDTH);
        postsLayout.setRenderDelay(RENDER_DELAY_MILLIS);
    }

    @Override
    protected Component createCompositionRoot() {
        return layout = new CssLayout();
    }

    @Override
    public void initView() {
        layout.setWidth("100%");
    }

    @Override
    protected ThreadPresenter createPresenter() {
        final ToriApplication app = ToriApplication.getCurrent();
        return new ThreadPresenter(app.getDataSource(),
                app.getAuthorizationService());
    }

    /**
     * @return returns <code>null</code> if the visitor has entered an invalid
     *         URL or a new thread is being created.
     */
    @CheckForNull
    @Override
    public DiscussionThread getCurrentThread() {
        return getPresenter().getCurrentThread();
    }

    /**
     * @return <code>null</code> if the visitor has entered an invalid URL.
     */
    @CheckForNull
    @Override
    public Category getCurrentCategory() {
        return getPresenter().getCurrentCategory();
    }

    @Override
    public void displayPosts(final List<Post> posts,
            @NonNull final DiscussionThread currentThread) {
        layout.removeAllComponents();

        postsLayout.removeAllComponents();
        layout.addComponent(postsLayout);

        for (int i = 0; i < posts.size(); i++) {
            final Post post = posts.get(i);
            final PostComponent c = newPostComponent(post);

            if (i < PRELOAD_THRESHHOLD || i > posts.size() - PRELOAD_THRESHHOLD) {
                postsLayout.addComponentEagerly(c);
            } else {
                postsLayout.addComponent(c);
            }

            if (i == 0) {
                // create the floating summary bar for the first post
                final FloatingBar summaryBar = getSummaryBar(post, c);
                summaryBar.setScrollComponent(c);
                summaryBar.setAlignment(FloatingAlignment.TOP);
                layout.addComponent(summaryBar);
            }

        }

        if (getPresenter().userMayReply()) {
            final Label spacer = new Label("<span class=\"eof\">eof</span>",
                    ContentMode.XHTML);
            spacer.setStyleName("spacer");
            layout.addComponent(spacer);

            reply = new ReplyComponent(replyListener, getPresenter()
                    .getFormattingSyntax(), "Post Reply");
            reply.setUserMayAddFiles(getPresenter().userMayAddFiles());
            reply.setMaxFileSize(getPresenter().getMaxFileSize());
            layout.addComponent(reply);

            final FloatingBar quickReplyBar = getQuickReplyBar(reply);
            quickReplyBar.setScrollComponent(reply);
            quickReplyBar.setAlignment(FloatingAlignment.BOTTOM);

            layout.addComponent(quickReplyBar);
        }

        final Label bottomSpacer = new Label("");
        bottomSpacer.setStyleName("spacer");
        layout.addComponent(bottomSpacer);
    }

    private PostComponent newPostComponent(final Post post) {
        final PostComponent c = new PostComponent(post, getPresenter());
        postsToComponents.put(post, c);

        // main component permissions

        if (getPresenter().userMayReportPosts()) {
            c.enableReporting();
        }
        if (getPresenter().userMayEdit(post)) {
            c.enableEditing();
        }
        if (getPresenter().userMayQuote(post)) {
            c.enableQuoting();
        }
        if (getPresenter().userMayVote()) {
            try {
                c.enableUpDownVoting(getPresenter().getPostVote(post));
            } catch (final DataSourceException e) {
                // NOP - everything's logged.
            }
        }

        // context menu permissions

        try {
            if (getPresenter().userCanFollowThread()) {
                c.enableThreadFollowing();
            }
            if (getPresenter().userCanUnFollowThread()) {
                c.enableThreadUnFollowing();
            }
        } catch (final DataSourceException e) {
            // NOP - everything's logged. If you can't follow, you can't
            // unfollow either.
        }
        if (getPresenter().userMayBan()) {
            c.enableBanning();
        }
        if (getPresenter().userMayDelete(post)) {
            c.enableDeleting();
        }
        return c;
    }

    private PostComponent newPostSummaryComponent(final Post post) {
        final PostComponent c = new PostComponent(post, getPresenter());
        c.addStyleName("summary");
        return c;
    }

    private Component getThreadSummary(final Post firstPost) {
        final DiscussionThread thread = getPresenter().getCurrentThread();
        if (thread == null) {
            return new Label("No thread selected");
        }

        final VerticalLayout summaryLayout = new VerticalLayout();

        summaryLayout.setWidth(100.0f, Unit.PERCENTAGE);
        summaryLayout.addStyleName("threadSummary");

        final PostComponent postSummary = newPostSummaryComponent(firstPost);
        postSummary.setVisible(false);

        final String topicXhtml = String
                .format("Thread: <strong>%s</strong> started by <strong>%s</strong> %s",
                        thread.getTopic(), firstPost.getAuthor()
                                .getDisplayedName(), new PrettyTime()
                                .format(firstPost.getTime()));
        final Label topicLabel = new Label(topicXhtml, ContentMode.XHTML);
        topicLabel.setWidth(null);

        final String showPostContentCaption = "Show post content";
        final String hidePostContentCaption = "Hide post content";
        final String collapsedStyle = "collapsed";
        final Label showOrHideLabel = new Label(showPostContentCaption);
        showOrHideLabel.setStyleName("show-or-hide");
        showOrHideLabel.addStyleName(collapsedStyle);
        showOrHideLabel.setWidth(null);
        summaryLayout.addListener(new LayoutClickListener() {

            @Override
            public void layoutClick(final LayoutClickEvent event) {
                postSummary.setVisible(!postSummary.isVisible());
                showOrHideLabel.setValue(postSummary.isVisible() ? hidePostContentCaption
                        : showPostContentCaption);
                if (postSummary.isVisible()) {
                    showOrHideLabel.removeStyleName(collapsedStyle);
                } else {
                    showOrHideLabel.addStyleName(collapsedStyle);
                }
            }
        });

        final CssLayout topRow = new CssLayout();
        topRow.addStyleName("topRow");
        topRow.setWidth("100%");
        topRow.setMargin(true);
        topRow.addComponent(topicLabel);
        topRow.addComponent(showOrHideLabel);

        summaryLayout.addComponent(topRow);
        summaryLayout.addComponent(postSummary);

        return summaryLayout;
    }

    private FloatingBar getSummaryBar(final Post post,
            final PostComponent originalPost) {
        final FloatingBar bar = new FloatingBar();
        bar.addStyleName("threadSummaryBar");
        bar.setContent(getThreadSummary(post));
        return bar;
    }

    private FloatingBar getQuickReplyBar(
            final ReplyComponent mirroredReplyComponent) {
        final ReplyComponent quickReply = new ReplyComponent(replyListener,
                getPresenter().getFormattingSyntax(), "Quick Reply",
                "Your reply...");

        // Using the TextArea of the ReplyComponent as the property data source
        // to keep the two editors in sync.
        quickReply.setUserMayAddFiles(false);
        quickReply.getInput().setPropertyDataSource(
                mirroredReplyComponent.getInput());
        quickReply.setCompactMode(true);
        quickReply.setCollapsible(true);
        quickReply.getInput().addListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(final FocusEvent event) {
                quickReply.setCompactMode(false);
            }
        });
        quickReply.setWidth(100.0f, Unit.PERCENTAGE);

        final FloatingBar bar = new FloatingBar();
        bar.addStyleName("quickReply");
        bar.setAlignment(FloatingAlignment.BOTTOM);
        bar.setContent(quickReply);
        bar.addListener(new VisibilityListener() {

            @Override
            public void onHide(final HideEvent event) {
                // FIXME re-implement
                // quickReply.getInput().blur();
            }

            @Override
            public void onDisplay(final DisplayEvent event) {
                // FIXME re-implement
                // mirroredReplyComponent.getInput().blur();
            }
        });
        return bar;
    }

    @Override
    public void displayThreadNotFoundError(final String threadIdString) {
        getRoot().showNotification("No thread found for " + threadIdString,
                Notification.TYPE_ERROR_MESSAGE);
    }

    @Override
    protected void navigationTo(final String[] arguments) {
        try {
            super.getPresenter().handleArguments(arguments);
        } catch (final DataSourceException e) {
            panic();
        }
    }

    @Override
    public void confirmPostReported() {
        getRoot().showNotification("Post is reported!");
    }

    @Override
    public void confirmBanned() {
        getRoot().showNotification("User is banned");
        reloadPage();
    }

    @Override
    public void confirmFollowingThread() {
        getRoot().showNotification("Following thread");
        swapFollowingMenus();
    }

    @Override
    public void confirmUnFollowingThread() {
        getRoot().showNotification("Not following thread anymore");
        swapFollowingMenus();
    }

    private void swapFollowingMenus() {
        for (final PostComponent c : postsToComponents.values()) {
            c.swapFollowingMenu();
        }
    }

    @Override
    public void confirmPostDeleted() {
        getRoot().showNotification("Post deleted");
        reloadPage();
    }

    /**
     * Resets and redraws the view.
     * 
     * @deprecated Now with the {@link LazyLayout} this <em>really really</em>
     *             should be avoided. Use incremental changes instead whenever
     *             humanly possible, please!
     */
    @Deprecated
    private void reloadPage() {
        try {
            getPresenter().resetView();
        } catch (final DataSourceException e) {
            panic();
        }
    }

    @Override
    public void refreshScores(final Post post, final long newScore) {
        postsToComponents.get(post).refreshScores(newScore);
    }

    @Override
    public void confirmReplyPostedAndShowIt(final Post newPost) {
        postsLayout.addComponentEagerly(newPostComponent(newPost));
    }

    @Override
    public void displayUserCanNotReply() {
        getRoot().showNotification(
                "Unfortunately, you are not allowed to reply to this thread.");
    }

    @Override
    public void displayUserCanNotEdit() {
        getRoot().showNotification(
                "Unfortunately, you are not allowed to edit this post.");
    }

    @Override
    public void redirectToDashboard() {
        getNavigator().navigateTo(
                ToriNavigator.ApplicationView.DASHBOARD.getUrl());
    }

    @Override
    public void displayNewThreadFormFor(final Category category) {
        layout.removeAllComponents();
        final HeadingLabel heading = new HeadingLabel("Start a New Thread",
                HeadingLevel.H2);
        layout.addComponent(heading);
        getRoot().scrollIntoView(heading);

        final HorizontalLayout topicLayout = new HorizontalLayout();
        topicLayout.setSpacing(true);
        topicLayout.setMargin(true, false, true, false);
        topicLayout.setWidth("50em");
        topicLayout.setStyleName("newthread");
        layout.addComponent(topicLayout);

        final HeadingLabel topicLabel = new HeadingLabel("Topic",
                HeadingLevel.H3);
        topicLabel.addStyleName("topiclabel");
        topicLabel.setWidth("153px");
        topicLayout.addComponent(topicLabel);

        final TextField topicField = new TextField();
        topicField.setStyleName("topicfield");
        topicField.setWidth("100%");
        topicLayout.addComponent(topicField);
        topicLayout.setExpandRatio(topicField, 1.0f);
        topicField.focus();

        newThreadComponent = new NewThreadComponent(new NewThreadListener() {
            @Override
            public void submit(final String rawBody) {
                String errorMessages = "";

                final String topic = topicField.getValue();
                if (topic.isEmpty()) {
                    errorMessages += "You need a topic<br/>";
                }
                if (rawBody.isEmpty()) {
                    errorMessages += "You need a thread body<br/>";
                }

                if (errorMessages.isEmpty()) {
                    try {
                        final DiscussionThread createdThread = getPresenter()
                                .createNewThread(category, topic, rawBody);
                        getNavigator().navigateTo(
                                ToriNavigator.ApplicationView.THREADS.getUrl()
                                        + "/" + createdThread.getId());
                    } catch (final DataSourceException e) {
                        getRoot()
                                .showNotification(
                                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                    }
                } else {
                    getRoot().showNotification(errorMessages,
                            Notification.TYPE_HUMANIZED_MESSAGE);
                }
            }

            @Override
            public void addAttachment(final String attachmentFileName,
                    final byte[] data) {
                getPresenter().addAttachment(attachmentFileName, data);
            }

            @Override
            public void resetInput() {
                getPresenter().resetInput();
            }

            @Override
            public void removeAttachment(final String fileName) {
                getPresenter().removeAttachment(fileName);
            }
        }, ToriApplication.getCurrent().getPostFormatter()
                .getFormattingSyntaxXhtml());
        newThreadComponent.setUserMayAddFiles(getPresenter().userMayAddFiles());
        newThreadComponent.setMaxFileSize(getPresenter().getMaxFileSize());
        layout.addComponent(newThreadComponent);
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

    @Override
    public void appendToReply(final String textToAppend) {
        reply.insertIntoMessage(textToAppend);
    }

    @Override
    public void refresh(final Post post) {
        final PostComponent postComponent = postsToComponents.get(post);
        postComponent.refreshBody(post);
    }

    @Override
    public void updateAttachmentList(
            final LinkedHashMap<String, byte[]> attachments) {
        if (reply != null) {
            reply.updateAttachmentList(attachments);
        }
        if (newThreadComponent != null) {
            newThreadComponent.updateAttachmentList(attachments);
        }
    }

}
