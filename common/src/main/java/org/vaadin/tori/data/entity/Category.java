package org.vaadin.tori.data.entity;

public class Category {

    private String name;
    private String description;
    private long id;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
