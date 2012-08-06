package org.vaadin.tori.widgetset.client.ui.floatingbar;

import org.vaadin.tori.component.FloatingBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.shared.ui.Connect;
import com.vaadin.terminal.gwt.client.ComponentConnector;
import com.vaadin.terminal.gwt.client.communication.RpcProxy;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.ui.AbstractComponentContainerConnector;

@SuppressWarnings("serial")
@Connect(FloatingBar.class)
public final class FloatingBarConnector extends
        AbstractComponentContainerConnector {

    private final FloatingBarRpc rpc = RpcProxy.create(FloatingBarRpc.class,
            this);

    @Override
    protected Widget createWidget() {
        final FloatingBarWidget widget = GWT.create(FloatingBarWidget.class);
        widget.setListener(rpc);
        return widget;
    }

    @Override
    public void updateCaption(final ComponentConnector connector) {
        // NOP
    }

    @Override
    public FloatingBarState getState() {
        return (FloatingBarState) super.getState();
    }

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        final FloatingBarState state = getState();
        final FloatingBarWidget widget = (FloatingBarWidget) getWidget();

        widget.setRootWidget(getConnection().getRootConnector().getWidget());
        widget.setPortlet(state.isPortlet());
        widget.setTopAligned(state.isTopAligned());
        widget.setContentWidget(((ComponentConnector) state.getContent())
                .getWidget());
        widget.setscrollTreshold(state.getScrollThreshold());
        widget.setScrollWidget(((ComponentConnector) state.getScrollComponent())
                .getWidget());
    }
}
