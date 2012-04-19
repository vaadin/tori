package org.vaadin.tori.widgetset.client.ui.lazylayout;

import org.vaadin.tori.component.LazyLayout2;
import org.vaadin.tori.widgetset.client.ui.lazylayout.VLazyLayout2.FlowPane;

import com.google.gwt.core.client.GWT;
import com.vaadin.terminal.gwt.client.ComponentConnector;
import com.vaadin.terminal.gwt.client.ConnectorHierarchyChangeEvent;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.ui.AbstractLayoutConnector;
import com.vaadin.terminal.gwt.client.ui.Connect;

@Connect(LazyLayout2.class)
@SuppressWarnings("serial")
public class LazyLayoutConnector extends AbstractLayoutConnector {

    @Override
    protected VLazyLayout2 createWidget() {
        return GWT.create(VLazyLayout2.class);
    }

    @Override
    public VLazyLayout2 getWidget() {
        return (VLazyLayout2) super.getWidget();
    }

    @Override
    public LazyLayoutState getState() {
        return (LazyLayoutState) super.getState();
    }

    @Override
    public void updateCaption(final ComponentConnector connector) {
        // not supported
    }

    @Override
    public void onConnectorHierarchyChange(
            final ConnectorHierarchyChangeEvent event) {
        super.onConnectorHierarchyChange(event);

        int index = 0;
        final FlowPane cssLayoutWidgetContainer = getWidget().panel;
        for (final ComponentConnector child : getChildren()) {
            cssLayoutWidgetContainer.addOrMove(child.getWidget(), index++);
        }

        for (final ComponentConnector child : event.getOldChildren()) {
            if (child.getParent() == this) {
                // Skip current children
                continue;
            }
            cssLayoutWidgetContainer.remove(child.getWidget());
        }
    }

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        getWidget()
                .setComponentsAmount(getState().getTotalAmountOfComponents());
    }
}
