package org.vaadin.tori.thread;

import java.util.Date;
import java.util.LinkedHashMap;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
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
    private final LinkedHashMap<String, byte[]> attachments = new LinkedHashMap<String, byte[]>();

    public ThreadPresenter(final @NonNull DataSource dataSource,
            final @NonNull AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentThreadById(final @NonNull String threadIdString)
            throws DataSourceException {
        DiscussionThread requestedThread = null;
        try {
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
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

        if (requestedThread != null) {
            try {
                dataSource.incrementViewCount(requestedThread);
                dataSource.markRead(requestedThread);
            } catch (final DataSourceException e) {
                // Just log the exception, we don't want an exception in view
                // count incrementing or marking as read to stop us here.
                log.error("Couldn't increment view count and "
                        + "mark thread as read.", e);
            }
        }
    }

    /**
     * @return <code>null</code> if the URL is invalid.
     */
    @CheckForNull
    public DiscussionThread getCurrentThread() {
        return currentThread;
    }

    public void handlePostReport(final @NonNull PostReport report)
            throws DataSourceException {
        try {
            dataSource.reportPost(report);
            getView().confirmPostReported();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

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

    public void ban(final @NonNull User user) throws DataSourceException {
        try {
            dataSource.ban(user);
            getView().confirmBanned();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayBan() {
        return authorizationService.mayBan();
    }

    public void followThread() throws DataSourceException {
        try {
            dataSource.follow(currentThread);
            getView().confirmFollowingThread();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void unFollowThread() throws DataSourceException {
        try {
            dataSource.unFollow(currentThread);
            getView().confirmUnFollowingThread();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * returns <code>true</code> iff the user doesn't currently follow this
     * thread, and can follow threads.
     */
    public boolean userCanFollowThread() throws DataSourceException {
        try {
            return authorizationService.mayFollow(currentThread)
                    && !dataSource.isFollowing(currentThread);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * returns <code>true</code> iff the user currently follows this thread, and
     * can follow threads.
     */
    public boolean userCanUnFollowThread() throws DataSourceException {
        try {
            return authorizationService.mayFollow(currentThread)
                    && dataSource.isFollowing(currentThread);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void delete(final @NonNull Post post) throws DataSourceException {
        try {
            dataSource.delete(post);
            getView().confirmPostDeleted();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayDelete(final @NonNull Post post) {
        return authorizationService.mayDelete(post);
    }

    public boolean userMayVote() {
        return authorizationService.mayVote();
    }

    /**
     * If the user hasn't upvoted a post, give it an upvote. If that user
     * already has upvoted the post, remove the vote.
     */
    public void upvote(final @NonNull Post post) throws DataSourceException {
        try {
            if (!getPostVote(post).isUpvote()) {
                dataSource.upvote(post);
            } else {
                dataSource.removeUserVote(post);
            }
            final long newScore = dataSource.getScore(post);
            getView().refreshScores(post, newScore);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * if the user hasn't downvoted a post, give it a downvote. Otherwise,
     * remove this user's downvote.
     */
    public void downvote(final @NonNull Post post) throws DataSourceException {
        try {
            if (!getPostVote(post).isDownvote()) {
                dataSource.downvote(post);
            } else {
                dataSource.removeUserVote(post);
            }
            final long newScore = dataSource.getScore(post);
            getView().refreshScores(post, newScore);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    @NonNull
    public PostVote getPostVote(final @NonNull Post post)
            throws DataSourceException {
        try {
            return dataSource.getPostVote(post);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public long getScore(final @NonNull Post post) throws DataSourceException {
        try {
            return dataSource.getScore(post);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayReply() {
        return authorizationService.mayReplyIn(currentThread);
    }

    public final boolean userMayAddFiles() {
        boolean mayAddFiles;
        if (currentThread == null) {
            mayAddFiles = authorizationService
                    .mayAddFiles(categoryWhileCreatingNewThread);
        } else {
            mayAddFiles = authorizationService.mayAddFiles(currentThread
                    .getCategory());
        }
        return mayAddFiles;
    }

    public final int getMaxFileSize() {
        return dataSource.getAttachmentMaxFileSize();
    }

    @NonNull
    public String getFormattingSyntax() {
        return ToriApplication.getCurrent().getPostFormatter()
                .getFormattingSyntaxXhtml();
    }

    public final void resetInput() {
        attachments.clear();
        getView().updateAttachmentList(attachments);
    }

    public final void addAttachment(final String fileName, final byte[] data) {
        attachments.put(fileName, data);
        getView().updateAttachmentList(attachments);
    }

    public final void removeAttachment(final String fileName) {
        attachments.remove(fileName);
        getView().updateAttachmentList(attachments);
    }

    public void sendReply(final @NonNull String rawBody)
            throws DataSourceException {
        if (userMayReply()) {
            try {
                final Post post = new Post();
                post.setAuthor(null);
                post.setBodyRaw(rawBody);
                post.setThread(currentThread);
                post.setTime(new Date());

                final Post updatedPost = dataSource.saveAsCurrentUser(post,
                        attachments);

                resetInput();

                getView().confirmReplyPostedAndShowIt(updatedPost);
            } catch (final DataSourceException e) {
                log.error(e);
                e.printStackTrace();
                throw e;
            }
        } else {
            getView().displayUserCanNotReply();
        }
    }

    /**
     * @throws DataSourceException
     * @throws IllegalStateException
     *             if {@link #currentThread} is <code>null</code>.
     */
    public void resetView() throws DataSourceException {
        try {
            /*
             * findbugs doesn't understand the nullcheck for field references,
             * so making it a local field instead. Probably because of possible
             * multithread stuff.
             */
            final DiscussionThread thread = currentThread;

            if (thread != null) {
                getView().displayPosts(dataSource.getPosts(thread), thread);
            } else {
                throw new IllegalStateException(
                        "This method may not be called while currentThread is null");
            }
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public String stripTags(final @NonNull String html) {
        return html.replaceAll("\\<.*?>", "");
    }

    public void handleArguments(final @NonNull String[] arguments)
            throws DataSourceException {
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

        /*
         * if some error occurred that really shouldn't have, just redirect back
         * to dashboard.
         */
        getView().redirectToDashboard();

    }

    private boolean categoryExists(final @NonNull String string)
            throws DataSourceException {
        try {
            final long categoryId = Long.parseLong(string);
            return dataSource.getCategory(categoryId) != null;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @NonNull
    public DiscussionThread createNewThread(final @NonNull Category category,
            final @NonNull String topic, final @NonNull String rawBody)
            throws DataSourceException {
        try {
            final DiscussionThread thread = new DiscussionThread(topic);
            thread.setCategory(category);

            final Post post = new Post();
            post.setBodyRaw(rawBody);
            post.setTime(new Date());

            thread.setPosts(Lists.newArrayList(post));
            post.setThread(thread);

            return dataSource.saveNewThread(thread, attachments, post);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    @CheckForNull
    public Category getCurrentCategory() {
        if (currentThread != null) {
            return currentThread.getCategory();
        } else {
            return categoryWhileCreatingNewThread;
        }
    }

    public void quotePost(final Post post) {
        final String quote = ToriApplication.getCurrent().getPostFormatter()
                .getQuote(post);
        getView().appendToReply(quote);
    }

    public void saveEdited(final Post post, final String newBody)
            throws DataSourceException {
        if (authorizationService.mayEdit(post)) {
            // FIXME: umm, maybe we should clone this first?
            post.setBodyRaw(newBody);
            dataSource.save(post);
            getView().refresh(post);
        } else {
            getView().displayUserCanNotEdit();
        }
    }

}
