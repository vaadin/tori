/*
 * Copyright 2014 Vaadin Ltd.
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

package org.vaadin.tori.view.listing.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.view.listing.SpecialCategory;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadData;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadProvider;

public class ThreadListingPresenter extends Presenter<ThreadListingView> {

    private Long categoryId;

    public ThreadListingPresenter(ThreadListingView view) {
        super(view);
    }

    public void categorySelected(Category category) {
        view.setMayCreateThreads(false);
        ThreadProvider threadProvider = null;
        if (category == SpecialCategory.RECENT_POSTS.getInstance()) {
            threadProvider = getRecentThreadsProvider();
        } else if (category == SpecialCategory.MY_POSTS.getInstance()) {
            threadProvider = getMyThreadsProvider();
        } else {
            Long categoryId = category != null ? category.getId() : null;
            threadProvider = getDefaultThreadProvider(categoryId);
            this.categoryId = categoryId;
            view.setMayCreateThreads(authorizationService
                    .mayCreateThreadInCategory(categoryId));
        }
        view.setThreadProvider(threadProvider);

    }

    public void follow(long threadId) {
        try {
            dataSource.followThread(threadId);
            view.showNotification("Thread followed");
            updateThread(threadId);
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    public void unfollow(long threadId) {
        try {
            dataSource.unfollowThread(threadId);
            view.showNotification("Thread unfollowed");
            updateThread(threadId);
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    public void sticky(long threadId) {
        try {
            dataSource.stickyThread(threadId);
            view.showNotification("Thread stickied");
            updateThread(threadId);
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    public void unsticky(long threadId) {
        try {
            dataSource.unstickyThread(threadId);
            view.showNotification("Thread unstickied");
            updateThread(threadId);
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    private void updateThread(long threadId) {
        try {
            view.updateThread(getThreadData(dataSource.getThread(threadId)));
        } catch (NoSuchThreadException e) {
            displayError(e);
        } catch (DataSourceException e) {
            displayError(e);
        }
    }

    public void delete(long threadId) {
        try {
            dataSource.deleteThread(threadId);
            view.showNotification("Thread deleted");
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    public void lock(final long threadId) {
        try {
            dataSource.lockThread(threadId);
            view.showNotification("Thread locked");
            updateThread(threadId);
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    public void unlock(final long threadId) {
        try {
            dataSource.unlockThread(threadId);
            view.showNotification("Thread unlocked");
            updateThread(threadId);
        } catch (final DataSourceException e) {
            displayError(e);
        }
    }

    private void displayError(DataSourceException e) {
        log.error(e);
        e.printStackTrace();
        view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
    }

    public void moveRequested(long threadId) {
        try {
            Category category = dataSource.getThread(threadId).getCategory();
            Long threadCategoryId = category != null ? category.getId() : null;
            List<Category> allCategories = getSubCategoriesRecursively(null);
            view.showThreadMovePopup(threadId, threadCategoryId, allCategories);
        } catch (DataSourceException e) {
            displayError(e);
        }
    }

    public void move(long threadId, Long categoryId) {
        try {
            dataSource.moveThread(threadId, categoryId);
            view.showNotification("Thread moved");
        } catch (DataSourceException e) {
            displayError(e);
        }
    }

    private List<Category> getSubCategoriesRecursively(Long categoryId) {
        List<Category> result = new ArrayList<Category>();
        try {
            for (Category subCategory : dataSource.getSubCategories(categoryId)) {
                if (authorizationService.mayViewCategory(subCategory.getId())) {
                    result.add(subCategory);
                    result.addAll(getSubCategoriesRecursively(subCategory
                            .getId()));
                }
            }
        } catch (DataSourceException e) {
            // NOP
        }
        return result;
    }

    private ThreadData getThreadData(final DiscussionThread thread) {
        final long threadId = thread.getId();
        final Long categoryId = thread.getCategory() != null ? thread
                .getCategory().getId() : null;
        return new ThreadData() {

            @Override
            public boolean isFollowing() {
                return dataSource.isFollowingThread(threadId);
            }

            @Override
            public boolean userHasRead() {
                return dataSource.isThreadRead(threadId);
            }

            @Override
            public boolean mayMove() {
                return authorizationService.mayMoveThreadInCategory(categoryId);
            }

            @Override
            public boolean maySticky() {
                return authorizationService
                        .mayStickyThreadInCategory(categoryId);
            }

            @Override
            public boolean mayLock() {
                return authorizationService.mayLockThreadInCategory(categoryId);
            }

            @Override
            public boolean mayDelete() {
                return authorizationService.mayDeleteThread(threadId);
            }

            @Override
            public boolean mayReplyIn() {
                return authorizationService.mayReplyInThread(threadId);
            }

            @Override
            public boolean mayFollow() {
                return authorizationService.mayFollowThread(threadId);
            }

            @Override
            public long getId() {
                return threadId;
            }

            @Override
            public boolean isLocked() {
                return thread.isLocked();
            }

            @Override
            public boolean isSticky() {
                return thread.isSticky();
            }

            @Override
            public String getAuthor() {
                return thread.getOriginalPoster().getDisplayedName();
            }

            @Override
            public int getPostCount() {
                return thread.getPostCount();
            }

            @Override
            public String getTopic() {
                return thread.getTopic();
            }

            @Override
            public String getLatestPostAuthor() {
                return thread.getLatestPost().getAuthor().getDisplayedName();
            }

            @Override
            public Date getLatestPostTime() {
                return thread.getLatestPost().getTime();
            }

        };
    }

    private ThreadProvider getDefaultThreadProvider(final Long categoryId) {
        return new AbstractThreadProvider() {
            @Override
            protected List<DiscussionThread> getThreadsBetweenInternal(
                    int from, int to) throws DataSourceException {
                return dataSource.getThreads(categoryId, from, to);
            }

            @Override
            protected int getThreadCountInternal() throws DataSourceException {
                return dataSource.getThreadCount(categoryId);
            }
        };
    }

    private ThreadProvider getMyThreadsProvider() {
        return new AbstractThreadProvider() {
            @Override
            protected List<DiscussionThread> getThreadsBetweenInternal(
                    int from, int to) throws DataSourceException {
                return dataSource.getMyPostThreads(from, to);
            }

            @Override
            protected int getThreadCountInternal() throws DataSourceException {
                return dataSource.getMyPostThreadsCount();
            }
        };
    }

    private ThreadProvider getRecentThreadsProvider() {
        return new AbstractThreadProvider() {
            @Override
            protected List<DiscussionThread> getThreadsBetweenInternal(
                    int from, int to) throws DataSourceException {
                return dataSource.getRecentPosts(from, to);
            }

            @Override
            protected int getThreadCountInternal() throws DataSourceException {
                return dataSource.getRecentPostsCount();
            }
        };
    }

    private abstract class AbstractThreadProvider implements ThreadProvider {

        @Override
        public int getThreadCount() {
            int result = 0;
            try {
                result = getThreadCountInternal();
            } catch (DataSourceException e) {
                displayError(e);
            }
            return result;
        }

        @Override
        public List<ThreadData> getThreadsBetween(int from, int to) {
            List<ThreadData> result = new ArrayList<ThreadData>();
            try {
                for (DiscussionThread thread : getThreadsBetweenInternal(from,
                        to)) {
                    if (thread.getOriginalPoster() != null) {
                        result.add(getThreadData(thread));
                    }
                }
            } catch (DataSourceException e) {
                displayError(e);
            }
            return result;
        }

        protected abstract int getThreadCountInternal()
                throws DataSourceException;

        protected abstract List<DiscussionThread> getThreadsBetweenInternal(
                int from, int to) throws DataSourceException;

    }

    public void createTopicRequested() {
        view.navigateToNewThreadView(categoryId);
    }

}
