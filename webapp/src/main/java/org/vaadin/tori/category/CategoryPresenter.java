package org.vaadin.tori.category;

import java.util.Collections;
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
        final CategoryView view = getView();
        try {
            if (categoryIdString.equals(SpecialCategory.RECENT_POSTS.getId())) {
                currentCategory = SpecialCategory.RECENT_POSTS.getInstance();

                final List<Category> empty = Collections.emptyList();
                view.displaySubCategories(empty);
                view.displayThreads(dataSource.getRecentPosts());
            } else if (categoryIdString
                    .equals(SpecialCategory.MY_POSTS.getId())) {
                currentCategory = SpecialCategory.MY_POSTS.getInstance();

                final List<Category> empty = Collections.emptyList();
                view.displaySubCategories(empty);
                view.displayThreads(dataSource.getMyPosts());
            } else {
                Category requestedCategory = null;
                try {
                    final long categoryId = Long.valueOf(categoryIdString);
                    requestedCategory = dataSource.getCategory(categoryId);
                } catch (final NumberFormatException e) {
                    log.error("Invalid category id format: " + categoryIdString);
                }

                if (requestedCategory != null) {
                    currentCategory = requestedCategory;
                    view.displaySubCategories(dataSource
                            .getSubCategories(currentCategory));
                } else {
                    getView().displayCategoryNotFoundError(categoryIdString);
                }
                if (countThreads() > 0) {
                    view.displayThreads();
                } else {
                    view.hideThreads();
                }
            }
            view.setUserMayStartANewThread(userMayStartANewThread());
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
            getView().confirmFollowing(thread);
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
            getView().confirmUnfollowing(thread);
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
        boolean userMayStartANewThread = false;
        if (SpecialCategory.isSpecialCategory(currentCategory)) {
            // special "categories" like recent posts
            userMayStartANewThread = false;
        }
        if (authorizationService != null) {
            userMayStartANewThread = authorizationService
                    .mayCreateThreadIn(currentCategory);
        }
        return userMayStartANewThread;
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

    public boolean userHasRead(final DiscussionThread thread) {
        try {
            return dataSource.isRead(thread);
        } catch (final DataSourceException e) {
            log.error(e);
            // Just log the error and return true, not considering this a
            // serious problem.
            return true;
        }
    }

    public boolean userIsFollowing(final DiscussionThread thread) {
        try {
            return dataSource.isFollowing(thread);
        } catch (final DataSourceException e) {
            log.error(e);
            // Just log the error and return false, not considering this a
            // serious problem.
            return false;
        }
    }

    public long countThreads() throws DataSourceException {
        try {
            return dataSource.getThreadCount(currentCategory);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

    public List<DiscussionThread> getThreadsBetween(final int from, final int to)
            throws DataSourceException {
        try {
            return dataSource.getThreads(currentCategory, from, to);
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }

}
