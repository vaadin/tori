package org.vaadin.tori.component;

import com.vaadin.ui.Label;

/**
 * Label for displaying HTML headings ({@code h1, h2, etc.}).
 */
@SuppressWarnings("serial")
public class HeadingLabel extends Label {

    public enum HeadingLevel {
        H1, H2, H3, H4
    }

    public HeadingLabel(final String content, final HeadingLevel level) {
        super(content);
        addStyleName(level.name().toLowerCase());
    }

}
