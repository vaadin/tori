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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.vaadin.tori.view.thread.ThreadView.ViewData;

public class ThreadPresenter extends Presenter<ThreadView> implements
        UserTypingListener, UserAuthoredListener {

    private DiscussionThread currentThread;

    public ThreadPresenter(ThreadView view) {
        super(view);
    }

    public PostData getPostData(final Post _post, final boolean selected) {
        return new PostData() {
            private final Post post = _post;
            final User author = _post.getAuthor();
            final long postId = _post.getId();

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
            public String getAuthorLink() {
                return author.getUserLink();
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
                String formattedPost = postFormatter.format(post.getBodyRaw());
                if (!allowHtml) {
                    formattedPost = stripTags(formattedPost);
                }
                return formattedPost;
            }

            @Override
            public boolean isSelected() {
                return selected;
            }

            @Override
            public String getRawBody() {
                return post.getBodyRaw();
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

                    view.setViewData(getViewData(currentThread),
                            getAuthoringData());
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
                dataSource.markThreadRead(requestedThread.getId());
            } catch (final DataSourceException e) {
                // Just log the exception, we don't want an exception in view
                // count incrementing or marking as read to stop us here.
                log.error("Couldn't increment view count and "
                        + "mark thread as read.", e);
            }
        }
    }

    private ViewData getViewData(final DiscussionThread currentThread) {
        return new ViewData() {
            @Override
            public boolean mayReplyInThread() {
                return authorizationService.mayReplyInThread(currentThread
                        .getId());
            }

            @Override
            public String getThreadTopic() {
                return currentThread.getTopic();
            }

            @Override
            public Long getThreadId() {
                return currentThread.getId();
            }

            @Override
            public boolean isUserBanned() {
                return dataSource.getCurrentUser().isBanned();
            }
        };
    }

    private AuthoringData getAuthoringData() {
        return new AuthoringData() {
            @Override
            public boolean mayAddFiles() {
                Category category = currentThread.getCategory();
                return authorizationService
                        .mayAddFilesInCategory(category != null ? category
                                .getId() : null);
            }

            @Override
            public int getMaxFileSize() {
                return dataSource.getAttachmentMaxFileSize();
            }

            @Override
            public String getCurrentUserName() {
                return dataSource.getCurrentUser().getDisplayedName();
            }

            @Override
            public String getCurrentUserAvatarUrl() {
                return dataSource.getCurrentUser().getAvatarUrl();
            }
        };
    }

    private void displayPosts(long threadId, Long selectedPostId) {
        List<PostData> posts = new ArrayList<PostData>();
        try {
            for (Post post : dataSource.getPosts(threadId)) {
                posts.add(getPostData(post, selectedPostId != null
                        && selectedPostId == post.getId()));
            }
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
        view.setPosts(posts);
    }

    public DiscussionThread getCurrentThread() {
        return currentThread;
    }

    public void ban(final long userId) {
        try {
            dataSource.banUser(userId);
            view.showNotification("User banned");
            displayPosts(currentThread.getId(), null);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
        }
    }

    public void unban(final long userId) {
        try {
            dataSource.unbanUser(userId);
            view.showNotification("User unbanned");
            displayPosts(currentThread.getId(), null);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
        }
    }

    public void delete(long postId) {
        try {
            dataSource.deletePost(postId);
            view.showNotification("Post deleted");
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
            displayPosts(currentThread.getId(), postId);
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

    private Date startedTyping;

    public void inputValueChanged() {
        if (messaging != null) {
            if (startedTyping == null) {
                startedTyping = new Date();
            }
            messaging.sendUserTyping(currentThread.getId(), startedTyping);
        }
    }

    public void sendReply(final String rawBody,
            Map<String, byte[]> attachments, boolean follow) {
        startedTyping = null;
        try {
            final Post updatedPost = dataSource.saveReply(rawBody, attachments,
                    currentThread.getId());

            if (follow && !dataSource.isFollowingThread(currentThread.getId())) {
                dataSource.followThread(currentThread.getId());
            } else if (!follow
                    && dataSource.isFollowingThread(currentThread.getId())) {
                dataSource.unfollowThread(currentThread.getId());
            }
            messaging.sendUserAuthored(updatedPost.getId(),
                    currentThread.getId());
            view.replySent();
            view.appendPosts(Arrays.asList(getPostData(updatedPost, false)));
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

    public void quotePost(long postId) {
        try {
            Post post = dataSource.getPost(postId);
            final String quote = postFormatter.getQuote(post);
            view.appendQuote(quote);
        } catch (DataSourceException e) {
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    public void saveEdited(final long postId, final String newBody) {
        if (authorizationService.mayEditPost(postId)) {
            try {
                dataSource.savePost(postId, newBody);
                view.updatePost(getPostData(dataSource.getPost(postId), false));
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

    public void handlePostReport(PostData post, Reason reason,
            String additionalInfo, String postUrl) {
        dataSource.reportPost(post.getId(), reason, additionalInfo, postUrl);
        view.showNotification("Post reported");
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

    private final Map<Long, Date[]> pendingReplies = new HashMap<Long, Date[]>();
    private final List<Long> newPosts = new ArrayList<Long>();

    @Override
    public void userTyping(long userId, long threadId, Date startedTyping) {
        if (currentThread.getId() == threadId) {
            pendingReplies
                    .put(userId, new Date[] { startedTyping, new Date() });
        }
        refreshThreadUpdates();
    }

    @Override
    public void userAuthored(long postId, long threadId) {
        if (currentThread.getId() == threadId) {
            newPosts.add(postId);
            try {
                Post post = dataSource.getPost(postId);
                pendingReplies.remove(post.getAuthor().getId());
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        }
        refreshThreadUpdates();
    }

    private void refreshThreadUpdates() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        // User name : user linkeg pairs
        Map<User, Date> pendingReplies = new HashMap<User, Date>();
        for (Entry<Long, Date[]> entry : this.pendingReplies.entrySet()) {
            if (entry.getValue()[1].after(calendar.getTime())) {
                try {
                    User user = dataSource.getToriUser(entry.getKey());
                    pendingReplies.put(user, entry.getValue()[0]);
                } catch (DataSourceException e) {
                    e.printStackTrace();
                }
            }
        }

        view.setThreadUpdates(newPosts.size(), pendingReplies);
    }

    public void showNewPostsRequested() {
        List<PostData> newPostsData = new ArrayList<ThreadView.PostData>();
        for (Long postId : newPosts) {
            try {
                Post post = dataSource.getPost(postId);
                newPostsData.add(getPostData(post, false));
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        }
        view.appendPosts(newPostsData);

        newPosts.clear();
        refreshThreadUpdates();
    }
}
