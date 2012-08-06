package org.vaadin.tori.widgetset.client.ui.floatingbar;

import com.vaadin.shared.ComponentState;
import com.vaadin.shared.Connector;

@SuppressWarnings("serial")
public class FloatingBarState extends ComponentState {
    private static final int DEFAULT_SCROLL_THRESHOLD = 200;
    private int scrollThreshold = DEFAULT_SCROLL_THRESHOLD;
    private Connector scrollComponent;
    private Connector content;
    private boolean portlet;
    private boolean topAligned;

    public final int getScrollThreshold() {
        return scrollThreshold;
    }

    public final void setScrollThreshold(final int scrollThreshold) {
        this.scrollThreshold = scrollThreshold;
    }

    public final Connector getScrollComponent() {
        return scrollComponent;
    }

    public final void setScrollComponent(final Connector scrollComponent) {
        this.scrollComponent = scrollComponent;
    }

    public final Connector getContent() {
        return content;
    }

    public final void setContent(final Connector content) {
        this.content = content;
    }

    public final boolean isPortlet() {
        return portlet;
    }

    public final void setPortlet(final boolean portlet) {
        this.portlet = portlet;
    }

    public final boolean isTopAligned() {
        return topAligned;
    }

    public final void setTopAligned(final boolean topAligned) {
        this.topAligned = topAligned;
    }

}
