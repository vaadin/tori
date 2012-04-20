package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import org.vaadin.tori.component.LazyLayout2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ComponentConnector;
import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.communication.RpcProxy;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.ui.AbstractComponentConnector;
import com.vaadin.terminal.gwt.client.ui.AbstractLayoutConnector;
import com.vaadin.terminal.gwt.client.ui.Connect;

@Connect(LazyLayout2.class)
@SuppressWarnings("serial")
public class LazyLayoutConnector extends AbstractLayoutConnector {

    private final LazyLayoutServerRpc rpc = RpcProxy.create(
            LazyLayoutServerRpc.class, this);

    @Override
    protected void init() {
        super.init();
        registerRpc(LazyLayoutClientRpc.class, new LazyLayoutClientRpc() {
            @Override
            public void renderComponents(final List<Integer> indicesToFetch) {
                final List<? extends Connector> components = getState()
                        .getComponents();

                if (indicesToFetch == null || indicesToFetch.isEmpty()) {
                    VConsole.error("no indices to fetch");
                } else {
                    for (final int i : indicesToFetch) {
                        try {
                            final Connector connector = components.get(i);
                            if (connector == null) {
                                VConsole.error("No component for index "
                                        + i
                                        + " in state, even if it should be there");
                                continue;
                            }
                            VConsole.error("YAY! " + i);

                            if (connector instanceof AbstractComponentConnector) {
                                final AbstractComponentConnector componentConnector = (AbstractComponentConnector) connector;
                                getWidget().put(componentConnector.getWidget(),
                                        i);
                            }
                        } catch (final IndexOutOfBoundsException e) {
                            VConsole.error(e.getMessage());
                        }
                    }
                }
            }
        });
    }

    @Override
    protected VLazyLayout2 createWidget() {
        final VLazyLayout2 lazyLayout = GWT.create(VLazyLayout2.class);
        lazyLayout.setFetcher(new VLazyLayout2.ComponentFetcher() {
            @Override
            public void fetchIndices(final List<Integer> indicesToFetch) {
                rpc.fetchComponentsForIndices(indicesToFetch);
            }
        });
        return lazyLayout;
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

    /*-
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
     */

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        attachScrollHandlersIfNeeded();

        getWidget().setPlaceholderSize(getState().getPlaceholderHeight(),
                getState().getPlaceholderWidth());
        getWidget()
                .setComponentsAmount(getState().getTotalAmountOfComponents());
        getWidget().setRenderDistance(getState().getRenderDistance());
        getWidget().setRenderDelay(getState().getRenderDelay());
    }

    private void attachScrollHandlersIfNeeded() {
        final Widget rootWidget = getConnection().getRootConnector()
                .getWidget();
        getWidget().attachScrollHandlersIfNeeded(rootWidget);
    }

}
