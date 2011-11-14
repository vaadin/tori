package org.vaadin.tori.util;

import org.vaadin.tori.data.entity.Post;

/**
 * Since formatting the posts depends on the back-end, this needs to be deferred
 * to the back-end specific projects.
 */
public interface PostFormatter {
    /**
     * Given a raw string of text for a {@link Post}, format it in the
     * appropriate way into valid XHTML.
     * 
     * @see Post#getBodyRaw()
     */
    String format(String rawPostBody);
}
