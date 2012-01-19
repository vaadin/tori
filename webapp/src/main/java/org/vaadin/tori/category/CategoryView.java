package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.View;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface CategoryView extends View {
    void displaySubCategories(List<Category> subCategories);

    // void displayThreads(List<DiscussionThread> threadsInCategory);

    void displayCategoryNotFoundError(String requestedCategoryId);

    /**
     * Get the current category in this view.
     * <p/>
     * <strong>Note:</strong> This method may return <code>null</code>, if the
     * user visited an url leading to an non-existent category
     */
    @CheckForNull
    Category getCurrentCategory();

    void confirmFollowing(DiscussionThread thread);

    void confirmUnfollowing(DiscussionThread thread);

    void confirmThreadMoved();

    /**
     * @param thread
     *            The updated {@link DiscussionThread}
     */
    void confirmThreadStickied(DiscussionThread thread);

    /**
     * @param thread
     *            The updated {@link DiscussionThread}
     */
    void confirmThreadUnstickied(DiscussionThread thread);

    /**
     * @param thread
     *            The updated {@link DiscussionThread}
     */
    void confirmThreadLocked(DiscussionThread thread);

    /**
     * @param thread
     *            The updated {@link DiscussionThread}
     */
    void confirmThreadUnlocked(DiscussionThread thread);

    void confirmThreadDeleted();

    /**
     * Show an error message to the user that says that something irrecoverable
     * went wrong, and there's nothing really we can do.
     * 
     * @see PanicComponent
     */
    void panic();
}
