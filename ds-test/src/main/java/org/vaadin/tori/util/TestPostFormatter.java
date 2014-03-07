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

import java.util.Arrays;
import java.util.Collection;

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

import com.liferay.portal.kernel.parsers.bbcode.BBCodeTranslatorUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.parsers.bbcode.HtmlBBCodeTranslatorImpl;
import com.liferay.portal.util.HtmlImpl;

public class TestPostFormatter implements PostFormatter {

    private static final FontsInfo FONTS_INFO = new FontsInfo() {

        @Override
        public Collection<FontFace> getFontFaces() {
            return Arrays.asList((FontFace[]) TestFontFace.values());
        }

        @Override
        public Collection<FontSize> getFontSizes() {
            return Arrays.asList((FontSize[]) TestFontSize.values());
        }

    };

    private final BBCodeTranslatorUtil bbCodeTranslatorUtil = new BBCodeTranslatorUtil();

    public TestPostFormatter() {
        bbCodeTranslatorUtil
                .setBBCodeTranslator(new HtmlBBCodeTranslatorImpl());
        new HtmlUtil().setHtml(new HtmlImpl());
    }

    @Override
    public String format(final Post post) {
        String msgBody = post.getBodyRaw();
        if (post.isFormatBBCode()) {
            msgBody = BBCodeTranslatorUtil.getHTML(msgBody);
        }
        return msgBody;
    }

    @Override
    public FontsInfo getFontsInfo() {
        return FONTS_INFO;
    }

    @Override
    public String getQuote(final Post postToQuote) {
        if (postToQuote == null) {
            return "";
        }
        return String.format("[quote=%s]%s[/quote]\n", postToQuote.getAuthor()
                .getDisplayedName(), postToQuote.getBodyRaw());
    }

    public enum TestFontFace implements FontFace {
        // @formatter:off
        ARIAL("Arial"),
        COMIC_SANS("Comic Sans MS"),
        COURIER_NEW("Courier New"),
        TAHOMA("Tahoma"),
        TIMES_NEW_ROMAN("Times New Roman"),
        VERDANA("Verdana");
        // @formatter:on

        private final String name;

        private TestFontFace(final String name) {
            this.name = name;
        }

        @Override
        public String getFontName() {
            return name;
        }
    }

    public enum TestFontSize implements FontSize {
        // @formatter:off
        SIZE1("1", "10px"),
        SIZE2("2", "12px"),
        SIZE3("3", "16px"),
        SIZE4("4", "18px"),
        SIZE5("5", "24px"),
        SIZE6("6", "32px"),
        SIZE7("7", "48px");
        // @formatter:on

        private final String name;
        private final String value;

        private TestFontSize(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getFontSizeName() {
            return name;
        }

        @Override
        public String getFontSizeValue() {
            return value;
        }

    }
}
