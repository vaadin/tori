package org.vaadin.tori.thread;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.ReplyComponent;
import org.vaadin.tori.component.ReplyComponent.ReplyListener;
import org.vaadin.tori.component.post.PostComponent;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;
    private final ReplyListener replyListener = new ReplyListener() {
        @Override
        public void sendReply(final String rawBody) {
            getPresenter().sendReply(rawBody);
        }
    };

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

        for (final Post post : posts) {
            final PostComponent c = new PostComponent(post, getPresenter());
            if (getPresenter().userMayReportPosts()) {
                c.enableReporting();
            }
            if (getPresenter().userMayEdit(post)) {
                c.enableEditing();
            }
            if (getPresenter().userMayQuote(post)) {
                c.enableQuoting();
            }
            layout.addComponent(c);
        }

        if (getPresenter().userMayReply()) {
            layout.addComponent(new HeadingLabel("~~ FIN ~~", HeadingLevel.H3));
            layout.addComponent(new ReplyComponent(replyListener,
                    getPresenter().getFormattingSyntax()));
        }
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
    public void confirmReplyPosted() {
        // TODO make neater
        getWindow().showNotification("Replied!");
    }

    @Override
    public void displayUserCanNotReply() {
        getWindow().showNotification(
                "Unfortunately, you are not allowed to reply to this thread.");
    }
}
