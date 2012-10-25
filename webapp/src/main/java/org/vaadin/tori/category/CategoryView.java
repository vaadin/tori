/*
 * Copyright 2012 Vaadin Ltd.
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

package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.View;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface CategoryView extends View {
    public interface ThreadProvider {
        /**
         * The total amount of threads available in the current category.
         * 
         * @return
         * @throws DataSourceException
         */
        public int getThreadAmount() throws DataSourceException;

        /**
         * Get all threads in indices between, and including, <code>from</code>
         * and <code>to</code>.
         */
        @NonNull
        public List<DiscussionThread> getThreadsBetween(int from, int to)
                throws DataSourceException;
    }

    void displaySubCategories(@NonNull List<Category> subCategories);

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

    void displayThreads(@NonNull ThreadProvider threadProvider);

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

    void hideThreads();

    void setUserMayStartANewThread(boolean userMayStartANewThread);
}
