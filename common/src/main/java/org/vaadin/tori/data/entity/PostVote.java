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
    @JoinColumn(nullable = false, unique = true)
    private User voter;

    @Id
    @ManyToOne
    @JoinColumn(nullable = false, unique = true)
    private Post post;

    // this value is just an internal representation of the vote
    protected int vote;

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
