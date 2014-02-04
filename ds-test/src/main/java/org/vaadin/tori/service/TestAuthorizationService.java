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

import java.util.HashMap;
import java.util.Map;

public class TestAuthorizationService implements DebugAuthorizationService {

    private boolean isCategoryAdministrator = true;
    private boolean mayReportPosts = true;
    private final Map<Long, Boolean> mayViewCategory = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayFollowCategory = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayDeleteCategory = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayEditCategory = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayReplyInThread = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayAddFilesInThread = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayEditPost = new HashMap<Long, Boolean>();
    private boolean mayBan = true;
    private final Map<Long, Boolean> mayFollow = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayDelete = new HashMap<Long, Boolean>();
    private boolean mayVote = true;
    private final Map<Long, Boolean> mayMove = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> maySticky = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayLock = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayDeleteThread = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> mayCreateThread = new HashMap<Long, Boolean>();

    @Override
    public boolean mayEditCategories() {
        return isCategoryAdministrator;
    }

    @Override
    public boolean mayRearrangeCategories() {
        return isCategoryAdministrator;
    }

    @Override
    public void setMayEditCategories(final boolean b) {
        isCategoryAdministrator = b;
    }

    @Override
    public boolean mayReportPosts() {
        return mayReportPosts;
    }

    @Override
    public void setMayReportPosts(final boolean b) {
        mayReportPosts = b;
    }

    @Override
    public void setMayFollowCategory(Long categoryId, final boolean b) {
        mayFollowCategory.put(categoryId, b);
    }

    @Override
    public boolean mayFollowCategory(Long categoryId) {
        return get(mayFollowCategory, categoryId, true);
    }

    @Override
    public boolean mayDeleteCategory(Long categoryId) {
        return get(mayDeleteCategory, categoryId, true);
    }

    @Override
    public void setMayDeleteCategory(Long categoryId, final boolean b) {
        mayDeleteCategory.put(categoryId, b);
    }

    @Override
    public boolean mayEditCategory(Long categoryId) {
        return get(mayEditCategory, categoryId, true);
    }

    @Override
    public void setMayEditCategory(Long categoryId, final boolean b) {
        mayEditCategory.put(categoryId, b);
    }

    private static boolean get(final Map<Long, Boolean> rights, Long key,
            final boolean defaultValue) {
        final Boolean value = rights.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public boolean mayEditPost(long postId) {
        return get(mayEditPost, postId, true);
    }

    @Override
    public void setMayEditPost(long postId, final boolean b) {
        mayEditPost.put(postId, b);
    }

    @Override
    public boolean mayReplyInThread(long threadId) {
        return get(mayReplyInThread, threadId, true);
    }

    @Override
    public boolean mayAddFilesInCategory(Long categoryId) {
        return get(mayAddFilesInThread, categoryId, true);
    }

    @Override
    public void setMayReplyInThread(long threadId, final boolean b) {
        mayReplyInThread.put(threadId, b);
    }

    @Override
    public void setMayAddFilesInCategory(Long categoryId, final boolean b) {
        mayAddFilesInThread.put(categoryId, b);
    }

    @Override
    public boolean mayBan() {
        return mayBan;
    }

    @Override
    public void setMayBan(final boolean b) {
        mayBan = b;
    }

    @Override
    public boolean mayFollowThread(long threadId) {
        return get(mayFollow, threadId, true);
    }

    @Override
    public void setMayFollowThread(long threadId, final boolean b) {
        mayFollow.put(threadId, b);
    }

    @Override
    public boolean mayDeletePost(long postId) {
        return get(mayDelete, postId, true);
    }

    @Override
    public void setMayDeletePost(long postId, final boolean b) {
        mayDelete.put(postId, b);
    }

    @Override
    public boolean mayVote() {
        return mayVote;
    }

    @Override
    public void setMayVote(final boolean b) {
        mayVote = b;
    }

    @Override
    public boolean mayMoveThreadInCategory(Long categoryId) {
        return get(mayMove, categoryId, true);
    }

    @Override
    public void setMayMoveThreadInCategory(Long categoryId, final boolean b) {
        mayMove.put(categoryId, b);
    }

    @Override
    public boolean mayStickyThreadInCategory(Long categoryId) {
        return get(maySticky, categoryId, true);
    }

    @Override
    public void setMayStickyThreadInCategory(Long categoryId, final boolean b) {
        maySticky.put(categoryId, b);
    }

    @Override
    public boolean mayLockThreadInCategory(Long categoryId) {
        return get(mayLock, categoryId, true);
    }

    @Override
    public void setMayLockThreadInCategory(Long categoryId, final boolean b) {
        mayLock.put(categoryId, b);
    }

    @Override
    public boolean mayDeleteThread(long threadId) {
        return get(mayDeleteThread, threadId, true);
    }

    @Override
    public void setMayDeleteThread(long threadId, final boolean b) {
        mayDeleteThread.put(threadId, b);
    }

    @Override
    public boolean mayCreateThreadInCategory(Long categoryId) {
        return get(mayCreateThread, categoryId, true);
    }

    @Override
    public void setMayCreateThreadInCategory(Long categoryId, final boolean b) {
        mayCreateThread.put(categoryId, b);
    }

    @Override
    public boolean mayViewCategory(Long categoryId) {
        return get(mayViewCategory, categoryId, true);
    }

    @Override
    public void setMayViewCategory(Long categoryId, final boolean b) {
        mayViewCategory.put(categoryId, b);
    }

}
