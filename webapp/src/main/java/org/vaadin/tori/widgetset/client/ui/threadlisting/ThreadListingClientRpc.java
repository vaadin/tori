package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.List;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadAdditionalData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadPrimaryData;

import com.vaadin.shared.communication.ClientRpc;

public interface ThreadListingClientRpc extends ClientRpc {
    void sendRows(List<ThreadPrimaryData> rows, int placeholders);

    void refreshThreadRows(List<ThreadAdditionalData> rows);

    void removeThreadRow(String threadId);
}
