package org.vaadin.tori.widgetset.client.ui.expandingtextarea;

import org.vaadin.hene.expandingtextarea.widgetset.client.ui.VExpandingTextArea;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VToriExpandingTextArea extends VExpandingTextArea {

    public static final String ATTR_BLUR_REQUESTED = "blurRequested";

    @Override
    public void updateFromUIDL(final UIDL uidl,
            final ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);

        if (uidl.getBooleanAttribute(ATTR_BLUR_REQUESTED)) {
            blur();
        }
    }

    private void blur() {
        setFocus(false);
    }
}
