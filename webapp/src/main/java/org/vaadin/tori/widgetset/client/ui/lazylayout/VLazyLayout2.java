package org.vaadin.tori.widgetset.client.ui.lazylayout;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.StyleConstants;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VMarginInfo;
import com.vaadin.terminal.gwt.client.ui.csslayout.VCssLayout;

public class VLazyLayout2 extends SimplePanel {
    public static final String TAGNAME = "lazylayout";
    public static final String CLASSNAME = "v-" + TAGNAME;

    FlowPane panel = new FlowPane();

    Element margin = DOM.createDiv();
    private int totalAmountOfComponents;

    public VLazyLayout2() {
        super();
        getElement().appendChild(margin);
        setStyleName(CLASSNAME);
        margin.setClassName(CLASSNAME + "-margin");
        setWidget(panel);
    }

    @Override
    protected Element getContainerElement() {
        return margin;
    }

    public static class FlowPane extends FlowPanel {

        public FlowPane() {
            super();
            setStyleName(CLASSNAME + "-container");
        }

        void addOrMove(final Widget child, final int index) {
            if (child.getParent() == this) {
                final int currentIndex = getWidgetIndex(child);
                if (index == currentIndex) {
                    return;
                }
            }
            insert(child, index);
        }

    }

    /**
     * Sets CSS classes for margin based on the given parameters.
     * 
     * @param margins
     *            A {@link VMarginInfo} object that provides info on
     *            top/left/bottom/right margins
     */
    protected void setMarginStyles(final VMarginInfo margins) {
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_TOP, margins.hasTop());
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_RIGHT, margins.hasRight());
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_BOTTOM, margins.hasBottom());
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_LEFT, margins.hasLeft());
    }

    public void setComponentsAmount(final int totalComponentAmount) {
        if (totalComponentAmount != totalAmountOfComponents) {
            totalAmountOfComponents = totalComponentAmount;
            VConsole.log("***************** COMPONENT AMOUNT CHANGED: "
                    + totalComponentAmount);
        }
    }
}
