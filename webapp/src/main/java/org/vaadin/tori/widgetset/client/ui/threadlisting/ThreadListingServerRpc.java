package org.vaadin.tori.widgetset.client.ui.threadlisting;

import com.vaadin.shared.communication.ServerRpc;

public interface ThreadListingServerRpc extends ServerRpc {
    void fetchRows();
}
