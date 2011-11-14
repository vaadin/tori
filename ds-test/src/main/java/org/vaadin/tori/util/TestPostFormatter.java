package org.vaadin.tori.util;

public class TestPostFormatter implements PostFormatter {

    @Override
    public String format(final String rawPostBody) {
        return rawPostBody;
    }

}
