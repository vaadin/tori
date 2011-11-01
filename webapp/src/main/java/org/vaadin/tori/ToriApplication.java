package org.vaadin.tori;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ToriApplication extends Application {

    @Override
    public void init() {
        final Window mainWindow = new Window("Tori");
        final Label label = new Label("Hello Tori user");
        mainWindow.addComponent(label);
        setMainWindow(mainWindow);
    }

}
