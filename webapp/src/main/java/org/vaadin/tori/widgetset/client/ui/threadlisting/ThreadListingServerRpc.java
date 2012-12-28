package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.List;

import com.vaadin.shared.communication.ServerRpc;

public interface ThreadListingServerRpc extends ServerRpc {
    void fetchComponentsForIndices(List<Integer> indicesToFetch);

    void fetchControlsForIndex(int rowIndex);
}
