package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import com.vaadin.terminal.gwt.client.communication.ClientRpc;

public interface AbstractLazyLayoutClientRpc extends ClientRpc {
    public interface LazyLayoutClientRpc extends AbstractLazyLayoutClientRpc {
    }

    public interface GeneratedLazyLayoutClientRpc extends
            AbstractLazyLayoutClientRpc {
    }

    void renderComponents(List<Integer> indicesToFetch);
}
