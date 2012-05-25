package org.vaadin.tori.indexing;

import java.util.List;

public class IndexableNullView extends IndexableView {

    public IndexableNullView(final List<String> arguments,
            final ToriIndexableApplication application) {
        super(arguments, application);
    }

    @Override
    public String getXhtml() {
        return "";
    }

}
