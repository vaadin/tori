package org.vaadin.tori.widgetset.client.ui.floatingbar;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VOverlay;
import com.vaadin.terminal.gwt.client.ui.VPopupView;

/**
 * The actual VOverlay that displays the content for the floating bar.
 */
class FloatingBarOverlay extends VOverlay {

    private Widget contentWidget;
    private final VFloatingBar owner;
    private boolean hiding = false;
    private boolean currentVisibility = false;

    private static final String CLASSNAME = VPopupView.CLASSNAME + " "
            + VPopupView.CLASSNAME + "-popup";

    private static final String CLASSNAME_SHADOW = "floatingbar "
            + VOverlay.CLASSNAME_SHADOW;

    public FloatingBarOverlay(final VFloatingBar owner) {
        this.owner = owner;

        // the shadow is defined in CSS instead of additional elements
        setShadowEnabled(false);
        setStyleName(CLASSNAME);
        setShadowStyle(CLASSNAME_SHADOW);
        setModal(false);
    }

    public void updateFromUIDL(final UIDL uidl,
            final ApplicationConnection client) {
        final UIDL child = uidl.getChildUIDL(0);
        final Paintable contentPaintable = client.getPaintable(child);
        contentWidget = (Widget) contentPaintable;

        if (!contentWidget.equals(getWidget())) {
            if (getWidget() != null) {
                client.unregisterPaintable((Paintable) getWidget());
            }
            setWidget(contentWidget);
        }
        contentPaintable.updateFromUIDL(child, client);
        updateShadowSizeAndPosition();
    }

    /*
     * Copied from: VPopupView.CustomPopup.getParent()
     * 
     * We need a hack make content act as a child of a VFloatingBar in Vaadin's
     * component tree, but work in default GWT manner when closing or opening.
     * 
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.Widget#getParent()
     */
    @Override
    public Widget getParent() {
        if (!isAttached() || hiding) {
            return super.getParent();
        } else {
            return owner;
        }
    }

    @Override
    public void hide(final boolean autoClosed) {
        hiding = true;
        super.hide(autoClosed);
    }

    @Override
    public void show() {
        hiding = false;
        super.show();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        hiding = false;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (currentVisibility != visible) {
            owner.visibilityChanged(visible);
            currentVisibility = visible;
        }

        if (owner.isTopAlignment()) {
            if (visible) {
                getElement().getStyle().setTop(0, Unit.PX);
            } else {
                getElement().getStyle().setTop(
                        -contentWidget.getOffsetHeight() - 10, Unit.PX);
            }
        } else {
            if (visible) {
                getElement().getStyle().setBottom(0, Unit.PX);
            } else {
                getElement().getStyle().setBottom(
                        -contentWidget.getOffsetHeight() - 10, Unit.PX);
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
        if (percentage >= 1) {
            setVisible(true);
        } else if (percentage <= 0) {
            setVisible(false);
        } else {
            // partially visible
            final double visiblePixels = (contentWidget.getOffsetHeight() * (1 - percentage));
            if (owner.isTopAlignment()) {
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

    // overridden for method visibility
    @Override
    public Element getContainerElement() {
        return super.getContainerElement();
    }

}