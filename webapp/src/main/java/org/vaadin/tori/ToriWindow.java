package org.vaadin.tori;

import org.vaadin.tori.dashboard.category.CategoryViewImpl;

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

        // TODO actual view management
        addComponent(new CategoryViewImpl());
    }
}
