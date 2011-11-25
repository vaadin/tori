package org.vaadin.tori.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.component.FloatingBar;
import org.vaadin.tori.component.FloatingBar.FloatingAlignment;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.ReplyComponent;
import org.vaadin.tori.component.ReplyComponent.ReplyListener;
import org.vaadin.tori.component.post.PostComponent;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD_STORE", justification = "We're ignoring serialization")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;
    private final ReplyListener replyListener = new ReplyListener() {
        @Override
        public void sendReply(final String rawBody) {
            getPresenter().sendReply(rawBody);
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

    @Override
    public DiscussionThread getCurrentThread() {
        return getPresenter().getCurrentThread();
    }

    @Override
    public void displayPosts(final List<Post> posts) {
        layout.removeAllComponents();
        layout.addComponent(new HeadingLabel(getCurrentThread().getTopic(),
                HeadingLevel.H2));

        postsLayout.removeAllComponents();
        layout.addComponent(postsLayout);

        boolean first = true;
        for (final Post post : posts) {
            final PostComponent c = newPostComponent(post);
            postsLayout.addComponent(c);

            if (first) {
                // create the floating summary bar for the first post
                final FloatingBar summaryBar = getPostSummaryBar(post);
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

            // Add the floating quick reply bar, using the TextArea of the
            // ReplyComponent as the property data source to keep the two
            // editors in sync.
            final FloatingBar quickReplyBar = getQuickReplyBar(reply.getInput());
            quickReplyBar.setScrollComponent(reply);

            layout.addComponent(quickReplyBar);
        }
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
            c.enableUpDownVoting(getPresenter().getPostVote(post));
        }

        // context menu permissions

        if (getPresenter().userCanFollowThread()) {
            c.enableThreadFollowing();
        }
        if (getPresenter().userCanUnFollowThread()) {
            c.enableThreadUnFollowing();
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
        if (getPresenter().userMayQuote(post)) {
            c.enableQuoting();
        }
        return c;
    }

    private FloatingBar getPostSummaryBar(final Post post) {
        final FloatingBar bar = new FloatingBar();
        bar.setContent(newPostSummaryComponent(post));
        return bar;
    }

    private FloatingBar getQuickReplyBar(final Property dataSource) {
        final ReplyComponent quickReply = new ReplyComponent(replyListener,
                getPresenter().getFormattingSyntax());
        quickReply.getInput().setPropertyDataSource(dataSource);
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
        return bar;
    }

    @SuppressWarnings("unused")
    private FloatingBar getQuickReplyBar2(final Property dataSource) {
        final FloatingBar bar = new FloatingBar();
        final HorizontalLayout layout = new HorizontalLayout();
        layout.addStyleName("quickReply");
        layout.setWidth("100%");

        final TextField replyField = new TextField();
        replyField.setReadOnly(true);
        replyField.setPropertyDataSource(dataSource);
        replyField.setWidth("100%");
        replyField.addListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(final FocusEvent event) {
                final ReplyComponent expandedMode = new ReplyComponent(
                        replyListener, getPresenter().getFormattingSyntax());
                expandedMode.getInput().setPropertyDataSource(dataSource);
                bar.setContent(expandedMode);
            }
        });
        final Label replyLabel = new Label("Your Reply");
        replyLabel.setWidth(null);
        layout.addComponent(replyLabel);
        layout.addComponent(replyField);
        layout.setExpandRatio(replyField, 1.0f);
        bar.setContent(layout);
        return bar;
    }

    @Override
    public void displayThreadNotFoundError(final String threadIdString) {
        getWindow().showNotification("No thread found for " + threadIdString,
                Notification.TYPE_ERROR_MESSAGE);
    }

    @Override
    protected void navigationTo(final String requestedDataId) {
        super.getPresenter().setCurrentThreadById(requestedDataId);
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
        displayPosts(getPresenter().getCurrentThread().getPosts());
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
}
