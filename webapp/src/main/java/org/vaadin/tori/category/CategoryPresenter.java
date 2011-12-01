package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

public class CategoryPresenter extends Presenter<CategoryView> {

    private Category currentCategory;

    public CategoryPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentCategoryById(final String categoryIdString) {
        Category requestedCategory = null;
        try {
            final long categoryId = Long.valueOf(categoryIdString);
            requestedCategory = dataSource.getCategory(categoryId);
        } catch (final NumberFormatException e) {
            log.error("Invalid category id format: " + categoryIdString);
        }

        if (requestedCategory != null) {
            currentCategory = requestedCategory;

            final CategoryView view = getView();
            view.displaySubCategories(dataSource
                    .getSubCategories(currentCategory));
            view.displayThreads(dataSource.getThreads(currentCategory));
        } else {
            getView().displayCategoryNotFoundError(categoryIdString);
        }
    }

    public Category getCurrentCategory() {
        return currentCategory;
    }

    public boolean userCanFollow(final DiscussionThread thread) {
        return authorizationService.mayFollow(thread)
                && !dataSource.isFollowing(thread);
    }

    public boolean userCanUnFollow(final DiscussionThread thread) {
        return authorizationService.mayFollow(thread)
                && dataSource.isFollowing(thread);
    }

    public void follow(final DiscussionThread thread) {
        dataSource.follow(thread);
        getView().confirmFollowing();
    }

    public void unfollow(final DiscussionThread thread) {
        dataSource.unFollow(thread);
        getView().confirmUnfollowing();
    }

    public boolean userMayMove(final DiscussionThread thread) {
        return authorizationService.mayMove(thread);
    }

    public List<Category> getRootCategories() {
        return dataSource.getRootCategories();
    }

    public List<Category> getSubCategories(final Category category) {
        return dataSource.getSubCategories(category);
    }

    public void move(final DiscussionThread thread,
            final Category destinationCategory) {
        dataSource.move(thread, destinationCategory);
        getView().confirmThreadMoved();
        getView().displayThreads(dataSource.getThreads(currentCategory));
    }

    public void sticky(final DiscussionThread thread) {
        dataSource.sticky(thread);
        getView().confirmThreadStickied();
    }

    public void unsticky(final DiscussionThread thread) {
        dataSource.unsticky(thread);
        getView().confirmThreadUnstickied();
    }

    public boolean userCanSticky(final DiscussionThread thread) {
        return authorizationService.maySticky(thread) && !thread.isSticky();
    }

    public boolean userCanUnSticky(final DiscussionThread thread) {
        return authorizationService.maySticky(thread) && thread.isSticky();
    }

    public void lock(final DiscussionThread thread) {
        dataSource.lock(thread);
        getView().confirmThreadLocked();
    }

    public void unlock(final DiscussionThread thread) {
        dataSource.unlock(thread);
        getView().confirmThreadUnlocked();
    }

    public boolean userCanLock(final DiscussionThread thread) {
        return authorizationService.mayLock(thread) && !thread.isLocked();
    }

    public boolean userCanUnLock(final DiscussionThread thread) {
        return authorizationService.mayLock(thread) && thread.isLocked();
    }

}
