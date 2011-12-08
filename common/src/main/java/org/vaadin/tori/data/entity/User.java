package org.vaadin.tori.data.entity;

import javax.annotation.CheckForNull;
import javax.persistence.Column;
import javax.persistence.Entity;

import org.vaadin.tori.util.SignatureFormatter;

@Entity
public class User extends AbstractEntity {

    @Column(nullable = false)
    private String displayedName;

    @Column(nullable = true)
    @CheckForNull
    private String avatarUrl;

    @Column(nullable = true)
    @CheckForNull
    private String rawSignature;

    public void setDisplayedName(final String displayedName) {
        this.displayedName = displayedName;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    /**
     * Set the url for the avatar image for this user. A <code>null</code>
     * argument indicates that this user has no personal avatar.
     */
    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * The URL to the avatar image for this user. May return <code>null</code>.
     */
    @CheckForNull
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Whether this object represents a user that is not logged in.
     */
    public boolean isAnonymous() {
        return false;
    }

    /**
     * Get the unformatted signature for this user. <code>null</code> means that
     * the user has no signature.
     * 
     * @see SignatureFormatter#format(String)
     */
    @CheckForNull
    public String getSignatureRaw() {
        return rawSignature;
    }

    /**
     * Set the unformatted signature for this user. <code>null</code> clears the
     * user's signature.
     */
    public void setRawSignature(final String rawSignature) {
        this.rawSignature = rawSignature;
    }
}
