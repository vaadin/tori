package org.vaadin.tori;

import org.vaadin.navigator.Navigator;

import com.vaadin.ui.Window;

/**
 * The main window of Tori application. There may be several {@code ToriWindow}
 * instances within one {@link ToriApplication} as the application supports
 * multiple browser windows/tabs.
 */
@SuppressWarnings("serial")
public class ToriWindow extends Window {

    private final Navigator navigator = new ToriNavigator();

    public ToriWindow() {
        super("Tori");
        addComponent(navigator);
    }
}
