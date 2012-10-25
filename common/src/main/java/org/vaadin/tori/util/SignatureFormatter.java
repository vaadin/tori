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
     * @see User#getSignatureRaw()
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
