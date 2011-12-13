package org.vaadin.tori.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

public class TestPostFormatter implements PostFormatter {

    private static class TestFontFace implements FontFace {
        private final String name;
        private final String syntax;
        private final boolean isDefault;

        public TestFontFace(final String name, final String syntax,
                final boolean isDefault) {
            this.name = name;
            this.syntax = syntax;
            this.isDefault = isDefault;
        }

        @Override
        public String getFontName() {
            return name;
        }

        @Override
        public String getFontSyntax() {
            return syntax;
        }

        @Override
        public boolean showAsDefault() {
            return isDefault;
        }
    }

    private static class TestFontSize implements FontSize {
        private final String name;
        private final String syntax;
        private final boolean isDefault;

        public TestFontSize(final String name, final String syntax,
                final boolean isDefault) {
            this.name = name;
            this.syntax = syntax;
            this.isDefault = isDefault;
        }

        @Override
        public String getFontSizeName() {
            return name;
        }

        @Override
        public String getFontSizeSyntax() {
            return syntax;
        }

        @Override
        public boolean showAsDefault() {
            return isDefault;
        }
    }

    private static final FontsInfo FONTS_INFO = new FontsInfo() {

        @Override
        public Collection<FontFace> getFontFaces() {
            final List<FontFace> list = new ArrayList<FontFace>();
            list.add(new TestFontFace("SANSSERIF!", "[font=sans-serif][/font]",
                    true));
            list.add(new TestFontFace("serif :(", "[font=serif][/font]", false));
            list.add(new TestFontFace("monospace", "[font=monospace][/font]",
                    false));
            return list;
        }

        @Override
        public Collection<FontSize> getFontSizes() {
            final List<FontSize> list = new ArrayList<FontSize>();
            list.add(new TestFontSize("Small", "[size=small][/size]", false));
            list.add(new TestFontSize("Normal", "[size=normal][/size]", true));
            list.add(new TestFontSize("Large", "[size=large][/size]", false));
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
    public String getFormattingSyntaxXhtml() {
        return "[b]bold[/b] &raquo; <b>bold</b>";
    }

    @Override
    public FontsInfo getFontsInfo() {
        return FONTS_INFO;
    }

}
