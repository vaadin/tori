package org.vaadin.tori;

import com.vaadin.server.WrappedRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ToriEditUI extends UI {

    @Override
    protected void init(final WrappedRequest request) {
        addComponent(new Label("TODO: link with edit mode"));
    }

}
