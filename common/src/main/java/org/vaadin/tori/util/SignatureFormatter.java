package org.vaadin.tori.util;

import org.vaadin.tori.data.entity.User;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Since the formatting allowed in signatures may differ from the formatting
 * allowed in a post, this interface allows the back-end to define it so.
 * <p/>
 * If the allowed formatting is always the same as in posts, it's probably a
 * good idea to let the same class implement both {@link SignatureFormatter} and
 * {@link PostFormatter}
 */
public interface SignatureFormatter {
    /**
     * Given a raw string of text for a {@link User User's} signature, format it
     * in the appropriate way into valid XHTML.
     * <p/>
     * <strong>Note:</strong> make sure to sanitize the raw signature for
     * possible XHTML, if you don't want users to be able to format posts with
     * XHTML, risking XSS and other security attacks.
     * 
     * @return The XHTML to be rendered as-is.
     * @see User#getRawSignature()
     */
    @NonNull
    String format(@NonNull String rawSignature);

    /**
     * The returned string should be an XHTML-formatted explanation of the
     * formatting currently being used to render the signatures.
     */
    @NonNull
    String getFormattingSyntaxXhtml();
}
