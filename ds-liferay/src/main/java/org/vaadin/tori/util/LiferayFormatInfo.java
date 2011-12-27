package org.vaadin.tori.util;

import org.vaadin.tori.util.PostFormatter.FormatInfo;

public enum LiferayFormatInfo implements FormatInfo {
    // @formatter:off
    BOLD("Bold", "[b][/b]", "bold.png"), 
    ITALIC("Italic", "[i][/i]","italic.png"),
    UNDERLINE("Underline", "[u][/u]","underline.png"),
    STRIKETHROUGH("Strikethrough", "[s][/s]","strikethrough.png");
    // @formatter:on

    private String name;
    private String syntax;
    private String icon;

    private LiferayFormatInfo(final String name, final String syntax,
            final String icon) {
        this.name = name;
        this.syntax = syntax;
        this.icon = icon;
    }

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public String getFormatSyntax() {
        return syntax;
    }

    @Override
    public String getFormatIcon() {
        return icon;
    }

}
