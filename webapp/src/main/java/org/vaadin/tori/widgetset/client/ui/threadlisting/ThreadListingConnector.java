package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.List;
import java.util.Map;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingWidget.Fetcher;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(org.vaadin.tori.component.thread.ThreadListing2.class)
public class ThreadListingConnector extends AbstractComponentConnector {
    private static final long serialVersionUID = 6535780735129905996L;

    private final ThreadListingServerRpc rpc = RpcProxy.create(
            ThreadListingServerRpc.class, this);

    private final Fetcher fetcher = new Fetcher() {
        @Override
        public void fetchIndices(final List<Integer> indicesToFetch) {
            rpc.fetchComponentsForIndices(indicesToFetch);
        }

        @Override
        public void fetchControlsFor(final int rowIndex) {
            rpc.fetchControlsForIndex(rowIndex);
        }
    };

    @Override
    protected void init() {
        super.init();

        registerRpc(ThreadListingClientRpc.class, new ThreadListingClientRpc() {
            @Override
            public void sendComponents(final Map<Integer, RowInfo> rows) {
                swapLazyComponents(rows);
            }

            @Override
            public void sendControls(final ControlInfo controlInfo) {
                getWidget().setPopupControls(controlInfo);
            }
        });
    }

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        getWidget().attachScrollHandlersIfNeeded(
                getConnection().getRootConnector().getWidget());

        final int rows = getState().rows;
        if (rows == ThreadListingState.UNINITIALIZED_ROWS) {
            throw new IllegalStateException("Row amount not set on init");
        }
        getWidget().init(rows, getState().preloadedRows, fetcher);
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(ThreadListingWidget.class);
    }

    @Override
    public ThreadListingWidget getWidget() {
        return (ThreadListingWidget) super.getWidget();
    }

    @Override
    public ThreadListingState getState() {
        return (ThreadListingState) super.getState();
    }

    private void swapLazyComponents(final Map<Integer, RowInfo> rows) {
        if (rows == null || rows.isEmpty()) {
            VConsole.error("No thread rows to swap in (unnecessary method call)");
        } else {
            final Duration duration = new Duration();

            getWidget().replaceRows(rows);

            ThreadListingWidget.debug("[LazyLayout] Replace components took "
                    + duration.elapsedMillis() + "ms (n=" + rows.size() + ")");
        }
    }
}
