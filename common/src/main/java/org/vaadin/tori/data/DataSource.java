package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.service.post.PostReport;

public interface DataSource {

    /**
     * Returns a list of all root {@link Category} instances.
     * 
     * @return all root {@link Category} instances.
     */
    List<Category> getRootCategories();

    /**
     * Get all {@link Category Categories} that have <code>category</code> as
     * their parent.
     * 
     * @param root
     *            The parent <code>Category</code> for the queried
     *            <code>Categories</code>.
     */
    List<Category> getSubCategories(Category category);

    /**
     * Get all threads in the given <code>category</code>, ordered by most
     * recent activity first.
     */
    List<DiscussionThread> getThreads(Category category);

    /**
     * Returns the Category corresponding to the id or {@code null} if no such
     * Category exist.
     */
    Category getCategory(long categoryId);

    /**
     * Returns the number {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category}.
     * 
     * @param category
     *            Category from which to count the threads.
     * @return number of DiscussionThreads
     */
    long getThreadCount(Category category);

    /**
     * Returns the number of {@link DiscussionThread DiscussionThreads} in the
     * given {@link Category} that are considered unread by the current user.
     * 
     * @param category
     *            Category from which to count the unread threads.
     * @return number of unread DiscussionThreads
     */
    long getUnreadThreadCount(Category category);

    /**
     * Returns the {@link DiscussionThread} corresponding to the id or
     * <code>null</code> if no such <code>DiscussionThread</code> exists.
     */
    DiscussionThread getThread(long threadId);

    /**
     * Returns all {@link Post Posts} in a {@link Thread} in ascending time
     * order (oldest, i.e. first, post first).
     */
    List<Post> getPosts(Thread thread);

    /**
     * Saves all changes made to the given {@link Category Categories}.
     * 
     * @param categoriesToSave
     *            {@link Category Categories} to save.
     */
    void save(Iterable<Category> categoriesToSave);

    /**
     * Saves all changes made to the given {@link Category Category} or adds it
     * if it's a new Category.
     * 
     * @param categoryToSave
     *            {@link Category Category} to save.
     */
    void save(Category categoryToSave);

    /**
     * Removes the given {@link Category} along with all containing
     * {@link DiscussionThread DiscussionThreads}, {@link Post Posts} and sub
     * categories.
     * 
     * @param categoryToDelete
     *            {@link Category Category} to delete.
     */
    void delete(Category categoryToDelete);

    /**
     * Handles the reporting of a single {@link Post}.
     * 
     * @param report
     *            The report in its entirety.
     */
    void reportPost(PostReport report);

    void ban(User user);

    void follow(DiscussionThread thread);

    void unFollow(DiscussionThread thread);

    boolean isFollowing(DiscussionThread currentThread);
}
