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

    @Override
    public void replaceComponent(final Component oldComponent,
            final Component newComponent) {

        if (oldComponent == newComponent) {
            return;
        }

        if (!components.contains(oldComponent)) {
            throw new IllegalArgumentException("old component " + oldComponent
                    + " isn't in the layout");
        }

        final int oldIndex = components.indexOf(oldComponent);
        final boolean oldIsLoaded = loadedComponents.contains(oldComponent);

        if (components.contains(newComponent)) {
            final int newIndex = components.indexOf(newComponent);
            final boolean newIsLoaded = loadedComponents.contains(newComponent);

            components.remove(oldIndex);
            components.add(oldIndex, newComponent);
            components.remove(newIndex);
            components.add(newIndex, oldComponent);

            if (oldIsLoaded || newIsLoaded) {
                loadedComponents.add(oldComponent);
                loadedComponents.add(newComponent);
            }
        } else {
            components.remove(oldComponent);
            components.add(oldIndex, newComponent);

            super.addComponent(newComponent, oldIndex);
            super.removeComponent(oldComponent);

            if (oldIsLoaded) {
                loadedComponents.add(newComponent);
                loadedComponents.remove(oldComponent);
            }
        }

        markAsDirty();
    }
}
