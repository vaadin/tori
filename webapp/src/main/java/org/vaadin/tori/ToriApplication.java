package org.vaadin.tori;

import com.vaadin.Application;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ToriApplication extends Application {

    @Override
    public void init() {
        final Window mainWindow = new ToriWindow();
        setMainWindow(mainWindow);
    }

    @Override
    public Window getWindow(final String name) {
        Window w = super.getWindow(name);
        if (w == null) {
            // User has opened a new browser window/tab -> create a new
            // ToriWindow for this window/tab.
            w = new ToriWindow();
            w.setName(name);
            addWindow(w);
        }
        return w;
    }

}
