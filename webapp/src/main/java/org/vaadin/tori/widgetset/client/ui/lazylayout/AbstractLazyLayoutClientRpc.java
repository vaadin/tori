package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import com.vaadin.shared.communication.ClientRpc;

public interface AbstractLazyLayoutClientRpc extends ClientRpc {
    void renderComponents(List<Integer> indicesToFetch);
}
