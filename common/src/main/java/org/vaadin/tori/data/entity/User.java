package org.vaadin.tori.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class User extends AbstractEntity {

    @Column(nullable = false)
    private String displayedName;

    @Column(nullable = true)
    private String avatarUrl;

    public void setDisplayedName(final String displayedName) {
        this.displayedName = displayedName;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * The URL to the avatar image for this user. May return <code>null</code>.
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Whether this object represents a user that is not logged in.
     */
    public boolean isAnonymous() {
        return false;
    }
}
