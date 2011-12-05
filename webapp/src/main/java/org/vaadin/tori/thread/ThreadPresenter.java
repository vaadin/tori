package org.vaadin.tori.thread;

import java.util.Date;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.post.PostReport;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class ThreadPresenter extends Presenter<ThreadView> {

    public static final String NEW_THREAD_ARGUMENT = "new";

    private DiscussionThread currentThread;
    private Category categoryWhileCreatingNewThread;

    public ThreadPresenter(final @NonNull DataSource dataSource,
            final @NonNull AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentThreadById(final @NonNull String threadIdString) {
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

    public void handlePostReport(final @NonNull PostReport report) {
        dataSource.reportPost(report);
        getView().confirmPostReported();
    }

    public boolean userMayReportPosts() {
        return authorizationService.mayReportPosts();
    }

    public boolean userMayEdit(final @NonNull Post post) {
        return authorizationService.mayEdit(post);
    }

    public boolean userMayQuote(final @NonNull Post post) {
        return authorizationService.mayReplyIn(currentThread);
    }

    public void ban(final @NonNull User user) {
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

    public void delete(final @NonNull Post post) {
        dataSource.delete(post);
        getView().confirmPostDeleted();
    }

    public boolean userMayDelete(final @NonNull Post post) {
        return authorizationService.mayDelete(post);
    }

    public boolean userMayVote() {
        return authorizationService.mayVote();
    }

    public void upvote(final @NonNull Post post) {
        if (!getPostVote(post).isUpvote()) {
            dataSource.upvote(post);
        } else {
            dataSource.removeUserVote(post);
        }
        final long newScore = dataSource.getScore(post);
        getView().refreshScores(post, newScore);
    }

    public void downvote(final @NonNull Post post) {
        if (!getPostVote(post).isDownvote()) {
            dataSource.downvote(post);
        } else {
            dataSource.removeUserVote(post);
        }
        final long newScore = dataSource.getScore(post);
        getView().refreshScores(post, newScore);
    }

    public void unvote(final @NonNull Post post) {
        dataSource.removeUserVote(post);
        final long newScore = dataSource.getScore(post);
        getView().refreshScores(post, newScore);
    }

    @NonNull
    public PostVote getPostVote(final @NonNull Post post) {
        return dataSource.getPostVote(post);
    }

    public long getScore(final @NonNull Post post) {
        return dataSource.getScore(post);
    }

    public boolean userMayReply() {
        return authorizationService.mayReplyIn(currentThread);
    }

    @NonNull
    public String getFormattingSyntax() {
        return ToriApplication.getCurrent().getPostFormatter()
                .getFormattingSyntaxXhtml();
    }

    public void sendReply(final @NonNull String rawBody) {

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

    public String stripTags(final @NonNull String html) {
        return html.replaceAll("\\<.*?>", "");
    }

    public void handleArguments(final @NonNull String[] arguments) {
        if (arguments.length > 0) {
            if (!arguments[0].equals(NEW_THREAD_ARGUMENT)) {
                setCurrentThreadById(arguments[0]);
                return;
            } else if (arguments.length > 1 && categoryExists(arguments[1])) {
                final Category category = dataSource.getCategory(Long
                        .parseLong(arguments[1]));
                categoryWhileCreatingNewThread = category;
                getView().displayNewThreadFormFor(category);
                return;
            }
        } else {
            log.info("Tried to visit a thread without arguments");
        }

        // if some error occurred that really shouldn't have, just redirect back
        // to dashboard.
        getView().redirectToDashboard();
    }

    private boolean categoryExists(final @NonNull String string) {
        try {
            final long categoryId = Long.parseLong(string);
            return dataSource.getCategory(categoryId) != null;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    public @NonNull
    DiscussionThread createNewThread(final @NonNull Category category,
            final @NonNull String topic, final @NonNull String rawBody) {
        final DiscussionThread thread = new DiscussionThread(topic);
        thread.setCategory(category);

        final Post post = new Post();
        post.setBodyRaw(rawBody);
        post.setTime(new Date());

        thread.setPosts(Lists.newArrayList(post));
        post.setThread(thread);

        return dataSource.saveNewThread(thread, post);
    }

    @CheckForNull
    public Category getCurrentCategory() {
        if (currentThread != null) {
            return currentThread.getCategory();
        } else {
            return categoryWhileCreatingNewThread;
        }
    }
}
