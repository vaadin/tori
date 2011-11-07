package org.vaadin.tori.data.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue
    private long id;

    public void setId(final long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
