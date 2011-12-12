package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class CategoryPresenter extends Presenter<CategoryView> {

    /** Something went wrong. Display a generic error message. */
    public static final class CategoryPresenterException extends Exception {
        private static final long serialVersionUID = 7051635753001642576L;

        public CategoryPresenterException(final Throwable e) {
            super(e);
        }
    }

    private Category currentCategory;

    public CategoryPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentCategoryById(final String categoryIdString) {
        try {
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
        } catch (final DataSourceException e) {
            e.printStackTrace();
            getView().panic();
        }
    }

    /**
     * Might return <code>null</code> if the visited URL doesn't include a valid
     * category id.
     */
    @CheckForNull
    public Category getCurrentCategory() {
        return currentCategory;
    }

    public boolean userCanFollow(final DiscussionThread thread)
            throws CategoryPresenterException {
        try {
            return authorizationService.mayFollow(thread)
                    && !dataSource.isFollowing(thread);
        } catch (final DataSourceException e) {
            log.error(e);
            throw new CategoryPresenterException(e);
        }
    }

    public boolean userCanUnFollow(final DiscussionThread thread)
            throws CategoryPresenterException {
        try {
            return authorizationService.mayFollow(thread)
                    && dataSource.isFollowing(thread);
        } catch (final DataSourceException e) {
            log.error(e);
            throw new CategoryPresenterException(e);
        }
    }

    public void follow(final DiscussionThread thread)
            throws CategoryPresenterException {
        try {
            dataSource.follow(thread);
            getView().confirmFollowing();
        } catch (final DataSourceException e) {
            throw new CategoryPresenterException(e);
        }
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
        resetView();
    }

    public void sticky(final DiscussionThread thread) {
        final DiscussionThread updatedThread = dataSource.sticky(thread);
        getView().confirmThreadStickied(updatedThread);
        resetView();
    }

    public void unsticky(final DiscussionThread thread) {
        final DiscussionThread updatedThread = dataSource.unsticky(thread);
        getView().confirmThreadUnstickied(updatedThread);
        resetView();
    }

    public boolean userCanSticky(final DiscussionThread thread) {
        return authorizationService.maySticky(thread) && !thread.isSticky();
    }

    public boolean userCanUnSticky(final DiscussionThread thread) {
        return authorizationService.maySticky(thread) && thread.isSticky();
    }

    public void lock(final DiscussionThread thread) {
        final DiscussionThread updatedThread = dataSource.lock(thread);
        getView().confirmThreadLocked(updatedThread);
    }

    public void unlock(final DiscussionThread thread) {
        final DiscussionThread updatedThread = dataSource.unlock(thread);
        getView().confirmThreadUnlocked(updatedThread);
    }

    public boolean userCanLock(final DiscussionThread thread) {
        return authorizationService.mayLock(thread) && !thread.isLocked();
    }

    public boolean userCanUnLock(final DiscussionThread thread) {
        return authorizationService.mayLock(thread) && thread.isLocked();
    }

    public boolean userMayDelete(final DiscussionThread thread) {
        return authorizationService.mayDelete(thread);
    }

    public void delete(final DiscussionThread thread) {
        dataSource.delete(thread);
        getView().confirmThreadDeleted();
        resetView();
    }

    public boolean userMayStartANewThread() {
        return authorizationService.mayCreateThreadIn(currentCategory);
    }

    private void resetView() {
        getView().displayThreads(dataSource.getThreads(currentCategory));
    }

}
