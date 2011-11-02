package org.vaadin.tori;

import org.vaadin.navigator.Navigator;
import org.vaadin.navigator.Navigator.NavigableApplication;

import com.vaadin.Application;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ToriApplication extends Application implements
        NavigableApplication {

    @Override
    public void init() {
        final Window mainWindow = new ToriWindow();
        setMainWindow(mainWindow);
    }

    @Override
    public Window getWindow(final String name) {
        // Delegate the multiple browser window/tab handling to Navigator
        return Navigator.getWindow(this, name, super.getWindow(name));
    }

    @Override
    public Window createNewWindow() {
        return new ToriWindow();
    }

}
