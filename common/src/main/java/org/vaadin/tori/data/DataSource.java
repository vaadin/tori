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

package org.vaadin.tori.data;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.vaadin.tori.Configuration;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchCategoryException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.service.post.PostReport.Reason;

public interface DataSource {

    public interface UrlInfo {
        public enum Destination {
            CATEGORY, THREAD, DASHBOARD;
        }

        public Destination getDestination();

        public long getId();
    }

    /**
     * Get all {@link Category Categories} that have <code>category</code> as
     * their parent.
     */
    List<Category> getSubCategories(Long categoryId) throws DataSourceException;

    /**
     * Get all threads in the given {@code category}, in the following ordering
     * priority:
     * 
     * <ul>
     * <li>Primary: Stickied threads ({@link DiscussionThread#isSticky()})
     * <li>Secondary: Recent activity, in descending order
     * </ul>
     * 
     * Notice that as this method returns all threads it might be quite slow.
     * For categories with large amount of threads it might be better to fetch
     * only the required threads (see {@link #getThreads(Category, long, long)}
     * ).
     * 
     * @see #getThreadCountRecursively(Category)
     * @see #getThreads(Category, int, int)
     */
    List<DiscussionThread> getThreads(Category category)
            throws DataSourceException;

    /**
     * Get threads between given {@code startIndex} and {@code endIndex} in the
     * given {@code category}. The index is zero-based, so zero equals to the
     * first post. The {@code endIndex} is inclusive, so fetching threads from 0
     * to 9 returns first 10 threads. As with {@link #getThreads(Category)}
     * method, the threads are ordered in the following ordering priority:
     * 
     * <ul>
     * <li>Primary: Stickied threads ({@link DiscussionThread#isSticky()})
     * <li>Secondary: Recent activity, in descending order
     * </ul>
     * 
     * @see #getThreadCountRecursively(Category)
     * @see #getThreads()
     */

    List<DiscussionThread> getThreads(Long categoryId, int startIndex,
            int endIndex) throws DataSourceException;

    /**
     * Returns the Category corresponding to the id or <code>null</code> if no
     * such Category exist.
     */

    Category getCategory(Long categoryId) throws DataSourceException;

    /**
     * Returns the number {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category}, and all categories beneath it.
     * 
     * @return number of DiscussionThreads
     */
    int getThreadCountRecursively(Long categoryId) throws DataSourceException;

    /**
     * Returns the number {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category}.
     * 
     * @return number of DiscussionThreads
     */
    int getThreadCount(Long categoryId) throws DataSourceException;

    /**
     * Returns the number of {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category} that are considered unread by the current user.
     * 
     * @return number of unread DiscussionThreads
     */
    int getUnreadThreadCount(long categoryId) throws DataSourceException;

    /**
     * Returns the {@link DiscussionThread} corresponding to the id or
     * <code>null</code> if no such <code>DiscussionThread</code> exists.
     */

    DiscussionThread getThread(long threadId) throws NoSuchThreadException,
            DataSourceException;

    /**
     * Returns all {@link Post Posts} in a {@link Thread} in ascending time
     * order (oldest, i.e. first, post first).
     */

    List<Post> getPosts(long threadId) throws DataSourceException;

    /**
     * Handles the reporting of a single {@link Post}.
     * 
     * @param report
     *            The report in its entirety.
     */
    void reportPost(long postId, Reason reason, String additionalInfo,
            String postUrl);

    void savePost(long postId, String newBody) throws DataSourceException;

    void banUser(long userId) throws DataSourceException;

    void unbanUser(long userId) throws DataSourceException;

    void followThread(long threadId) throws DataSourceException;

    void unfollowThread(long threadId) throws DataSourceException;

    boolean isFollowingThread(long threadId);

    void deletePost(long postId) throws DataSourceException;

    Boolean getPostVote(long postId) throws DataSourceException;

    /**
     * Deletes the current user's possible vote on the given {@link Post}. If no
     * such vote is given, this method does nothing.
     */
    void removeUserVote(long postId) throws DataSourceException;

    /**
     * The current user upvotes the given {@link Post}.
     * <p/>
     * <em>Note:</em> This method must make sure that all previous votes on the
     * given {@link Post} are removed before the new vote is given.
     */
    void upvote(long postId) throws DataSourceException;

    /**
     * The current user downvotes the given {@link Post}
     * <p/>
     * <em>Note:</em> This method must make sure that all previous votes on the
     * given {@link Post} are removed before the new vote is given.
     */
    void downvote(long postId) throws DataSourceException;

    /**
     * Upvotes count as +1 points, downvotes count as -1 points.
     * <p/>
     * <strong>Note: This method is on probation. The calculations may change,
     * or this method might be split in three: <code>getUpvotes</code>,
     * <code>getDownvotes</code> and <code>getVoteCount</code>
     */
    long getPostScore(long postId) throws DataSourceException;

    void moveThread(long threadId, Long destinatinoCategoryId)
            throws DataSourceException;

    void stickyThread(long threadId) throws DataSourceException;

    void unstickyThread(long threadId) throws DataSourceException;

    void lockThread(long threadId) throws DataSourceException;

    void unlockThread(long threadId) throws DataSourceException;

    /**
     * Deletes a thread.
     * <p/>
     * This cascades all the way to removing:
     * <ul>
     * <li>{@link Post Posts} in thread
     * <li>Votes for each post
     * <li>Following statuses for the thread
     * </ul>
     * 
     * @see org.vaadin.tori.data.entity.PostVote PostVote
     * @see org.vaadin.tori.data.entity.Following Following
     */
    void deleteThread(long threadId) throws DataSourceException;

    /**
     * This method is responsible for making sure that a new thread is created,
     * with a certain first post. It also needs to add "current user" as the
     * author of that post.
     * <p/>
     * This method requires that the thread and post are already interlinked.
     * 
     * @return the newly created {@link DiscussionThread} first Post.
     */

    Post saveNewThread(String topic, String rawBody,
            Map<String, byte[]> attachments, Long categoryId)
            throws DataSourceException;

    /**
     * Increments the view count of the given thread by one. This method should
     * be always used instead of directly incrementing the view count property
     * of the {@link DiscussionThread}.
     * 
     * @param thread
     * @throws DataSourceException
     */
    void incrementViewCount(DiscussionThread thread) throws DataSourceException;

    /**
     * Returns true if the current user has read the given thread. If no user is
     * logged in, this method will return true for any thread.
     */
    boolean isThreadRead(long threadId);

    /**
     * Marks the given thread as read. If no user is logged in, this method
     * doesn't do anything.
     * 
     * @param thread
     * @throws DataSourceException
     * @see {@link #isRead(DiscussionThread)}
     */
    void markRead(DiscussionThread thread) throws DataSourceException;

    List<DiscussionThread> getRecentPosts(int from, int to)
            throws DataSourceException;

    int getRecentPostsCount() throws DataSourceException;

    List<DiscussionThread> getMyPostThreads(int from, int to)
            throws DataSourceException;

    int getMyPostThreadsCount() throws DataSourceException;

    int getAttachmentMaxFileSize();

    boolean isLoggedInUser();

    Map<String, String> getPostReplacements();

    boolean getReplaceMessageBoardsLinks();

    boolean getUpdatePageTitle();

    String getPageTitlePrefix();

    void save(Configuration configuration) throws DataSourceException;

    /**
     * @return The tracker id to be used, or <code>null</code> if there is no
     *         tracker id. Then the tracker won't be used at all.
     */

    String getGoogleAnalyticsTrackerId();

    /**
     * Gets the path root configured for this application. Usable for
     * redirection to the "root" of Tori.
     * 
     * @deprecated This method needs to be moved somewhere else
     */
    @Deprecated
    String getPathRoot();

    /**
     * Accepts a request from the native platform, checks whether that request
     * contains any information that would link to forum content, and returns
     * the pieces back in a Tori-understandable way.
     * 
     * @return {@link UrlInfo} parsed from the request, or <code>null</code> if
     *         request didn't contain any native-specific things.
     * @throws NoSuchThreadException
     *             If the request seemed to contain thread information, but the
     *             indicated category doesn't exist.
     * @throws NoSuchCategoryException
     *             If the request seemed to contain category information, but
     *             the indicated category doesn't exist.
     * @throws DataSourceException
     *             pokemon
     */

    UrlInfo getUrlInfoFromBackendNativeRequest(HttpServletRequest servletRequest)
            throws NoSuchThreadException, DataSourceException;

    User getToriUser(long userId) throws DataSourceException;

    Post getPost(long postId) throws DataSourceException;

    void saveNewCategory(Long parentCategoryId, String name, String description)
            throws DataSourceException;

    void updateCategory(long categoryId, String name, String description)
            throws DataSourceException;

    /**
     * Removes the given category along with all containing
     * {@link DiscussionThread DiscussionThreads}, {@link Post Posts} and sub
     * categories.
     */
    void deleteCategory(long categoryId) throws DataSourceException;

    Post saveReply(String rawBody, Map<String, byte[]> attachments,
            long threadId) throws DataSourceException;

    User getCurrentUser();

}
