package org.vaadin.tori.data.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Post extends AbstractEntity {
    private User author;

    @ManyToOne
    private Thread thread;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

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

    public void setThread(final Thread thread) {
        this.thread = thread;
    }

    public Thread getThread() {
        return thread;
    }

}
