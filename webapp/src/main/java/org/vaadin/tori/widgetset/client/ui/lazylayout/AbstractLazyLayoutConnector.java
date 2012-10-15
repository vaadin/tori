package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.VConsole;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.Connector;

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
        registerRpc(AbstractLazyLayoutClientRpc.class,
                new AbstractLazyLayoutClientRpc() {
                    @Override
                    public void sendComponents(
                            final Map<Integer, Connector> components) {
                        swapLazyComponents(components);
                    }
                });

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

        // order matters
        getWidget().setPlaceholderSize(getState().placeholderHeight,
                getState().placeholderWidth);
        getWidget().setAmountOfComponents(getState().amountOfComponents);
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

    private void swapLazyComponents(final Map<Integer, Connector> components) {
        if (components == null || components.isEmpty()) {
            VConsole.error("No components to swap in (unnecessary method call)");
        } else {
            final Duration duration = new Duration();

            final int[] indices = new int[components.size()];
            final Widget[] widgets = new Widget[components.size()];
            int i = 0;

            for (final Map.Entry<Integer, Connector> entry : components
                    .entrySet()) {
                final int index = entry.getKey();
                final Connector connector = entry.getValue();
                if (connector instanceof ComponentConnector) {
                    final ComponentConnector cConnector = (ComponentConnector) connector;
                    final Widget widget = cConnector.getWidget();

                    indices[i] = index;
                    widgets[i] = widget;

                    i++;

                } else {
                    VConsole.error("Expected a ComponentConnector, got something else instead (at index "
                            + index + ")");
                }
            }

            getWidget().replaceComponents(indices, widgets);

            if (VLazyLayout.DEBUG) {
                VConsole.error("[LazyLayout] Replace components took "
                        + duration.elapsedMillis() + "ms (n="
                        + components.size() + ")");
            }
        }
    }
}
