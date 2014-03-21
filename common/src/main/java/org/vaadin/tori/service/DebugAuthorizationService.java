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

package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

/**
 * <p>
 * If the deployed {@link AuthorizationService} is an instance of
 * <code>DebugAuthorizationService</code>, the developer will be presented with
 * additional testing controls. These controls allows the developer direct
 * manipulation of any and all values retrieved via the
 * <code>AuthorizationService</code>.
 * </p>
 * 
 * <p>
 * This interface contains a symmetric setter for each method found in
 * <code>AuthorizationService</code>
 * </p>
 */
public interface DebugAuthorizationService extends AuthorizationService {
    /** @see AuthorizationService#mayEditCategories() */
    void setMayEditCategories(boolean b);

    /** @see AuthorizationService#mayReportPosts() */
    void setMayReportPosts(boolean b);

    /** @see AuthorizationService#mayFollow(Category) */
    void setMayFollowCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayDelete(org.vaadin.tori.data.entity.Category) */
    void setMayDeleteCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayEdit(Category) */
    void setMayEditCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayEdit(Post) */
    void setMayEditPost(long postId, boolean b);

    /** @see AuthorizationService#mayReplyIn(DiscussionThread) */
    void setMayReplyInThread(long threadId, boolean b);

    /** @see AuthorizationService#mayAddFiles(DiscussionThread) */
    void setMayAddFilesInCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayBan() */
    void setMayBan(boolean b);

    /** @see AuthorizationService#mayFollow(DiscussionThread) */
    void setMayFollowThread(long threadId, boolean b);

    /** @see AuthorizationService#mayDelete(Post) */
    void setMayDeletePost(long postId, boolean b);

    /** @see AuthorizationService#mayVote() */
    void setMayVote(boolean b);

    /** @see AuthorizationService#mayMove(DiscussionThread) */
    void setMayMoveThreadInCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#maySticky(DiscussionThread) */
    void setMayStickyThreadInCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayLock(DiscussionThread) */
    void setMayLockThreadInCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayLock(DiscussionThread) */
    void setMayDeleteThread(long threadId, boolean b);

    /** @see AuthorizationService#mayCreateThreadIn(Category) */
    void setMayCreateThreadInCategory(Long categoryId, boolean b);

    /** @see AuthorizationService#mayView(Category) */
    void setMayViewCategory(Long categoryId, boolean b);

    void setMayViewThread(long threadId, boolean b);

    void setMayViewPost(long postId, boolean b);

}
