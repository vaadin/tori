package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.View;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface CategoryView extends View {
    void displaySubCategories(List<Category> subCategories);

    void displayThreads(List<DiscussionThread> threadsInCategory);

    void displayCategoryNotFoundError(String requestedCategoryId);

    /**
     * Get the current category in this view.
     * <p/>
     * <strong>Note:</strong> This method may return <code>null</code>, if the
     * user visited an url leading to an non-existent category
     */
    @CheckForNull
    Category getCurrentCategory();

    void confirmFollowing();

    void confirmUnfollowing();

    void confirmThreadMoved();

    void confirmThreadStickied();

    void confirmThreadUnstickied();

    void confirmThreadLocked();

    void confirmThreadUnlocked();

    void confirmThreadDeleted();
}
