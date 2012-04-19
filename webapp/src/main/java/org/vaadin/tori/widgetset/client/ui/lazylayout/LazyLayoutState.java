package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.List;

import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.ui.AbstractLayoutState;

@SuppressWarnings("serial")
public class LazyLayoutState extends AbstractLayoutState {
    private int totalAmountOfComponents = 0;
    private String placeholderHeight;
    private String placeholderWidth;
    private int renderDistance;
    private int renderDelay;
    private List<? extends Connector> components;

    public int getRenderDelay() {
        return renderDelay;
    }

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

    public void setRenderDistance(final int renderDistancePx) {
        this.renderDistance = renderDistancePx;
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public void setRenderDelay(final int renderDelayMillis) {
        this.renderDelay = renderDelayMillis;
    }

    public void setComponents(final List<? extends Connector> components) {
        this.components = components;
    }

    public List<? extends Connector> getComponents() {
        return components;
    }
}
