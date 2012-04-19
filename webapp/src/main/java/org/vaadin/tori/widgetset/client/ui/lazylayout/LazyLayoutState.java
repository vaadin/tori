package org.vaadin.tori.widgetset.client.ui.lazylayout;

import com.vaadin.terminal.gwt.client.ui.AbstractLayoutState;

@SuppressWarnings("serial")
public class LazyLayoutState extends AbstractLayoutState {
    private int totalAmountOfComponents = 0;
    private String placeholderHeight;
    private String placeholderWidth;

    public String getPlaceholderHeight() {
        return placeholderHeight;
    }

    public String getPlaceholderWidth() {
        return placeholderWidth;
    }

    public int getTotalAmountOfComponents() {
        return totalAmountOfComponents;
    }

    public void setTotalAmountOfComponents(final int totalAmountOfComponents) {
        this.totalAmountOfComponents = totalAmountOfComponents;
    }

    public void setPlaceholderHeight(final String placeholderHeight) {
        this.placeholderHeight = placeholderHeight;
    }

    public void setPlaceholderWidth(final String placeholderWidth) {
        this.placeholderWidth = placeholderWidth;
    }
}
