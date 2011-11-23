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
