package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import com.vaadin.terminal.gwt.client.communication.ClientRpc;

public interface LazyLayoutClientRpc extends ClientRpc {
    void renderComponents(List<Integer> indicesToFetch);
}
