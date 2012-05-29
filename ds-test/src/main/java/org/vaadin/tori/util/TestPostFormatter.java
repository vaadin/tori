package org.vaadin.tori.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;
import org.vaadin.tori.util.PostFormatter.FormatInfo;

public class TestPostFormatter implements PostFormatter {

    private static class TestFormatInfo implements FormatInfo {

        private final String formatIconThemeId;
        private final String formatName;
        private final String formatSyntax;

        public TestFormatInfo(final String formatName,
                final String formatSyntax, final String formatIconThemeId) {
            this.formatName = formatName;
            this.formatSyntax = formatSyntax;
            this.formatIconThemeId = formatIconThemeId;
        }

        @Override
        public String getFormatName() {
            return formatName;
        }

        @Override
        public String getFormatSyntax() {
            return formatSyntax;
        }

        @Override
        public String getFormatIcon() {
            return formatIconThemeId;
        }

    }

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

    private static final FormatInfo BOLD_INFO = new TestFormatInfo("Bold",
            "[b][/b]", "bold.png");
    private static final FormatInfo ITALIC_INFO = new TestFormatInfo("Italic",
            "[i][/i]", "italic.png");
    private static final Collection<? extends FormatInfo> OTHER_FORMAT_INFO = Collections
            .singleton(new TestFormatInfo("Vaadin", "}>", "vaadin.png"));

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
    public Collection<? extends FormatInfo> getOtherFormattingInfo() {
        return OTHER_FORMAT_INFO;
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
    public void setPostReplacements(Map<String, String> postReplacements) {

    }
}
