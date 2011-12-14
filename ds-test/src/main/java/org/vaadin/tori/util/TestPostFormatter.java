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
            list.add(new TestFontSize("Small", "[size=small][/size]"));
            list.add(new TestFontSize("Normal", "[size=normal][/size]"));
            list.add(new TestFontSize("Large", "[size=large][/size]"));
            return list;
        }

    };
    private static final FormatInfo BOLD_INFO = new FormatInfo() {
        @Override
        public String getFormatText() {
            return "[b][/b]";
        }

        @Override
        public String getFormatName() {
            return "Bold";
        }
    };
    private static final FormatInfo ITALIC_INFO = new FormatInfo() {
        @Override
        public String getFormatText() {
            return "[i][/i]";
        }

        @Override
        public String getFormatName() {
            return "Italic";
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

    @Override
    public FormatInfo getBoldInfo() {
        return BOLD_INFO;
    }

    @Override
    public FormatInfo getItalicInfo() {
        return ITALIC_INFO;
    }

    @Override
    public Collection<FormatInfo> getOtherFormattingInfo() {
        return null;
    }

}
