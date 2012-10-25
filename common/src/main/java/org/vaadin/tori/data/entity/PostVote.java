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
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class PostVote {
    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    private User voter;

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    private Post post;

    // this value is just an internal representation of the vote
    protected int vote;

    /**
     * @deprecated Do not use this getter as some data sources might do not
     *             properly assign the voter (at least for now).
     */
    @Deprecated
    public User getVoter() {
        return voter;
    }

    public void setVoter(final User voter) {
        this.voter = voter;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(final Post post) {
        this.post = post;
    }

    @Transient
    public boolean isUpvote() {
        return vote > 0;
    }

    @Transient
    public boolean isDownvote() {
        return vote < 0;
    }

    @Transient
    public void setUpvote() {
        vote = 1;
    }

    @Transient
    public void setDownvote() {
        vote = -1;
    }

    /**
     * @deprecated Avoid using this method, and instead just removing this
     *             object from the persistence. But this is left here as a
     *             convenience, for those situations you really really need it.
     */
    @Deprecated
    public void setNoVote() {
        vote = 0;
    }
}
