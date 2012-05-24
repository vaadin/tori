package org.vaadin.tori.indexing;

import java.util.List;

import org.vaadin.tori.data.DataSource;

public class IndexableThreadView extends IndexableView {

    public IndexableThreadView(final List<String> arguments, final DataSource ds) {
        super(arguments, ds);
    }

    @Override
    public String getXhtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append("THREADVIEW!");
        sb.append("<ul>");
        for (final String arg : arguments) {
            sb.append("<li>" + arg);
        }
        sb.append("</ul>");
        return sb.toString();
    }

}
