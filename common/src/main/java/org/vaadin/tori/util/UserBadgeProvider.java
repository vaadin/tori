package org.vaadin.tori.util;

import org.vaadin.tori.data.entity.User;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/** An injected interface responsible for */
public interface UserBadgeProvider {
    /**
     * Get the badge to display for the given user.
     * 
     * @return The badge to show in HTML format, or <code>null</code> if no
     *         badges should be shown.
     */
    @CheckForNull
    public String getHtmlBadgeFor(@NonNull User user);
}
