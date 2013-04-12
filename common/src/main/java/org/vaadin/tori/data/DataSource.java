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

import org.vaadin.tori.Configuration;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.post.PostReport;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface DataSource {

    public interface UrlInfo {
        public enum Destination {
            CATEGORY, THREAD, DASHBOARD;
        }

        public Destination getDestination();

        public long getId();
    }

    /**
     * Returns a list of all root {@link Category} instances.
     * 
     * @return all root {@link Category} instances.
     */
    @NonNull
    List<Category> getRootCategories() throws DataSourceException;

    /**
     * Get all {@link Category Categories} that have <code>category</code> as
     * their parent.
     * 
     * @param root
     *            The parent <code>Category</code> for the queried
     *            <code>Categories</code>.
     */
    @NonNull
    List<Category> getSubCategories(Category category)
            throws DataSourceException;

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
    @NonNull
    List<DiscussionThread> getThreads(@NonNull Category category)
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
    @NonNull
    List<DiscussionThread> getThreads(@NonNull Category category,
            int startIndex, int endIndex) throws DataSourceException;

    /**
     * Returns the Category corresponding to the id or <code>null</code> if no
     * such Category exist.
     */
    @CheckForNull
    Category getCategory(long categoryId) throws DataSourceException;

    /**
     * Returns the number {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category}, and all categories beneath it.
     * 
     * @param category
     *            Category from which to count the threads.
     * @return number of DiscussionThreads
     */
    long getThreadCountRecursively(@NonNull Category category)
            throws DataSourceException;

    /**
     * Returns the number {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category}.
     * 
     * @param category
     *            Category from which to count the threads.
     * @return number of DiscussionThreads
     */
    long getThreadCount(@NonNull Category category) throws DataSourceException;

    /**
     * Returns the number of {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category} that are considered unread by the current user.
     * 
     * @param category
     *            Category from which to count the unread threads.
     * @return number of unread DiscussionThreads
     */
    long getUnreadThreadCount(@NonNull Category category)
            throws DataSourceException;

    /**
     * Returns the {@link DiscussionThread} corresponding to the id or
     * <code>null</code> if no such <code>DiscussionThread</code> exists.
     */
    @CheckForNull
    DiscussionThread getThread(long threadId) throws DataSourceException;

    /**
     * Returns all {@link Post Posts} in a {@link Thread} in ascending time
     * order (oldest, i.e. first, post first).
     */
    @NonNull
    List<Post> getPosts(@NonNull DiscussionThread thread)
            throws DataSourceException;

    /**
     * Saves all changes made to the given {@link Category Categories}.
     * 
     * @param categoriesToSave
     *            {@link Category Categories} to save.
     */
    void save(@NonNull Iterable<Category> categoriesToSave)
            throws DataSourceException;

    /**
     * Saves all changes made to the given {@link Category Category} or adds it
     * if it's a new Category.
     * 
     * @param categoryToSave
     *            {@link Category Category} to save.
     * @return
     */
    Category save(@NonNull Category categoryToSave) throws DataSourceException;

    /**
     * Removes the given {@link Category} along with all containing
     * {@link DiscussionThread DiscussionThreads}, {@link Post Posts} and sub
     * categories.
     * 
     * @param categoryToDelete
     *            {@link Category Category} to delete.
     */
    void delete(@NonNull Category categoryToDelete) throws DataSourceException;

    /**
     * Handles the reporting of a single {@link Post}.
     * 
     * @param report
     *            The report in its entirety.
     */
    void reportPost(@NonNull PostReport report) throws DataSourceException;

    void save(@NonNull Post post) throws DataSourceException;

    void ban(@NonNull User user) throws DataSourceException;

    void unban(@NonNull User user) throws DataSourceException;

    void follow(@NonNull DiscussionThread thread) throws DataSourceException;

    void unFollow(@NonNull DiscussionThread thread) throws DataSourceException;

    boolean isFollowing(@NonNull DiscussionThread thread)
            throws DataSourceException;

    void delete(@NonNull Post post) throws DataSourceException;

    @NonNull
    PostVote getPostVote(@NonNull Post post) throws DataSourceException;

    /**
     * Deletes the current user's possible vote on the given {@link Post}. If no
     * such vote is given, this method does nothing.
     */
    void removeUserVote(@NonNull Post post) throws DataSourceException;

    /**
     * The current user upvotes the given {@link Post}.
     * <p/>
     * <em>Note:</em> This method must make sure that all previous votes on the
     * given {@link Post} are removed before the new vote is given.
     */
    void upvote(@NonNull Post post) throws DataSourceException;

    /**
     * The current user downvotes the given {@link Post}
     * <p/>
     * <em>Note:</em> This method must make sure that all previous votes on the
     * given {@link Post} are removed before the new vote is given.
     */
    void downvote(@NonNull Post post) throws DataSourceException;

    /**
     * Upvotes count as +1 points, downvotes count as -1 points.
     * <p/>
     * <strong>Note: This method is on probation. The calculations may change,
     * or this method might be split in three: <code>getUpvotes</code>,
     * <code>getDownvotes</code> and <code>getVoteCount</code>
     */
    long getScore(@NonNull Post post) throws DataSourceException;

    /**
     * Same as {@link #save(Post)}, but makes sure that the <code>post</code>'s
     * author is the current user.
     * 
     * @param files
     * 
     * @return The properly updated {@link Post}
     */
    Post saveAsCurrentUser(@NonNull Post post, Map<String, byte[]> files)
            throws DataSourceException;

    void move(@NonNull DiscussionThread thread,
            @NonNull Category destinationCategory) throws DataSourceException;

    @NonNull
    DiscussionThread sticky(@NonNull DiscussionThread thread)
            throws DataSourceException;

    @NonNull
    DiscussionThread unsticky(@NonNull DiscussionThread thread)
            throws DataSourceException;

    @NonNull
    DiscussionThread lock(@NonNull DiscussionThread thread)
            throws DataSourceException;

    @NonNull
    DiscussionThread unlock(@NonNull DiscussionThread thread)
            throws DataSourceException;

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
    void delete(@NonNull DiscussionThread thread) throws DataSourceException;

    /**
     * This method is responsible for making sure that a new thread is created,
     * with a certain first post. It also needs to add "current user" as the
     * author of that post.
     * <p/>
     * This method requires that the thread and post are already interlinked.
     * 
     * @return the newly created {@link DiscussionThread} that contains a proper
     *         thread id.
     */
    @NonNull
    DiscussionThread saveNewThread(@NonNull DiscussionThread newThread,
            final Map<String, byte[]> files, @NonNull Post firstPost)
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
     * 
     * @param thread
     * @return
     * @throws DataSourceException
     * @see {@link #markRead(DiscussionThread)}
     */
    boolean isRead(DiscussionThread thread) throws DataSourceException;

    /**
     * Marks the given thread as read. If no user is logged in, this method
     * doesn't do anything.
     * 
     * @param thread
     * @throws DataSourceException
     * @see {@link #isRead(DiscussionThread)}
     */
    void markRead(DiscussionThread thread) throws DataSourceException;

    @NonNull
    List<DiscussionThread> getRecentPosts(int from, int to)
            throws DataSourceException;

    int getRecentPostsAmount() throws DataSourceException;

    @NonNull
    List<DiscussionThread> getMyPosts(int from, int to)
            throws DataSourceException;

    int getMyPostsAmount() throws DataSourceException;

    int getAttachmentMaxFileSize();

    boolean isLoggedInUser();

    @NonNull
    Map<String, String> getPostReplacements();

    boolean getReplaceMessageBoardsLinks();

    void save(@NonNull Configuration configuration) throws DataSourceException;

    /**
     * @return The tracker id to be used, or <code>null</code> if there is no
     *         tracker id. Then the tracker won't be used at all.
     */
    @CheckForNull
    String getGoogleAnalyticsTrackerId();

    /**
     * @param queryUrl
     *            The part of the URL that comes after the context path
     *            (excludes fragment)
     * @param string
     * @param queryPart
     * @return The fragment that corresponds to the queried URL.
     *         <code>null</code> if no changes are to be made. Empty string to
     *         clear the fragment.
     * @deprecated This method needs to be moved somewhere else. (see also
     *             {@link #getPathRoot()})
     * @throws Exception
     *             if the translation to a Tori fragment was unsuccessful.
     */
    @Deprecated
    @CheckForNull
    UrlInfo getToriFragment(@NonNull String queryUrl, String queryPart)
            throws Exception;

    /**
     * @deprecated This method needs to be moved somewhere else (see also
     *             {@link #getToriFragment(String)})
     */
    @Deprecated
    String getPathRoot();
}
