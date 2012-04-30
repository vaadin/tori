package org.vaadin.tori.component;

import org.vaadin.tori.widgetset.client.ui.lazylayout.AbstractLazyLayoutClientRpc;
import org.vaadin.tori.widgetset.client.ui.lazylayout.AbstractLazyLayoutClientRpc.LazyLayoutClientRpc;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class LazyLayout extends AbstractLazyLayout {
    @Override
    protected AbstractLazyLayoutClientRpc getRpc() {
        return getRpcProxy(LazyLayoutClientRpc.class);
    }

    public void addComponentEagerly(final Component c) {
        addComponent(c);
        loadedComponents.add(c);
    }
}
