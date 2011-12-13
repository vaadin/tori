package org.vaadin.tori.util;

import java.util.Collection;

public class TestPostFormatter implements PostFormatter {

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
        return new FontsInfo() {
            @Override
            public Collection<FontFace> getFontFaces() {
                // FIXME
                System.out
                        .println("LiferayPostFormatter.getFontsInfo().new FontsInfo() {...}.getFontFaces()");
                System.out.println("not yet implemented");
                return null;
            }

            @Override
            public Collection<FontSize> getFontSizes() {
                // FIXME
                System.out
                        .println("LiferayPostFormatter.getFontsInfo().new FontsInfo() {...}.getFontSizes()");
                System.out.println("not yet implemented");
                return null;
            }
        };
    }

}
