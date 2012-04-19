package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import com.vaadin.terminal.gwt.client.communication.ServerRpc;

public interface LazyLayoutServerRpc extends ServerRpc {
    public void fetchComponentsForIndices(List<Integer> indicesToFetch);
}
