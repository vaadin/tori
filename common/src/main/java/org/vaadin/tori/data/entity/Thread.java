package org.vaadin.tori.data.entity;

public class Thread {

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

}
