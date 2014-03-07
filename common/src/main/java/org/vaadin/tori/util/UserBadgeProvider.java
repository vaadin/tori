package org.vaadin.tori.util;

import org.vaadin.tori.data.entity.User;

/** An injected interface responsible for */
public interface UserBadgeProvider {
    /**
     * Get the badge to display for the given user.
     * 
     * @return The badge to show in HTML format, or <code>null</code> if no
     *         badges should be shown.
     */
    public String getHtmlBadgeFor(User user);
}
