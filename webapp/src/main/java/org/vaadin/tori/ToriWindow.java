package org.vaadin.tori;

import org.vaadin.tori.component.Breadcrumbs;

import com.vaadin.ui.Window;

/**
 * The main window of Tori application. There may be several {@code ToriWindow}
 * instances within one {@link ToriApplication} as the application supports
 * multiple browser windows/tabs.
 */
@SuppressWarnings("serial")
public class ToriWindow extends Window {

    private final ToriNavigator navigator = new ToriNavigator();

    public ToriWindow() {
        super("Tori");
        addComponent(new Breadcrumbs(navigator));
        addComponent(navigator);
    }
}
