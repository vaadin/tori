package org.vaadin.tori.widgetset.client.ui.lazylayout;

import org.vaadin.tori.component.LazyLayout;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;

@Connect(LazyLayout.class)
@SuppressWarnings("serial")
public class LazyLayoutConnector extends AbstractLazyLayoutConnector {

    @Override
    protected void onFirstStateChanged(final StateChangeEvent stateChangeEvent) {
        /* eager load components some way in here */
    }

    @Override
    public LazyLayoutState getState() {
        return super.getState();
    }

}
