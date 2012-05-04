package org.vaadin.tori.widgetset.client.ui.floatingbar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ui.VOverlay;
import com.vaadin.terminal.gwt.client.ui.popupview.VPopupView;

/**
 * The actual VOverlay that displays the content for the floating bar.
 */
class FloatingBarOverlay extends VOverlay {
    private static final String PREFIX_ALIGNMENT_CLASSNAME = "alignment-";
    private FloatingBarWidgetListener listener;
    private boolean currentVisibility = false;
    private boolean topAligned;

    private static final String CLASSNAME = VPopupView.CLASSNAME + " "
            + VPopupView.CLASSNAME + "-popup";

    private static final String CLASSNAME_SHADOW = "floatingbar "
            + VOverlay.CLASSNAME_SHADOW;

    public FloatingBarOverlay() {
        // the shadow is defined in CSS instead of additional elements
        setShadowEnabled(false);
        setStyleName(CLASSNAME);
        setShadowStyle(CLASSNAME_SHADOW);
        setModal(false);
        super.setVisible(false);
    }

    public void setContentWidget(final Widget contentWidget) {
        setWidget(contentWidget);
    }

    public void setListener(final FloatingBarWidgetListener listener) {
        this.listener = listener;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (currentVisibility != visible) {
            listener.visibilityChanged(visible);
            currentVisibility = visible;
        }

        if (topAligned) {
            if (visible) {
                getElement().getStyle().setTop(0, Unit.PX);
            } else {
                getElement().getStyle().setTop(
                        -getWidget().getOffsetHeight() - 10, Unit.PX);
            }
        } else {
            if (visible) {
                getElement().getStyle().setBottom(0, Unit.PX);
            } else {
                getElement().getStyle().setBottom(
                        -getWidget().getOffsetHeight() - 10, Unit.PX);
            }
        }
    }

    /**
     * Set this overlay to be partially visible.
     * 
     * @param percentage
     *            percentage of visibility ({@code >=1} means fully visible,
     *            {@code <= 0} means fully hidden).
     */
    public void setVisible(final double percentage) {
        super.setVisible(true);
        if (percentage >= 1) {
            setVisible(true);
        } else if (percentage <= 0) {
            setVisible(false);
        } else {
            // partially visible
            final double visiblePixels = (getWidget().getOffsetHeight() * (1 - percentage));
            if (topAligned) {
                getElement().getStyle().setTop(-visiblePixels, Unit.PX);
            } else {
                getElement().getStyle().setBottom(-visiblePixels, Unit.PX);
            }
        }
    }

    // overridden for method visibility
    @Override
    public void updateShadowSizeAndPosition() {
        super.updateShadowSizeAndPosition();
    }

    public void setTopAligned(final boolean topAligned) {
        removeStyleName(PREFIX_ALIGNMENT_CLASSNAME
                + (this.topAligned ? "top" : "bottom"));
        this.topAligned = topAligned;
        addStyleName(PREFIX_ALIGNMENT_CLASSNAME
                + (this.topAligned ? "top" : "bottom"));
    }

}