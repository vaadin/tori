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

import java.util.Date;
import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.View;

public interface ThreadListingView extends View {

    void setThreadProvider(ThreadProvider threadProvider);

    void showError(String message);

    void showNotification(String message);

    void updateThread(ThreadData thread);

    void setMayCreateThreads(boolean mayCreateThreads);

    void showThreadMovePopup(long threadId, Long threadCategoryId,
            List<Category> allCategories);

    void navigateToNewThreadView(Long categoryId);

    public interface ThreadProvider {
        int getThreadCount();

        List<ThreadData> getThreadsBetween(int from, int to);
    }

    public interface ThreadData {

        long getId();

        boolean isFollowing();

        boolean userHasRead();

        boolean mayDelete();

        boolean mayReplyIn();

        boolean mayFollow();

        boolean mayMove();

        boolean maySticky();

        boolean mayLock();

        boolean isLocked();

        boolean isSticky();

        boolean mayView();

        String getAuthor();

        String getTopic();

        int getPostCount();

        String getLatestPostAuthor();

        Date getLatestPostTime();

        Long getLatestPostId();

        Date getCreateTime();

        boolean mayMarkAsRead();

    }

}
