package org.vaadin.tori.widgetset.client.ui.lazylayout;

import com.vaadin.terminal.gwt.client.ui.AbstractLayoutState;

@SuppressWarnings("serial")
public class LazyLayoutState extends AbstractLayoutState {
    private int totalAmountOfComponents = 0;

    public int getTotalAmountOfComponents() {
        return totalAmountOfComponents;
    }

    public void setTotalAmountOfComponents(final int totalAmountOfComponents) {
        this.totalAmountOfComponents = totalAmountOfComponents;
    }
}
