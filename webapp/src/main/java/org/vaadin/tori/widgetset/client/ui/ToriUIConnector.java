package org.vaadin.tori.widgetset.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Connect(UI.class)
public class ToriUIConnector extends UIConnector implements
        ValueChangeHandler<String> {

    private final ToriUIServerRpc rpc = RpcProxy.create(ToriUIServerRpc.class,
            this);

    private int minutesInactive;
    private static final int INACTIVITY_LIMIT_MINUTES = 5;

    private static final String LOADING_STYLENAME = "loading";

    public ToriUIConnector() {
        History.addValueChangeHandler(this);

        Timer pollTimer = new Timer() {
            @Override
            public void run() {
                if (++minutesInactive == INACTIVITY_LIMIT_MINUTES) {
                    rpc.userInactive();
                }
            }
        };
        pollTimer.scheduleRepeating(60000);

        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(final NativePreviewEvent event) {
                if (minutesInactive >= INACTIVITY_LIMIT_MINUTES) {
                    rpc.userActive();
                }
                minutesInactive = 0;
            }
        });
    }

    @Override
    public void onValueChange(final ValueChangeEvent<String> event) {
        getWidget().addStyleName(LOADING_STYLENAME);
        Timer timer = new Timer() {
            @Override
            public void run() {
                if (!getConnection().hasActiveRequest()) {
                    getWidget().removeStyleName(LOADING_STYLENAME);
                    cancel();
                }
            }
        };
        timer.scheduleRepeating(50);
    }
}
