package org.vaadin.tori.data.entity;

import javax.persistence.Entity;

@Entity
public class User extends AbstractEntity {

    private String displayedName;

    public void setDisplayedName(final String displayedName) {
        this.displayedName = displayedName;
    }

    public String getDisplayedName() {
        return displayedName;
    }

}
