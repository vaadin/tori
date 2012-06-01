package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.terminal.gwt.client.Connector;
import com.vaadin.terminal.gwt.client.ui.AbstractLayoutState;

@SuppressWarnings("serial")
public class LazyLayoutState extends AbstractLayoutState {
    private int totalAmountOfComponents = 0;
    private String placeholderHeight;
    private String placeholderWidth;
    private double renderDistanceMultiplier = 1;
    private int renderDelay = 700;
    private List<Connector> connectors = new ArrayList<Connector>();

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

    public void setRenderDistanceMultiplier(
            final double renderDistanceMultiplier) {
        this.renderDistanceMultiplier = renderDistanceMultiplier;
    }

    public double getRenderDistanceMultiplier() {
        return renderDistanceMultiplier;
    }

    public void setRenderDelay(final int renderDelayMillis) {
        this.renderDelay = renderDelayMillis;
    }

    public void setConnectors(final List<Connector> connectors) {
        this.connectors = connectors;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }
}
