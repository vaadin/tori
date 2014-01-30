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
 * Provides methods for specifying access rights to resources or certain
 * operations.
 */
public interface AuthorizationService {

    boolean mayEditCategories();

    boolean mayRearrangeCategories();

    boolean mayReportPosts();

    boolean mayFollow(Category category);

    boolean mayDelete(Category category);

    boolean mayEdit(Category category);

    boolean mayEdit(Post post);

    boolean mayReplyIn(DiscussionThread thread);

    boolean mayBan();

    boolean mayFollow(DiscussionThread currentThread);

    boolean mayDelete(Post post);

    boolean mayVote();

    boolean mayMove(DiscussionThread thread);

    boolean maySticky(DiscussionThread thread);

    boolean mayLock(DiscussionThread thread);

    boolean mayDelete(DiscussionThread thread);

    boolean mayCreateThreadIn(Category category);

    boolean mayAddFiles(Category category);

    boolean mayView(Category category);

}
