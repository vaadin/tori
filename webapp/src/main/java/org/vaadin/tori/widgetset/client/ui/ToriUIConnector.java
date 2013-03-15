package org.vaadin.tori.widgetset.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.VConsole;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;
import com.vaadin.ui.UI;

@Connect(value = UI.class, loadStyle = LoadStyle.EAGER)
public class ToriUIConnector extends UIConnector implements
        ValueChangeHandler<String> {
    private static final String LOADING_STYLENAME = "loading";
    private static final long serialVersionUID = -5688266273862180292L;

    public ToriUIConnector() {
        History.addValueChangeHandler(this);
    }

    @Override
    public void onValueChange(final ValueChangeEvent<String> event) {
        getWidget().addStyleName(LOADING_STYLENAME);
        new Timer() {
            @Override
            public void run() {
                if (!getConnection().hasActiveRequest()) {
                    VConsole.log("active requests none!");
                    getWidget().removeStyleName(LOADING_STYLENAME);
                    cancel();
                } else {
                    VConsole.log("still has requests");
                }
            }
        }.scheduleRepeating(50);
    }
}
