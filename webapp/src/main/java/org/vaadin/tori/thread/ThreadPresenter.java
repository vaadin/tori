package org.vaadin.tori.thread;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.post.PostReport;

public class ThreadPresenter extends Presenter<ThreadView> {

    private DiscussionThread currentThread;

    public ThreadPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentThreadById(final String threadIdString) {
        DiscussionThread requestedThread = null;
        try {
            final long threadId = Long.valueOf(threadIdString);
            requestedThread = dataSource.getThread(threadId);
        } catch (final NumberFormatException e) {
            log.error("Invalid thread id format: " + threadIdString);
        }

        if (requestedThread != null) {
            currentThread = requestedThread;
            final ThreadView view = getView();
            view.displayPosts(currentThread.getPosts());
        } else {
            getView().displayThreadNotFoundError(threadIdString);
        }
    }

    public DiscussionThread getCurrentThread() {
        return currentThread;
    }

    public void handlePostReport(final PostReport report) {
        dataSource.reportPost(report);
        getView().confirmPostReported();
    }

    public boolean userMayReportPosts() {
        return authorizationService.mayReportPosts();
    }

    public boolean userMayEdit(final Post post) {
        return authorizationService.mayEdit(post);
    }

    public boolean userMayQuote(final Post post) {
        return authorizationService.mayReplyIn(currentThread);
    }

    public void ban(final User user) {
        dataSource.ban(user);
        getView().confirmBanned();
    }

    public boolean userMayBan() {
        return authorizationService.mayBan();
    }

    public void followThread() {
        dataSource.follow(currentThread);
        getView().confirmFollowingThread();
    }

    public void unFollowThread() {
        dataSource.unFollow(currentThread);
        getView().confirmUnFollowingThread();
    }

    public boolean userCanFollowThread() {
        return authorizationService.mayFollow(currentThread)
                && !dataSource.isFollowing(currentThread);
    }

    public boolean userCanUnFollowThread() {
        // TODO Auto-generated method stub
        return false;
    }
}
