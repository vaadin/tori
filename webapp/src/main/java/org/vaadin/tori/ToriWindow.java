package org.vaadin.tori;

import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * The main window of Tori application. There may be several {@code ToriWindow}
 * instances within one {@link ToriApplication} as the application supports
 * multiple browser windows/tabs.
 */
@SuppressWarnings("serial")
public class ToriWindow extends Window {

    public ToriWindow() {
        super("Tori");

        addComponent(new Label("Hello Tori user"));
    }
}
