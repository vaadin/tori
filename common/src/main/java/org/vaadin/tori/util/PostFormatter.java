package org.vaadin.tori.util;

import org.vaadin.tori.data.entity.Post;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Since formatting the posts depends on the back-end, this needs to be deferred
 * to the back-end specific projects.
 */
public interface PostFormatter {
    /**
     * Given a raw string of text for a {@link Post}, format it in the
     * appropriate way into valid XHTML.
     * <p/>
     * <strong>Note:</strong> make sure to sanitize the raw post for possible
     * XHTML, if you don't want users to be able to format posts with XHTML,
     * risking XSS and other security attacks.
     * 
     * @return The XHTML to be rendered as-is.
     * @see Post#getBodyRaw()
     */
    @NonNull
    String format(@NonNull String rawPostBody);

    /**
     * The returned string should be an XHTML-formatted explanation of the
     * formatting currently being used to render the posts.
     */
    @NonNull
    String getFormattingSyntaxXhtml();
}
