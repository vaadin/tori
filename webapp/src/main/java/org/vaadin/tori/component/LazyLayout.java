package org.vaadin.tori.component;

import org.vaadin.tori.widgetset.client.ui.lazylayout.AbstractLazyLayoutClientRpc;
import org.vaadin.tori.widgetset.client.ui.lazylayout.AbstractLazyLayoutClientRpc.LazyLayoutClientRpc;

@SuppressWarnings("serial")
public class LazyLayout extends AbstractLazyLayout {
    @Override
    protected AbstractLazyLayoutClientRpc getRpc() {
        return getRpcProxy(LazyLayoutClientRpc.class);
    }
}
