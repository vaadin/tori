package org.vaadin.tori.util;

import org.vaadin.tori.util.PostFormatter.FormatInfo;

public enum LiferayFormatInfo implements FormatInfo {
    // @formatter:off
    BOLD("Bold", "[b][/b]"), 
    ITALIC("Italic", "[i][/i]"),
    UNDERLINE("Underline", "[u][/u]"),
    STRIKETHROUGH("Strikethrough", "[s][/s]");
    // @formatter:on

    private String name;
    private String syntax;

    private LiferayFormatInfo(final String name, final String syntax) {
        this.name = name;
        this.syntax = syntax;
    }

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public String getFormatSyntax() {
        return syntax;
    }

}
