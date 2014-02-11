package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.List;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.vaadin.shared.communication.ClientRpc;

public interface ThreadListingClientRpc extends ClientRpc {
    void sendRows(List<RowInfo> rows, int placeholders);

    void refreshThreadRow(RowInfo rowInfo);

    void removeThreadRow(long threadId);
}
