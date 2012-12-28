package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.List;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo.Action;

import com.vaadin.shared.communication.ServerRpc;

public interface ThreadListingServerRpc extends ServerRpc {
    void fetchComponentsForIndices(List<Integer> indicesToFetch);

    void fetchControlsForIndex(int rowIndex);

    void handle(Action action, long threadId);
}
