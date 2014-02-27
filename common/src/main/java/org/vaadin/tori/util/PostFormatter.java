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

import java.util.Collection;

import org.vaadin.tori.data.entity.Post;

/**
 * Since formatting the posts depends on the back-end, this needs to be deferred
 * to the back-end specific projects.
 */
public interface PostFormatter {

    public interface FontsInfo {
        public interface FontFace {

            String getFontName();
        }

        public interface FontSize {

            String getFontSizeName();

            String getFontSizeValue();
        }

        /**
         * @return Font face information. <code>null</code> is returned if the
         *         author is not allowed to change the font.
         */
        Collection<FontFace> getFontFaces();

        /**
         * @return Font size information. <code>null</code> is returned if the
         *         author is not allowed to change the font size.
         */
        Collection<FontSize> getFontSizes();
    }

    /**
     * Given a {@link Post}, format its body in the appropriate way into valid
     * XHTML.
     * <p/>
     * <strong>Note:</strong> make sure to sanitize the raw post for possible
     * XHTML, if you don't want users to be able to format posts with XHTML,
     * risking XSS and other security attacks.
     * 
     * @return The XHTML to be rendered as-is.
     * @see Post#getBodyRaw()
     */
    String format(Post rawPostBody);

    FontsInfo getFontsInfo();

    String getQuote(final Post postToQuote);

}
