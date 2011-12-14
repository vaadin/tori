package org.vaadin.tori.util;

import java.util.Collection;

import com.liferay.portlet.messageboards.util.BBCodeUtil;

public class LiferayPostFormatter implements PostFormatter {
    @Override
    public String format(final String rawPostBody) {
        return BBCodeUtil.getHTML(rawPostBody);
    }

    @Override
    public String getFormattingSyntaxXhtml() {
        // TODO
        return "<b>Not yet implemented for " + LiferayPostFormatter.class
                + ".</b>";
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

    @Override
    public FormatInfo getBoldInfo() {
        // FIXME
        System.out.println("LiferayPostFormatter.getBoldInfo()");
        System.out.println("not yet implemented");
        return null;
    }

    @Override
    public FormatInfo getItalicInfo() {
        // FIXME
        System.out.println("LiferayPostFormatter.getItalicInfo()");
        System.out.println("not yet implemented");
        return null;
    }

    @Override
    public Collection<FormatInfo> getOtherFormattingInfo() {
        // FIXME
        System.out.println("LiferayPostFormatter.getOtherFormattingInfo()");
        System.out.println("not yet implemneted");
        return null;
    }
}
