package org.vaadin.tori.thread;

import java.util.HashMap;
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

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("serial")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD_STORE", justification = "We're ignoring serialization")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;
    private final ReplyListener replyListener = new ReplyListener() {
        @Override
        public void submit(final String rawBody) {
            if (!rawBody.trim().isEmpty()) {
                try {
                    getPresenter().sendReply(rawBody);
                } catch (final DataSourceException e) {
                    getApplication().getMainWindow().showNotification(
                            DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                }
            }
        }
    };

    private final Map<Post, PostComponent> postsToComponents = new HashMap<Post, PostComponent>();
    private final CssLayout postsLayout = new CssLayout();

    public ThreadViewImpl() {
        setStyleName("threadview");
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

        boolean first = true;
        for (final Post post : posts) {
            final PostComponent c = newPostComponent(post);
            postsLayout.addComponent(c);

            if (first) {
                // create the floating summary bar for the first post
                final FloatingBar summaryBar = getPostSummaryBar(post, c);
                summaryBar.setScrollComponent(c);
                postsLayout.addComponent(summaryBar);
                first = false;
            }

        }

        if (getPresenter().userMayReply()) {
            final Label spacer = new Label("~~ FIN ~~");
            spacer.setStyleName("spacer");
            layout.addComponent(spacer);

            final ReplyComponent reply = new ReplyComponent(replyListener,
                    getPresenter().getFormattingSyntax());
            layout.addComponent(reply);

            final FloatingBar quickReplyBar = getQuickReplyBar(reply);
            quickReplyBar.setScrollComponent(reply);

            layout.addComponent(quickReplyBar);
        }

        final Label bottomSpacer = new Label("~~~~~~~~");
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
            // NOP - everything's logged. I fyou can't follow, you can't
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
        final PostComponent c = new PostComponent(post, getPresenter(), false);
        c.addStyleName("summary");
        if (getPresenter().userMayQuote(post)) {
            c.enableQuoting();
        }
        return c;
    }

    private FloatingBar getPostSummaryBar(final Post post,
            final PostComponent originalPost) {
        final PostComponent summary = newPostSummaryComponent(post);
        summary.setScrollToComponent(originalPost);

        final FloatingBar bar = new FloatingBar();
        bar.setContent(summary);
        return bar;
    }

    private FloatingBar getQuickReplyBar(
            final ReplyComponent mirroredReplyComponent) {
        final ReplyComponent quickReply = new ReplyComponent(replyListener,
                getPresenter().getFormattingSyntax());

        // Using the TextArea of the ReplyComponent as the property data source
        // to keep the two editors in sync.
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

        final FloatingBar bar = new FloatingBar();
        bar.setAlignment(FloatingAlignment.BOTTOM);
        bar.setContent(quickReply);
        bar.addListener(new VisibilityListener() {

            @Override
            public void onHide(final HideEvent event) {
                quickReply.getInput().blur();
            }

            @Override
            public void onDisplay(final DisplayEvent event) {
                mirroredReplyComponent.getInput().blur();
            }
        });
        return bar;
    }

    @Override
    public void displayThreadNotFoundError(final String threadIdString) {
        getWindow().showNotification("No thread found for " + threadIdString,
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
        getWindow().showNotification("Post is reported!");
    }

    @Override
    public void confirmBanned() {
        getWindow().showNotification("User is banned");
        reloadPage();
    }

    @Override
    public void confirmFollowingThread() {
        getWindow().showNotification("Following thread");
        swapFollowingMenus();
    }

    @Override
    public void confirmUnFollowingThread() {
        getWindow().showNotification("Not following thread anymore");
        swapFollowingMenus();
    }

    private void swapFollowingMenus() {
        for (final PostComponent c : postsToComponents.values()) {
            c.swapFollowingMenu();
        }
    }

    @Override
    public void confirmPostDeleted() {
        getWindow().showNotification("Post deleted");
        reloadPage();
    }

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
    public void confirmReplyPosted() {
        getWindow().showNotification("Replied!");
        reloadPage();
    }

    @Override
    public void displayUserCanNotReply() {
        getWindow().showNotification(
                "Unfortunately, you are not allowed to reply to this thread.");
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
        ToriApplication.getCurrent().getMainWindow().scrollIntoView(heading);

        final HorizontalLayout topicLayout = new HorizontalLayout();
        topicLayout.setSpacing(true);
        topicLayout.setMargin(true, false, true, false);
        topicLayout.setWidth("50em");
        topicLayout.setStyleName("newthread");
        layout.addComponent(topicLayout);

        final HeadingLabel topicLabel = new HeadingLabel("Topic:",
                HeadingLevel.H3);
        topicLabel.addStyleName("topiclabel");
        topicLabel.setWidth("140px");
        topicLayout.addComponent(topicLabel);

        final TextField topicField = new TextField();
        topicField.setStyleName("topicfield");
        topicField.setWidth("100%");
        topicLayout.addComponent(topicField);
        topicLayout.setExpandRatio(topicField, 1.0f);

        layout.addComponent(new NewThreadComponent(new NewThreadListener() {
            @Override
            public void submit(final String rawBody) {
                String errorMessages = "";

                final String topic = (String) topicField.getValue();
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
                        getApplication()
                                .getMainWindow()
                                .showNotification(
                                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                    }
                } else {
                    ToriApplication
                            .getCurrent()
                            .getMainWindow()
                            .showNotification(errorMessages,
                                    Notification.TYPE_HUMANIZED_MESSAGE);
                }
            }
        }, ToriApplication.getCurrent().getPostFormatter()
                .getFormattingSyntaxXhtml()));
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }
}
