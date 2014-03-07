package org.vaadin.tori.widgetset.client.ui.threadlisting;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingRow.ThreadListingRowListener;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingWidget.Fetcher;

import com.vaadin.shared.communication.ServerRpc;

public interface ThreadListingServerRpc extends ServerRpc, Fetcher,
        ThreadListingRowListener {
}
