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

package org.vaadin.tori.data.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

/**
 * @deprecated This class is not originally designed to be used in the
 *             application as an object. It's just a JPA helper for testing.
 */
@Entity
@Deprecated
public class Following {

    @Id
    @JoinColumn(nullable = false)
    private User follower;

    @Id
    @JoinColumn(nullable = false)
    private DiscussionThread thread;

    public User getFollower() {
        return follower;
    }

    public void setFollower(final User follower) {
        this.follower = follower;
    }

    public DiscussionThread getThread() {
        return thread;
    }

    public void setThread(final DiscussionThread thread) {
        this.thread = thread;
    }
}
