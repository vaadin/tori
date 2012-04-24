package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ComponentConnector;
import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.ConnectorHierarchyChangeEvent;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.communication.RpcProxy;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.ui.AbstractLayoutConnector;
import com.vaadin.terminal.gwt.client.ui.PostLayoutListener;

@SuppressWarnings("serial")
public abstract class AbstractLazyLayoutConnector extends
        AbstractLayoutConnector implements PostLayoutListener {

    private final LazyLayoutServerRpc rpc = RpcProxy.create(
            LazyLayoutServerRpc.class, this);
    private boolean eagerLoadHasBeenDone;

    @Override
    protected void init() {
        super.init();
        registerRpc(LazyLayoutClientRpc.class, new LazyLayoutClientRpc() {
            @Override
            public void renderComponents(final List<Integer> indicesToFetch) {
                getWidget().updateScrollAdjustmentReference();
                swapLazyComponents(indicesToFetch);
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

        if (!eagerLoadHasBeenDone) {
            final List<Integer> componentIndices = getEagerlyLoadedComponentIndices();
            swapLazyComponents(componentIndices);
            eagerLoadHasBeenDone = true;
        }
    }

    /**
     * <b>Warning:</b> Calling this method at any other point of time than the
     * initialization, will result in unintended answers.
     * 
     * @return
     */
    private List<Integer> getEagerlyLoadedComponentIndices() {
        /*
         * we take advantage of the fact that all components have a slot in the
         * connector list, but they're null if the data hasn't been transferred
         * over. Thus - each non-null element in the components list is eagerly
         * loaded.
         */

        int i = 0;
        final List<Integer> indices = new ArrayList<Integer>();
        for (final Connector connector : getState().getComponents()) {
            if (connector != null) {
                indices.add(i);
            }
            i++;
        }
        return indices;
    }

    private void attachScrollHandlersIfNeeded() {
        final Widget rootWidget = getConnection().getRootConnector()
                .getWidget();
        getWidget().attachScrollHandlersIfNeeded(rootWidget);
    }

    @Override
    public void onConnectorHierarchyChange(
            final ConnectorHierarchyChangeEvent event) {
    }

    private void swapLazyComponents(final List<Integer> indicesToFetch) {
        final List<? extends Connector> components = getState().getComponents();

        if (indicesToFetch == null || indicesToFetch.isEmpty()) {
            VConsole.error("no indices to fetch");
        } else {
            for (final int i : indicesToFetch) {
                try {
                    final Connector connector = components.get(i);
                    if (connector == null) {
                        VConsole.error("No component for index " + i
                                + " in state, even if it should be there");
                        continue;
                    }

                    if (connector instanceof ComponentConnector) {
                        final ComponentConnector componentConnector = (ComponentConnector) connector;
                        getWidget().replacePlaceholderWith(
                                componentConnector.getWidget(), i);
                    } else {
                        VConsole.error("LazyLayout expected an ComponentConnector; "
                                + connector.getClass().getName()
                                + " is not one");
                    }
                } catch (final IndexOutOfBoundsException e) {
                    VConsole.error(e.getMessage());
                }
            }
        }
    }

    @Override
    public void postLayout() {
        getWidget().fixScrollbar();
    }
}
