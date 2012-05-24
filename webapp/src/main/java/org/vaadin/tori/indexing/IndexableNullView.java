package org.vaadin.tori.indexing;

import java.util.List;

import org.vaadin.tori.data.DataSource;

public class IndexableNullView extends IndexableView {

    public IndexableNullView(final List<String> arguments, final DataSource ds) {
        super(arguments, ds);
    }

    @Override
    public String getXhtml() {
        return "";
    }

}
