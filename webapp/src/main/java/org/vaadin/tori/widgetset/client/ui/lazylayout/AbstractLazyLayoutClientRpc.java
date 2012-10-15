package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.Map;

import com.vaadin.shared.Connector;
import com.vaadin.shared.communication.ClientRpc;

public interface AbstractLazyLayoutClientRpc extends ClientRpc {
    void sendComponents(Map<Integer, Connector> components);
}
