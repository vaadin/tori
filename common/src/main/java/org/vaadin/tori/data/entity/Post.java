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

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Post extends AbstractEntity {

    @JoinColumn(nullable = false)
    private User author;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private DiscussionThread thread;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date time;

    @Lob
    @Column(nullable = false, name = "body")
    private String bodyRaw;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<PostVote> postVotes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Attachment> attachments;

    public void setAuthor(final User author) {
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }

    public void setTime(final Date time) {
        this.time = (Date) time.clone();
    }

    public Date getTime() {
        return (Date) time.clone();
    }

    public void setThread(final DiscussionThread thread) {
        this.thread = thread;
    }

    public DiscussionThread getThread() {
        return thread;
    }

    public void setBodyRaw(final String bodyRaw) {
        this.bodyRaw = bodyRaw;
    }

    public List<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(List<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    /** Gets the unformatted forum post. */
    public String getBodyRaw() {
        return bodyRaw;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<Attachment> attachments) {
        // fix bidirectional relationship
        for (final Attachment attachment : attachments) {
            attachment.setPost(this);
        }
        this.attachments = attachments;
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

}
