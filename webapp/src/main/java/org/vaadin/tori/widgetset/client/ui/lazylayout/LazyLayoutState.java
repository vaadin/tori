package org.vaadin.tori.widgetset.client.ui.lazylayout;

import com.vaadin.shared.annotations.DelegateToWidget;
import com.vaadin.shared.ui.AbstractLayoutState;

@SuppressWarnings("serial")
public class LazyLayoutState extends AbstractLayoutState {
    @DelegateToWidget
    public double renderDistanceMultiplier = 1;
    @DelegateToWidget
    public int renderDelay = 700;

    /* this isn't delegated to widget, since order of execution matters */
    public int amountOfComponents = 0;
    public String placeholderHeight;
    public String placeholderWidth;
}
