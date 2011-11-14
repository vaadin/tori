package org.vaadin.tori.util;

public class TestPostFormatter implements PostFormatter {

    @Override
    public String format(final String rawPostBody) {
        return rawPostBody.replace("<", "&lt;").replace(">", "&gt;")
                .replace("[b]", "<b>").replace("[/b]", "</b>");
    }

}
