package org.vaadin.tori;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ToriEditUI extends UI {

    @Override
    protected void init(final VaadinRequest request) {
        addComponent(new Label("TODO: link with edit mode"));
    }

}
