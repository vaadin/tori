package org.vaadin.tori.data;

import java.util.List;

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
    List<Category> getSubCategories(@NonNull Category category)
            throws DataSourceException;

    /**
     * Get all threads in the given <code>category</code>, in the following
     * ordering priority:
     * 
     * <ul>
     * <li>Primary: Stickied threads ({@link DiscussionThread#isSticky()})
     * <li>Secondary: Recent activity, in descending order
     * </ul>
     */
    @NonNull
    List<DiscussionThread> getThreads(@NonNull Category category)
            throws DataSourceException;

    /**
     * Returns the Category corresponding to the id or <code>null</code> if no
     * such Category exist.
     */
    @CheckForNull
    Category getCategory(long categoryId) throws DataSourceException;

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
     */
    void save(@NonNull Category categoryToSave) throws DataSourceException;

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
     */
    void saveAsCurrentUser(@NonNull Post post) throws DataSourceException;

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
     * Passes the current request to this DataSource. It can be an instance of
     * {@code PortletRequest} or {@code HttpServletRequest} depending on the
     * context. The implementation is free to ignore the request if it doesn't
     * need any parameters from the request.
     * 
     * @param request
     *            {@code PortletRequest} or {@code HttpServletRequest}
     */
    void setRequest(@NonNull Object request);

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
            @NonNull Post firstPost) throws DataSourceException;
}
