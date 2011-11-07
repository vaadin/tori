package org.vaadin.tori.data.entity;

public class Thread {

    private String topic;
    private long id;

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
