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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.post.PostReport.Reason;
import org.vaadin.tori.util.ToriActivityMessaging.UserAuthoredListener;
import org.vaadin.tori.util.ToriActivityMessaging.UserTypingListener;
import org.vaadin.tori.util.UserBadgeProvider;
import org.vaadin.tori.view.thread.ThreadView.PostData;
import org.vaadin.tori.view.thread.ThreadView.ViewPermissions;

public class ThreadPresenter extends Presenter<ThreadView> implements
        UserTypingListener, UserAuthoredListener {

    public static final String NEW_THREAD_ARGUMENT = "new";

    /** This can be null if the user is visiting a non-existing thread. */

    private DiscussionThread currentThread;

    // private final LinkedHashMap<String, byte[]> attachments = new
    // LinkedHashMap<String, byte[]>();

    public ThreadPresenter(ThreadView view) {
        super(view);
    }

    public PostData getPostData(final Post post, final Long selectedPostId) {
        final long postId = post.getId();
        final long selectedId = selectedPostId != null ? selectedPostId : 0;
        final User author = post.getAuthor();
        final String bodyRaw = post.getBodyRaw();
        return new PostData() {

            @Override
            public long getId() {
                return postId;
            }

            @Override
            public Date getTime() {
                return post.getTime();
            }

            @Override
            public String getAuthorName() {
                return author.getDisplayedName();
            }

            @Override
            public boolean userMayBanAuthor() {
                return authorizationService.mayBan();
            }

            @Override
            public long getThreadId() {
                return post.getThread().getId();
            }

            @Override
            public String getAuthorAvatarUrl() {
                return author.getAvatarUrl();
            }

            @Override
            public long getScore() {
                long result = 9001;
                try {
                    result = dataSource.getPostScore(postId);
                } catch (final DataSourceException e) {
                    log.error(e);
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            public String getFormattedBody(boolean allowHtml) {
                String formattedPost = postFormatter.format(bodyRaw);
                if (!allowHtml) {
                    formattedPost = stripTags(formattedPost);
                }
                return formattedPost;
            }

            @Override
            public boolean isSelected() {
                return postId == selectedId;
            }

            @Override
            public String getRawBody() {
                return bodyRaw;
            }

            @Override
            public boolean hasAttachments() {
                return post.hasAttachments();
            }

            @Override
            public Map<String, String> getAttachments() {
                Map<String, String> attachments = new LinkedHashMap<String, String>();
                for (Attachment attachment : post.getAttachments()) {
                    final String linkCaption = String.format("%s (%s KB)",
                            attachment.getFilename(),
                            attachment.getFileSize() / 1024);
                    attachments.put(attachment.getDownloadUrl(), linkCaption);
                }
                return attachments;
            }

            @Override
            public long getAuthorId() {
                return author.getId();
            }

            @Override
            public Boolean getUpVoted() {
                Boolean result = null;
                try {
                    result = dataSource.getPostVote(postId);
                } catch (DataSourceException e) {
                }
                return result;
            }

            @Override
            public String getBadgeHTML() {
                String result = null;
                final UserBadgeProvider badgeProvider = ToriApiLoader
                        .getCurrent().getUserBadgeProvider();
                if (badgeProvider != null) {
                    result = badgeProvider.getHtmlBadgeFor(author);
                }
                return result;
            }

            @Override
            public boolean isAuthorBanned() {
                return author.isBanned();
            }

            @Override
            public boolean userMayReportPosts() {
                return authorizationService.mayReportPosts();
            }

            @Override
            public boolean userMayEdit() {
                return authorizationService.mayEditPost(postId);
            }

            @Override
            public boolean userMayQuote() {
                return authorizationService.mayReplyInThread(post.getThread()
                        .getId());
            }

            @Override
            public boolean userMayVote() {
                return authorizationService.mayVote();
            }

            @Override
            public boolean userMayDelete() {
                return authorizationService.mayDeletePost(postId);
            }

        };
    }

    public void setCurrentThreadById(final String threadIdString,
            String selectedPostIdString) throws DataSourceException {
        DiscussionThread requestedThread = null;
        try {
            try {
                final long threadId = Long.valueOf(threadIdString);
                requestedThread = dataSource.getThread(threadId);

                if (requestedThread != null) {
                    currentThread = requestedThread;

                    Long selectedPostId = null;
                    if (selectedPostIdString != null) {
                        try {
                            selectedPostId = Long
                                    .parseLong(selectedPostIdString);
                        } catch (final NumberFormatException e) {
                            log.error("Invalid post id format: "
                                    + selectedPostIdString);
                        }
                    }

                    displayPosts(threadId, selectedPostId);
                    view.setViewPermissions(new ViewPermissions() {
                        @Override
                        public boolean mayAddFiles() {
                            return authorizationService
                                    .mayAddFilesInCategory(currentThread
                                            .getCategory().getId());
                        }

                        @Override
                        public int getMaxFileSize() {
                            return dataSource.getAttachmentMaxFileSize();
                        }
                    });

                } else {
                    log.error("requestedthread was null, but no exception was thrown.");
                }

            } catch (final NumberFormatException e) {
                log.error("Invalid thread id format: " + threadIdString);
            } catch (final NoSuchThreadException e) {
                view.showError("Thread not found");
                return;
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

    private void displayPosts(long threadId, Long selectedPostId) {
        List<PostData> posts = new ArrayList<PostData>();
        try {
            for (Post post : dataSource.getPosts(threadId)) {
                posts.add(getPostData(post, selectedPostId));
            }
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
        view.setPosts(posts);
    }

    /**
     * @return <code>null</code> if the URL is invalid.
     */

    public DiscussionThread getCurrentThread() {
        return currentThread;
    }

    public void ban(final long userId) throws DataSourceException {
        try {
            dataSource.banUser(userId);
            view.showNotification("User banned");
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

    public void unban(final long userId) throws DataSourceException {
        try {
            dataSource.unbanUser(userId);
            view.showNotification("User unbanned");
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

    public void delete(long postId) throws DataSourceException {
        try {
            dataSource.deletePost(postId);
            view.showNotification("Post deleted");
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * If the user hasn't upvoted a post, give it an upvote. If that user
     * already has upvoted the post, remove the vote.
     */
    public void upvote(long postId) throws DataSourceException {
        try {
            Boolean vote = getPostVote(postId);
            if (vote == null || !vote) {
                dataSource.upvote(postId);
            } else {
                dataSource.removeUserVote(postId);
            }
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
    public void downvote(final long postId) throws DataSourceException {
        try {
            Boolean vote = getPostVote(postId);
            if (vote == null || vote) {
                dataSource.downvote(postId);
            } else {
                dataSource.removeUserVote(postId);
            }
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public Boolean getPostVote(final long postId) throws DataSourceException {
        try {
            return dataSource.getPostVote(postId);
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

    public void sendReply(final String rawBody, Map<String, byte[]> attachments) {
        try {
            final Post updatedPost = dataSource.saveReply(rawBody, attachments,
                    currentThread.getId());
            dataSource.markRead(updatedPost.getThread());
            messaging.sendUserAuthored(updatedPost.getId(),
                    currentThread.getId());

            view.confirmReplyPostedAndShowIt(updatedPost);
        } catch (final DataSourceException e) {
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
            log.error(e);
            e.printStackTrace();
        }
    }

    public String stripTags(final String html) {
        return html.replaceAll("\\<.*?>", "");
    }

    public void handleArguments(final String[] arguments)
            throws NoSuchThreadException, DataSourceException {
        if (arguments.length > 0) {
            String postId = null;
            if (arguments.length > 1) {
                postId = arguments[1];
            }
            setCurrentThreadById(arguments[0], postId);
        } else {
            log.info("Tried to visit a thread without arguments");
            view.redirectToDashboard();
        }
    }

    public long createNewThread(final Category category, final String topic,
            final String rawBody, Map<String, byte[]> attachments)
            throws DataSourceException {
        try {
            Post post = dataSource.saveNewThread(topic, rawBody, attachments,
                    category.getId());
            messaging.sendUserAuthored(post.getId(), post.getThread().getId());
            return post.getThread().getId();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void quotePost(long postId) {
        try {
            Post post = dataSource.getPost(postId);
            final String quote = postFormatter.getQuote(post);
            view.appendToReply(quote + "\n\n ");
        } catch (DataSourceException e) {
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    public void saveEdited(final long postId, final String newBody) {
        if (authorizationService.mayEditPost(postId)) {
            try {
                dataSource.savePost(postId, newBody);
            } catch (DataSourceException e) {
                view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
                e.printStackTrace();
            }
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
            view.showError("Thread not found");
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

    public void handlePostReport(PostData post, Reason reason,
            String additionalInfo, String postUrl) {
        dataSource.reportPost(post.getId(), reason, additionalInfo, postUrl);
        view.showNotification("Post reported");
    }
}
