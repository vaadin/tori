package org.vaadin.tori.util;

import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;

/**
 * Font faces supported by Liferay forum.
 */
public enum LiferayFontFace implements FontFace {
    // @formatter:off
    ARIAL("Arial", "[font=Arial][/font]"),
    COMIC_SANS("Comic Sans", "[font=Comic Sans][/font]"),
    COURIER_NEW("Courier New", "[font=Courier New][/font]"),
    TAHOMA("Tahoma", "[font=Tahoma][/font]"),
    TIMES_NEW_ROMAN("Times New Roman", "[font=Times New Roman][/font]"),
    VERDANA("Verdana", "[font=Verdana][/font]"),
    WINGDINGS("Wingdings", "[font=Wingdings][/font]");
    // @formatter:on

    private String name;
    private String syntax;

    private LiferayFontFace(final String name, final String syntax) {
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
