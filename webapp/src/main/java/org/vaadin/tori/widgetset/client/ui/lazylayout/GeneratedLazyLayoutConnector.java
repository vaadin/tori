package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import org.vaadin.tori.component.GeneratedLazyLayout;
import org.vaadin.tori.widgetset.client.ui.lazylayout.AbstractLazyLayoutClientRpc.GeneratedLazyLayoutClientRpc;

import com.vaadin.terminal.gwt.client.ui.Connect;

@Connect(GeneratedLazyLayout.class)
@SuppressWarnings("serial")
public class GeneratedLazyLayoutConnector extends AbstractLazyLayoutConnector {
    @Override
    protected void registerRpcs() {
        registerRpc(GeneratedLazyLayoutClientRpc.class,
                new GeneratedLazyLayoutClientRpc() {
                    @Override
                    public void renderComponents(
                            final List<Integer> indicesToFetch) {
                        getWidget().updateScrollAdjustmentReference();
                        swapLazyComponents(indicesToFetch);
                    }
                });

    }
}
