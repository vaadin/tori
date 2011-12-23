package org.vaadin.tori.util;

import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

public enum LiferayFontSize implements FontSize {
    // @formatter:off
    SIZE1("1", "[size=1][/size]"),
    SIZE2("2", "[size=2][/size]"),
    SIZE3("3", "[size=3][/size]"),
    SIZE4("4", "[size=4][/size]"),
    SIZE5("5", "[size=5][/size]"),
    SIZE6("6", "[size=6][/size]"),
    SIZE7("7", "[size=7][/size]");
    // @formatter:on

    private String name;
    private String syntax;

    private LiferayFontSize(final String name, final String syntax) {
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
