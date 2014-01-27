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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

import com.liferay.portal.kernel.parsers.bbcode.BBCodeTranslatorUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;

public class LiferayPostFormatter implements PostFormatter, PortletRequestAware {

    private static final Logger log = Logger
            .getLogger(LiferayPostFormatter.class);

    private static Collection<FontFace> fontFaces;
    private static Collection<FontSize> fontSizes;
    private Map<String, String> postReplacements;

    private ThemeDisplay themeDisplay;

    static {
        fontFaces = new ArrayList<FontFace>(Arrays.asList(LiferayFontFace
                .values()));
        fontSizes = new ArrayList<FontSize>(Arrays.asList(LiferayFontSize
                .values()));
    }

    @Override
    public String format(final String rawPostBody) {
        try {
            String body = BBCodeTranslatorUtil.getHTML(rawPostBody.trim());
            body = StringUtil.replace(body, "@theme_images_path@/emoticons",
                    themeDisplay.getPathThemeImages() + "/emoticons");

            if (postReplacements != null) {
                for (final Entry<String, String> entry : postReplacements
                        .entrySet()) {
                    try {
                        body = body
                                .replaceAll(entry.getKey(), entry.getValue());
                    } catch (final PatternSyntaxException e) {
                        log.warn(
                                "Invalid replacement regex pattern: "
                                        + entry.getKey(), e);
                    }
                }
            }
            return body;
        } catch (final Exception e) {
            log.debug("Couldn't parse the given post body: " + rawPostBody);
        }
        return rawPostBody;
    }

    @Override
    public FontsInfo getFontsInfo() {
        return new FontsInfo() {
            @Override
            public Collection<FontFace> getFontFaces() {
                return fontFaces;
            }

            @Override
            public Collection<FontSize> getFontSizes() {
                return fontSizes;
            }
        };
    }

    @Override
    public String getQuote(final Post postToQuote) {
        if (postToQuote == null) {
            return "";
        }
        return String.format("[quote=%s]%s[/quote]\n", postToQuote.getAuthor()
                .getDisplayedName(), postToQuote.getBodyRaw());
    }

    @Override
    public final void setPostReplacements(
            final Map<String, String> postReplacements) {
        this.postReplacements = postReplacements;
    }

    @Override
    public void setRequest(PortletRequest request) {
        themeDisplay = (ThemeDisplay) request
                .getAttribute(WebKeys.THEME_DISPLAY);
    }
}
