package org.vaadin.tori.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
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

    @Column(nullable = false)
    private int score;

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

    /** Gets the unformatted forum post. */
    public String getBodyRaw() {
        return bodyRaw;
    }

    public int getScore() {
        return score;
    }

    public void setScore(final int score) {
        this.score = score;
    }

}
