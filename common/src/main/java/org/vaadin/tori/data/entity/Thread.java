package org.vaadin.tori.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Thread {

    @Id
    @GeneratedValue
    private long id;
    private String topic;

    public Thread() {
    }

    public Thread(final String topic) {
        this.topic = topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
