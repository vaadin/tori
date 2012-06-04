package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ComponentConnector;
import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.ConnectorHierarchyChangeEvent;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.communication.RpcProxy;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.ui.AbstractLayoutConnector;
import com.vaadin.terminal.gwt.client.ui.layout.ElementResizeEvent;
import com.vaadin.terminal.gwt.client.ui.layout.ElementResizeListener;

@SuppressWarnings("serial")
public abstract class AbstractLazyLayoutConnector extends
        AbstractLayoutConnector {

    private final LazyLayoutServerRpc rpc = RpcProxy.create(
            LazyLayoutServerRpc.class, this);

    private final ElementResizeListener elementResizeListener = new ElementResizeListener() {
        @Override
        public void onElementResize(final ElementResizeEvent e) {
            getWidget().refreshPageHeight();
        }
    };

    private HandlerRegistration resizeHandler;

    @Override
    protected void init() {
        super.init();
        registerRpcs();
        getLayoutManager().addElementResizeListener(getWidget().getElement(),
                elementResizeListener);
        resizeHandler = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent event) {
                getWidget().refreshPageHeight();
            }
        });
    }

    @Override
    public void onUnregister() {
        getLayoutManager().removeElementResizeListener(
                getWidget().getElement(), elementResizeListener);
        resizeHandler.removeHandler();
        super.onUnregister();
    }

    abstract protected void registerRpcs();

    @Override
    protected VLazyLayout createWidget() {
        final VLazyLayout lazyLayout = GWT.create(VLazyLayout.class);
        lazyLayout.setFetcher(new VLazyLayout.ComponentFetcher() {
            @Override
            public void fetchIndices(final List<Integer> indicesToFetch) {
                rpc.fetchComponentsForIndices(indicesToFetch);
            }
        });
        return lazyLayout;
    }

    @Override
    public VLazyLayout getWidget() {
        return (VLazyLayout) super.getWidget();
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
        getWidget().setRenderDistanceMultiplier(
                getState().getRenderDistanceMultiplier());
        getWidget().setRenderDelay(getState().getRenderDelay());
    }

    /** Called on the very first {@link #onStateChanged(StateChangeEvent)} call. */
    protected void onFirstStateChanged(final StateChangeEvent stateChangeEvent) {
        // don't force an implementation
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

    protected void swapLazyComponents(final List<Integer> indicesToFetch) {
        final List<? extends Connector> components = getState().getConnectors();

        if (indicesToFetch == null || indicesToFetch.isEmpty()) {
            VConsole.error("no indices to fetch");
        } else {

            final int[] indices = new int[indicesToFetch.size()];
            final Widget[] widgets = new Widget[indicesToFetch.size()];

            int ii = 0;
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
                        final Widget widget = componentConnector.getWidget();
                        indices[ii] = i;
                        widgets[ii] = widget;
                    } else {
                        VConsole.error("LazyLayout expected an ComponentConnector; "
                                + connector.getClass().getName()
                                + " is not one");
                    }
                } catch (final IndexOutOfBoundsException e) {
                    VConsole.error(e.getMessage());
                }
                ii++;
            }
            getWidget().replaceComponents(indices, widgets);
        }
    }
}
