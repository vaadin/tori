/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.util.SignatureFormatter;

import edu.umd.cs.findbugs.annotations.CheckForNull;

@Entity
public class User extends AbstractEntity {

    @Transient
    @CheckForNull
    private Object originalUserObject;

    @Column(nullable = false)
    private String displayedName;

    @Column(nullable = true)
    @CheckForNull
    private String avatarUrl;

    @Column(nullable = true)
    @CheckForNull
    private String rawSignature;

    @Column(nullable = false)
    private boolean banned;

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

    public boolean isBanned() {
        return banned;
    }

    /**
     * @deprecated Don't call this method directly, use
     *             {@link DataSource#ban(User)} or
     *             {@link DataSource#unban(User)} instead.
     */
    @Deprecated
    public void setBanned(final boolean banned) {
        this.banned = banned;
    }

    /**
     * The original user object provided by your
     * {@link org.vaadin.tori.data.DataSource DataSource}. May return
     * <code>null</code>.
     */
    public Object getOriginalUserObject() {
        return originalUserObject;
    }

    public void setOriginalUserObject(final Object originalUserObject) {
        this.originalUserObject = originalUserObject;
    }
}
