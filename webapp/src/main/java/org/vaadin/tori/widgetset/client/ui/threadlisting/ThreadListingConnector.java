package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.List;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(org.vaadin.tori.view.listing.thread.ThreadListing.class)
public class ThreadListingConnector extends AbstractComponentConnector {

    private final ThreadListingServerRpc rpc = RpcProxy.create(
            ThreadListingServerRpc.class, this);

    @Override
    protected void init() {
        super.init();
        getWidget().init(new ThreadListingWidget.Fetcher() {
            @Override
            public void fetchRows() {
                rpc.fetchRows();
            }
        });
        getWidget().attachScrollHandlersIfNeeded(
                getConnection().getUIConnector().getWidget());

        registerRpc(ThreadListingClientRpc.class, new ThreadListingClientRpc() {
            @Override
            public void refreshRow(final RowInfo rowInfo) {
                getWidget().replaceOpenedThreadListingRowWith(rowInfo);
            }

            @Override
            public void removeRow(int index) {
                getWidget().removeSelectedRow();
            }

            @Override
            public void sendRows(List<RowInfo> rows, int placeholders) {
                getWidget().addRows(rows, placeholders);
            }
        });
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(ThreadListingWidget.class);
    }

    @Override
    public ThreadListingWidget getWidget() {
        return (ThreadListingWidget) super.getWidget();
    }

}
