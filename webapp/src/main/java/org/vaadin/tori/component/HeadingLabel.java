package org.vaadin.tori.component;

import com.vaadin.ui.Label;

/**
 * Label for displaying HTML headings ({@code h1, h2, etc.}).
 */
@SuppressWarnings("serial")
public class HeadingLabel extends Label {

    public enum HeadingLevel {
        H1, H2, H3, H4, H5, H6
    }

    public HeadingLabel(final String content, final HeadingLevel level) {
        super();
        final String headingTag = level.name().toLowerCase();
        final String headingHtml = String.format("<%s>%s</%s>", headingTag,
                content, headingTag);

        setContentMode(Label.CONTENT_XHTML);
        setValue(headingHtml);
    }

}
