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

    /**
     * Returns <code>true</code> iff the current user doesn't follow the thread,
     * and is allowed to follow a thread.
     */
    public boolean userCanFollow(final DiscussionThread thread)
            throws DataSourceException {
        try {
            return authorizationService.mayFollow(thread)
                    && !dataSource.isFollowing(thread);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Returns <code>true</code> iff the current user follows the thread, and
     * <em>is allowed to follow a thread</em>.
     */
    public boolean userCanUnFollow(final DiscussionThread thread)
            throws DataSourceException {
        try {
            return authorizationService.mayFollow(thread)
                    && dataSource.isFollowing(thread);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void follow(final DiscussionThread thread)
            throws DataSourceException {
        try {
            dataSource.follow(thread);
            getView().confirmFollowing();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void unfollow(final DiscussionThread thread)
            throws DataSourceException {
        try {
            dataSource.unFollow(thread);
            getView().confirmUnfollowing();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayMove(final DiscussionThread thread) {
        return authorizationService.mayMove(thread);
    }

    public List<Category> getRootCategories() throws DataSourceException {
        try {
            return dataSource.getRootCategories();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public List<Category> getSubCategories(final Category category)
            throws DataSourceException {
        try {
            return dataSource.getSubCategories(category);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void move(final DiscussionThread thread,
            final Category destinationCategory) throws DataSourceException {
        try {
            dataSource.move(thread, destinationCategory);
            getView().confirmThreadMoved();
            resetView();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void sticky(final DiscussionThread thread)
            throws DataSourceException {
        try {
            final DiscussionThread updatedThread = dataSource.sticky(thread);
            getView().confirmThreadStickied(updatedThread);
            resetView();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void unsticky(final DiscussionThread thread)
            throws DataSourceException {
        try {
            final DiscussionThread updatedThread = dataSource.unsticky(thread);
            getView().confirmThreadUnstickied(updatedThread);
            resetView();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userCanSticky(final DiscussionThread thread) {
        return authorizationService.maySticky(thread) && !thread.isSticky();
    }

    public boolean userCanUnSticky(final DiscussionThread thread) {
        return authorizationService.maySticky(thread) && thread.isSticky();
    }

    public void lock(final DiscussionThread thread) throws DataSourceException {
        try {
            final DiscussionThread updatedThread = dataSource.lock(thread);
            getView().confirmThreadLocked(updatedThread);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public void unlock(final DiscussionThread thread)
            throws DataSourceException {
        try {
            final DiscussionThread updatedThread = dataSource.unlock(thread);
            getView().confirmThreadUnlocked(updatedThread);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

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

    public void delete(final DiscussionThread thread)
            throws DataSourceException {
        try {
            dataSource.delete(thread);
            getView().confirmThreadDeleted();
            resetView();
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }

    }

    public boolean userMayStartANewThread() {
        return authorizationService.mayCreateThreadIn(currentCategory);
    }

    private void resetView() throws DataSourceException {
        try {
            getView().displayThreads(dataSource.getThreads(currentCategory));
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

}
