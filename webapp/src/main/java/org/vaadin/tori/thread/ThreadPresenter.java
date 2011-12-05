package org.vaadin.tori.thread;

import java.util.Date;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.post.PostReport;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class ThreadPresenter extends Presenter<ThreadView> {
    @CheckForNull
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
            view.displayPosts(dataSource.getPosts(requestedThread),
                    requestedThread);
        } else {
            getView().displayThreadNotFoundError(threadIdString);
        }
    }

    /**
     * @return <code>null</code> if the URL is invalid.
     */
    @CheckForNull
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
        return authorizationService.mayFollow(currentThread)
                && dataSource.isFollowing(currentThread);
    }

    public void delete(final Post post) {
        dataSource.delete(post);
        getView().confirmPostDeleted();
    }

    public boolean userMayDelete(final Post post) {
        return authorizationService.mayDelete(post);
    }

    public boolean userMayVote() {
        return authorizationService.mayVote();
    }

    public void upvote(final Post post) {
        if (!getPostVote(post).isUpvote()) {
            dataSource.upvote(post);
        } else {
            dataSource.removeUserVote(post);
        }
        final long newScore = dataSource.getScore(post);
        getView().refreshScores(post, newScore);
    }

    public void downvote(final Post post) {
        if (!getPostVote(post).isDownvote()) {
            dataSource.downvote(post);
        } else {
            dataSource.removeUserVote(post);
        }
        final long newScore = dataSource.getScore(post);
        getView().refreshScores(post, newScore);
    }

    public void unvote(final Post post) {
        dataSource.removeUserVote(post);
        final long newScore = dataSource.getScore(post);
        getView().refreshScores(post, newScore);
    }

    public PostVote getPostVote(final Post post) {
        return dataSource.getPostVote(post);
    }

    public long getScore(final Post post) {
        return dataSource.getScore(post);
    }

    public boolean userMayReply() {
        return authorizationService.mayReplyIn(currentThread);
    }

    public String getFormattingSyntax() {
        return ToriApplication.getCurrent().getPostFormatter()
                .getFormattingSyntaxXhtml();
    }

    public void sendReply(final String rawBody) {

        if (userMayReply()) {
            final Post post = new Post();
            post.setAuthor(null);
            post.setBodyRaw(rawBody);
            post.setThread(currentThread);
            post.setTime(new Date());
            dataSource.saveAsCurrentUser(post);
            getView().confirmReplyPosted();
        } else {
            getView().displayUserCanNotReply();
        }
        resetView();
    }

    /**
     * @throws IllegalStateException
     *             if {@link #currentThread} is <code>null</code>.
     */
    public void resetView() {
        /*
         * findbugs doesn't understand the nullcheck for field references, so
         * making it a local field instead. Probably because of possible
         * multithread stuff.
         */
        final DiscussionThread thread = currentThread;

        if (thread != null) {
            getView().displayPosts(dataSource.getPosts(thread), thread);
        } else {
            throw new IllegalStateException(
                    "This method may not be called while currentThread is null");
        }
    }

    public String stripTags(final String html) {
        return html.replaceAll("\\<.*?>", "");
    }

}
