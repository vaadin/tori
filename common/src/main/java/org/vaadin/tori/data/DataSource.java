package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;

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
    List<Thread> getThreads(Category category);

    /**
     * Returns the Category corresponding to the id or {@code null} if no such
     * Category exist.
     */
    Category getCategory(long categoryId);
}
