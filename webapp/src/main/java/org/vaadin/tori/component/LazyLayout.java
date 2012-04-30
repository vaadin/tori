package org.vaadin.tori.component;

import java.util.Collections;

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

        /*
         * TODO: this maybe needs to be optimized so that it's not individual
         * rpc calls, but a queue that gets built and sent over as a state
         * change. PROBLEM: how do we know when one queue is sent, so that a
         * new, empty, queue can be built?
         */
        getRpcProxy(LazyLayoutClientRpc.class).renderComponents(
                Collections.singletonList(components.indexOf(c)));
    }
}
