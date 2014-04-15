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

import org.vaadin.tori.Configuration;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.FileNameException;
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
    private final Configuration configuration;

    public ThreadPresenter(final ThreadView view) {
        super(view);
        configuration = dataSource.getConfiguration();
    }

    public PostData getPostData(final Post _post) {
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
                return !author.isAnonymous() && authorizationService.mayBan();
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
            public String getFormattedBody(final boolean allowHtml) {
                return ThreadPresenter.this.getFormattedBody(post, allowHtml);
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
                return !author.isAnonymous()
                        && authorizationService.mayReportPosts();
            }

            @Override
            public boolean userMayEdit() {
                return authorizationService.mayEditPost(postId);
            }

            @Override
            public boolean userMayQuote() {
                return authorizationService.mayReplyInThread(post.getThread()
                        .getId()) && post.isFormatBBCode();
            }

            @Override
            public boolean userMayVote() {
                return authorizationService.mayVote();
            }

            @Override
            public boolean userMayDelete() {
                return authorizationService.mayDeletePost(postId);
            }

            @Override
            public boolean isFormatBBCode() {
                return post.isFormatBBCode();
            }

            @Override
            public boolean userMayView() {
                return authorizationService.mayViewPost(postId);
            }

        };
    }

    private String getFormattedBody(final Post post, final boolean allowHtml) {
        Map<String, String> postReplacements = configuration.getReplacements();
        boolean replaceMessageBoardsLinks = configuration
                .isReplaceMessageBoardsLinks();
        String formattedPost = postFormatter.format(post, postReplacements,
                replaceMessageBoardsLinks);
        if (!allowHtml) {
            formattedPost = stripTags(formattedPost);
        }
        return formattedPost;
    }

    public void setCurrentThreadById(final String threadIdString,
            final String selectedPostIdString) throws DataSourceException {
        DiscussionThread requestedThread = null;
        try {
            try {
                final long threadId = Long.valueOf(threadIdString);
                requestedThread = dataSource.getThread(threadId);

                if (requestedThread != null) {
                    currentThread = requestedThread;

                    if (authorizationService.mayViewThread(threadId)) {
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

                        view.setViewData(getViewData(currentThread),
                                getAuthoringData());

                        displayPosts(threadId, selectedPostId);

                        try {
                            dataSource.incrementViewCount(requestedThread);
                            dataSource.markThreadRead(requestedThread.getId());
                        } catch (final DataSourceException e) {
                            log.error("Couldn't increment view count and "
                                    + "mark thread as read.", e);
                        }
                    } else {
                        view.setViewData(null, null);
                        view.showError("Not allowed to view the topic");
                    }
                } else {
                    view.setViewData(null, null);
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

            @Override
            public Long getCategoryId() {
                final Category category = currentThread.getCategory();
                return category != null ? category.getId() : null;
            }

            @Override
            public String getMayNotReplyNote() {
                return configuration.getMayNotReplyNote();
            }

            @Override
            public boolean isThreadLocked() {
                return currentThread.isLocked();
            }
        };
    }

    private AuthoringData getAuthoringData() {
        final User currentUser = dataSource.getCurrentUser();
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
                return currentUser.getDisplayedName();
            }

            @Override
            public String getCurrentUserAvatarUrl() {
                return currentUser.getAvatarUrl();
            }

            @Override
            public String getCurrentUserBadgeHTML() {
                String result = null;
                final UserBadgeProvider badgeProvider = ToriApiLoader
                        .getCurrent().getUserBadgeProvider();
                if (badgeProvider != null) {
                    result = badgeProvider.getHtmlBadgeFor(currentUser);
                }
                return result;
            }

            @Override
            public String getCurrentUserLink() {
                return currentUser.getUserLink();
            }

            @Override
            public boolean mayFollow() {
                return authorizationService.mayFollowThread(currentThread
                        .getId());
            }
        };
    }

    private void displayPosts(final long threadId, final Long selectedPostId) {
        List<PostData> posts = new ArrayList<PostData>();
        Integer selectedIndex = null;
        try {
            int index = -1;
            for (Post post : dataSource.getPosts(threadId)) {
                index++;
                posts.add(getPostData(post));
                if (selectedPostId != null && selectedPostId == post.getId()) {
                    selectedIndex = index;
                }
            }
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
        view.setPosts(posts, selectedIndex);
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

    public void delete(final long postId) {
        try {
            boolean deletingLastMessage = currentThread.getPostCount() == 1;
            dataSource.deletePost(postId);

            if (deletingLastMessage) {
                view.showNotification("Thread deleted");
                view.threadDeleted();
            } else {
                view.showNotification("Post deleted");
            }
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
    public void upvote(final long postId) throws DataSourceException {
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
            final Map<String, byte[]> attachments, final boolean follow) {
        startedTyping = null;
        try {
            final Post updatedPost = dataSource.saveReply(rawBody, attachments,
                    currentThread.getId());

            if (follow
                    && authorizationService.mayFollowThread(currentThread
                            .getId())
                    && !dataSource.isFollowingThread(currentThread.getId())) {
                dataSource.followThread(currentThread.getId());
            } else if (!follow
                    && dataSource.isFollowingThread(currentThread.getId())) {
                dataSource.unfollowThread(currentThread.getId());
            }
            if (messaging != null) {
                messaging.sendUserAuthored(updatedPost.getId(),
                        currentThread.getId());
            }
            if (configuration.isUseToriMailService() && mailService != null) {
                mailService.sendUserAuthored(updatedPost.getId(),
                        getFormattedBody(updatedPost, true));
            }
            view.replySent();
            view.appendPosts(Arrays.asList(getPostData(updatedPost)));
        } catch (final FileNameException e) {
            log.error(e);
            e.printStackTrace();
            view.showError("Invalid file names");
            view.authoringFailed();
        } catch (final DataSourceException e) {
            view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
            log.error(e);
            e.printStackTrace();
            view.authoringFailed();
        }
    }

    public String stripTags(final String html) {
        return html.replaceAll("\\<.*?>", "");
    }

    public void handleArguments(final String[] arguments)
            throws DataSourceException {
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

    public void quotePost(final long postId) {
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
                view.updatePost(getPostData(dataSource.getPost(postId)));
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

    public void handlePostReport(final PostData post, final Reason reason,
            final String additionalInfo, final String postUrl) {
        dataSource.reportPost(post.getId(), reason, additionalInfo, postUrl);
        view.showNotification("Post reported");
    }

    @Override
    public void navigationTo(final String[] args) {
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
    public synchronized void userTyping(final long userId, final long threadId,
            final Date startedTyping) {
        try {
            if (currentThread != null && currentThread.getId() == threadId) {
                pendingReplies.put(userId, new Date[] { startedTyping,
                        new Date() });
            }
            refreshThreadUpdates();
        } catch (NullPointerException e) {
            log.warn("NPE while delivering user typing event", e);
        }
    }

    @Override
    public synchronized void userAuthored(final long postId, final long threadId) {
        try {
            if (currentThread != null && currentThread.getId() == threadId) {
                newPosts.add(postId);
                try {
                    Post post = dataSource.getPost(postId);
                    pendingReplies.remove(post.getAuthor().getId());
                } catch (DataSourceException e) {
                    log.warn(
                            "DataSourceException while delivering user authored event",
                            e);
                }
            }
            refreshThreadUpdates();
        } catch (NullPointerException e) {
            log.warn("NPE while delivering user authored event", e);
        }
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
                newPostsData.add(getPostData(post));
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        }
        view.appendPosts(newPostsData);

        newPosts.clear();
        refreshThreadUpdates();
    }
}
