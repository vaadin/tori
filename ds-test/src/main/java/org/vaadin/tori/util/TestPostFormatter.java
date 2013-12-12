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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

public class TestPostFormatter implements PostFormatter {

    private static class TestFontFace implements FontFace {
        private final String name;
        private final String syntax;

        public TestFontFace(final String name, final String syntax) {
            this.name = name;
            this.syntax = syntax;
        }

        @Override
        public String getFontName() {
            return name;
        }

        @Override
        public String getFontSyntax() {
            return syntax;
        }
    }

    private static class TestFontSize implements FontSize {
        private final String name;
        private final String syntax;

        public TestFontSize(final String name, final String syntax) {
            this.name = name;
            this.syntax = syntax;
        }

        @Override
        public String getFontSizeName() {
            return name;
        }

        @Override
        public String getFontSizeSyntax() {
            return syntax;
        }
    }

    private static final FontsInfo FONTS_INFO = new FontsInfo() {

        @Override
        public Collection<FontFace> getFontFaces() {
            final List<FontFace> list = new ArrayList<FontFace>();
            list.add(new TestFontFace("SANSSERIF!", "[font=sans-serif][/font]"));
            list.add(new TestFontFace("serif :(", "[font=serif][/font]"));
            list.add(new TestFontFace("monospace", "[font=monospace][/font]"));
            return list;
        }

        @Override
        public Collection<FontSize> getFontSizes() {
            final List<FontSize> list = new ArrayList<FontSize>();
            list.add(new TestFontSize("1", "0.7em"));
            list.add(new TestFontSize("2", "0.8em"));
            list.add(new TestFontSize("3", "0.9em"));
            list.add(new TestFontSize("4", "1.0em"));
            list.add(new TestFontSize("5", "1.1em"));
            list.add(new TestFontSize("6", "1.3em"));
            list.add(new TestFontSize("7", "1.5em"));
            return list;
        }

    };

    @Override
    public String format(final String rawPostBody) {
        return rawPostBody.replace("<", "&lt;").replace(">", "&gt;")
                .replace("[b]", "<b>").replace("[/b]", "</b>")
                .replace("\n", "<br/>");
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
        return String.format("%s wrote:\n\n%s\n---\n", postToQuote.getAuthor()
                .getDisplayedName(), postToQuote.getBodyRaw());
    }

    @Override
    public void setPostReplacements(final Map<String, String> postReplacements) {

    }
}
