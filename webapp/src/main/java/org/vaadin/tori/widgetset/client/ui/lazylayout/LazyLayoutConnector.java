package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.component.LazyLayout;
import org.vaadin.tori.widgetset.client.ui.lazylayout.AbstractLazyLayoutClientRpc.LazyLayoutClientRpc;

import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.ui.Connect;

@Connect(LazyLayout.class)
@SuppressWarnings("serial")
public class LazyLayoutConnector extends AbstractLazyLayoutConnector {

    @Override
    protected void registerRpcs() {
        registerRpc(LazyLayoutClientRpc.class, new LazyLayoutClientRpc() {
            @Override
            public void renderComponents(final List<Integer> indicesToFetch) {
                getWidget().updateScrollAdjustmentReference();
                swapLazyComponents(indicesToFetch);
            }
        });
    }

    @Override
    protected void onFirstStateChanged(final StateChangeEvent stateChangeEvent) {
        /*
         * we take advantage of the fact that all components have a slot in the
         * connector list, but they're null if the data hasn't been transferred
         * over. Thus - each non-null element in the components list is eagerly
         * loaded.
         */

        int i = 0;
        final List<Integer> indices = new ArrayList<Integer>();
        for (final Connector connector : getState().getComponents()) {
            if (connector != null) {
                indices.add(i);
            }
            i++;
        }

        swapLazyComponents(indices);
    }

}
