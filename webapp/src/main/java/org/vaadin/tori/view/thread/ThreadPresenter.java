/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.view.thread;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.post.PostReport;
import org.vaadin.tori.util.ToriActivityMessaging.UserAuthoredListener;
import org.vaadin.tori.util.ToriActivityMessaging.UserTypingListener;

public class ThreadPresenter extends Presenter<ThreadView> implements
        UserTypingListener, UserAuthoredListener {

    public static final String NEW_THREAD_ARGUMENT = "new";

    /** This can be null if the user is visiting a non-existing thread. */

    private DiscussionThread currentThread;
    private Category categoryWhileCreatingNewThread;
    private final LinkedHashMap<String, byte[]> attachments = new LinkedHashMap<String, byte[]>();

    public ThreadPresenter(ThreadView view) {
        super(view);
    }

    public void setCurrentThreadById(final String threadIdString,
            String selectedPostIdString) throws DataSourceException {
        DiscussionThread requestedThread = null;
        try {
            try {
                final long threadId = Long.valueOf(threadIdString);
                requestedThread = dataSource.getThread(threadId);
            } catch (final NumberFormatException e) {
                log.error("Invalid thread id format: " + threadIdString);
            } catch (final NoSuchThreadException e) {
                view.displayThreadNotFoundError(threadIdString);
                return;
            }

            if (requestedThread != null) {
                currentThread = requestedThread;

                Long selectedPostId = null;
                if (selectedPostIdString != null) {
                    try {
                        selectedPostId = Long.parseLong(selectedPostIdString);
                    } catch (final NumberFormatException e) {
                        log.error("Invalid post id format: "
                                + selectedPostIdString);
                    }
                }

                view.displayPosts(dataSource.getPosts(requestedThread),
                        selectedPostId);
            } else {
                log.error("requestedthread was null, but no exception was thrown.");
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

    public DiscussionThread getCurrentThread() {
        return currentThread;
    }

    public void handlePostReport(final PostReport report)
            throws DataSourceException {
        try {
            dataSource.reportPost(report);
            view.confirmPostReported();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayReportPosts() {
        return authorizationService.mayReportPosts();
    }

    public boolean userMayEdit(final Post post) {
        return authorizationService.mayEdit(post);
    }

    public boolean userMayQuote(final Post post) {
        return currentThread != null
                && authorizationService.mayReplyInThread(currentThread.getId());
    }

    public void ban(final User user) throws DataSourceException {
        try {
            dataSource.ban(user);
            view.confirmBanned(user);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

    public void unban(final User user) throws DataSourceException {
        try {
            dataSource.unban(user);
            view.confirmUnbanned(user);
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
        if (currentThread == null) {
            return;
        }
        try {
            dataSource.followThread(currentThread.getId());
            view.confirmFollowingThread();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void unFollowThread() throws DataSourceException {
        if (currentThread == null) {
            return;
        }
        try {
            dataSource.unfollowThread(currentThread.getId());
            view.confirmUnFollowingThread();
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
        return currentThread != null
                && authorizationService.mayFollowThread(currentThread.getId())
                && !dataSource.isFollowingThread(currentThread.getId());
    }

    /**
     * returns <code>true</code> iff the user currently follows this thread, and
     * can follow threads.
     */
    public boolean userCanUnFollowThread() throws DataSourceException {
        return currentThread != null
                && authorizationService.mayFollowThread(currentThread.getId())
                && dataSource.isFollowingThread(currentThread.getId());

    }

    public void delete(final Post post) throws DataSourceException {
        try {
            dataSource.delete(post);
            view.confirmPostDeleted();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayDelete(final Post post) {
        return authorizationService.mayDelete(post);
    }

    public boolean userMayVote() {
        return authorizationService.mayVote();
    }

    /**
     * If the user hasn't upvoted a post, give it an upvote. If that user
     * already has upvoted the post, remove the vote.
     */
    public void upvote(final Post post) throws DataSourceException {
        try {
            if (!getPostVote(post).isUpvote()) {
                dataSource.upvote(post);
            } else {
                dataSource.removeUserVote(post);
            }
            final long newScore = dataSource.getScore(post);
            view.refreshScores(post, newScore);
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
    public void downvote(final Post post) throws DataSourceException {
        try {
            if (!getPostVote(post).isDownvote()) {
                dataSource.downvote(post);
            } else {
                dataSource.removeUserVote(post);
            }
            final long newScore = dataSource.getScore(post);
            view.refreshScores(post, newScore);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public PostVote getPostVote(final Post post) throws DataSourceException {
        try {
            return dataSource.getPostVote(post);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public long getScore(final Post post) throws DataSourceException {
        try {
            return dataSource.getScore(post);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void inputValueChanged() {
        if (messaging != null) {
            messaging.sendUserTyping(currentThread.getId());
        }
    }

    public boolean userMayReply() {
        return currentThread != null
                && authorizationService.mayReplyInThread(currentThread.getId());
    }

    public final boolean userMayAddFiles() {
        boolean mayAddFiles;
        if (currentThread == null) {
            mayAddFiles = authorizationService
                    .mayAddFilesInCategory(categoryWhileCreatingNewThread
                            .getId());
        } else {
            mayAddFiles = authorizationService
                    .mayAddFilesInCategory(currentThread.getCategory().getId());
        }
        return mayAddFiles;
    }

    public final int getMaxFileSize() {
        return dataSource.getAttachmentMaxFileSize();
    }

    public final void resetInput() {
        attachments.clear();
        view.updateAttachmentList(attachments);
    }

    public final void addAttachment(final String fileName, final byte[] data) {
        attachments.put(fileName, data);
        view.updateAttachmentList(attachments);
    }

    public final void removeAttachment(final String fileName) {
        attachments.remove(fileName);
        view.updateAttachmentList(attachments);
    }

    public void sendReply(final String rawBody) throws DataSourceException {
        if (userMayReply()) {
            try {
                final Post post = new Post();
                post.setAuthor(null);
                post.setBodyRaw(rawBody);
                post.setThread(currentThread);
                post.setTime(new Date());

                final Post updatedPost = dataSource.saveAsCurrentUser(post,
                        attachments);
                dataSource.markRead(updatedPost.getThread());
                messaging.sendUserAuthored(updatedPost.getId(),
                        currentThread.getId());
                resetInput();

                view.confirmReplyPostedAndShowIt(updatedPost);
            } catch (final DataSourceException e) {
                log.error(e);
                e.printStackTrace();
                throw e;
            }
        } else {
            view.displayUserCanNotReply();
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
                view.displayPosts(dataSource.getPosts(thread), null);
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

    public String stripTags(final String html) {
        return html.replaceAll("\\<.*?>", "");
    }

    public void handleArguments(final String[] arguments)
            throws NoSuchThreadException, DataSourceException {
        if (arguments.length > 0) {
            if (!arguments[0].equals(NEW_THREAD_ARGUMENT)) {
                String postId = null;
                if (arguments.length > 1) {
                    postId = arguments[1];
                }
                setCurrentThreadById(arguments[0], postId);
                return;
            } else if (arguments.length > 1) {
                try {
                    final Category category = dataSource.getCategory(Long
                            .parseLong(arguments[1]));

                    if (category != null) {
                        categoryWhileCreatingNewThread = category;

                        view.displayNewThreadFormFor(category);
                        return;
                    }
                } catch (final NumberFormatException IGNORE) {
                }
            }
        } else {
            log.info("Tried to visit a thread without arguments");
        }

        /*
         * if some error occurred that really shouldn't have, just redirect back
         * to dashboard.
         */
        view.redirectToDashboard();

    }

    public DiscussionThread createNewThread(final Category category,
            final String topic, final String rawBody)
            throws DataSourceException {
        try {
            final DiscussionThread thread = new DiscussionThread(topic);
            thread.setCategory(category);

            final Post post = new Post();
            post.setBodyRaw(rawBody);
            post.setTime(new Date());

            thread.setPosts(Arrays.asList(post));
            post.setThread(thread);

            DiscussionThread discussionThread = dataSource.saveNewThread(
                    thread, attachments, post);
            messaging.sendUserAuthored(post.getId(), discussionThread.getId());
            return discussionThread;
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public Category getCurrentCategory() {
        if (currentThread != null) {
            return currentThread.getCategory();
        } else {
            return categoryWhileCreatingNewThread;
        }
    }

    public void quotePost(final Post post) {
        final String quote = postFormatter.getQuote(post);
        view.appendToReply(quote + "\n\n ");
    }

    public void saveEdited(final Post post, final String newBody)
            throws DataSourceException {
        if (authorizationService.mayEdit(post)) {
            // FIXME: umm, maybe we should clone this first?
            post.setBodyRaw(newBody);
            dataSource.save(post);
            view.refresh(post);
        } else {
            view.displayUserCanNotEdit();
        }
    }

    public String getThreadTopic() {
        if (currentThread != null) {
            return currentThread.getTopic();
        } else {
            return "?";
        }
    }

    @Override
    public void navigationFrom() {
        if (messaging != null) {
            messaging.removeUserAuthoredListener(this);
            messaging.removeUserTypingListener(this);
        }
    }

    @Override
    public void navigationTo(String[] args) {
        if (messaging != null) {
            messaging.addUserAuthoredListener(this);
            messaging.addUserTypingListener(this);
        }

        try {
            handleArguments(args);
        } catch (final NoSuchThreadException e) {
            view.displayThreadNotFoundError(String.valueOf(e.getThreadId()));
        } catch (final DataSourceException e) {
            view.panic();
        }
    }

    @Override
    public void userTyping(long userId, long threadId) {
        if (currentThread.getId() == threadId) {
            try {
                view.otherUserTyping(dataSource.getToriUser(userId));
            } catch (DataSourceException e) {
                log.error(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void userAuthored(long postId, long threadId) {
        if (currentThread.getId() == threadId) {
            try {
                view.otherUserAuthored(dataSource.getPost(postId));
            } catch (DataSourceException e) {
                log.error(e);
                e.printStackTrace();
            }
        }
    }
}
