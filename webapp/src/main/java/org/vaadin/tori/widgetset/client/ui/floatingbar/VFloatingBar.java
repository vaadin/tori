package org.vaadin.tori.widgetset.client.ui.floatingbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * The client-side implementation for server-side {@code FloatingBar} component.
 */
public class VFloatingBar extends Widget implements Container, HasWidgets,
        ResizeHandler {

    public static final String ATTR_SCROLL_COMPONENT = "scrollComponent";
    public static final String ATTR_ALIGNMENT = "alignment";
    public static final String VAR_VISIBILITY = "visibility";
    public static final String ALIGNMENT_TOP = "top";
    public static final String ALIGNMENT_BOTTOM = "bottom";

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-floatingbar";
    private static final String PREFIX_ALIGNMENT_CLASSNAME = "alignment-";

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    protected ApplicationConnection client;

    private FloatingBarOverlay overlay;
    private Widget scrollComponent;
    private String alignment;

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration scrollHandlerRegistrationWin;
    private HandlerRegistration resizeHandlerRegistration;

    public VFloatingBar() {
        setElement(Document.get().createDivElement());

        // This method call of the Paintable interface sets the component
        // style name in DOM tree
        setStyleName(CLASSNAME);
    }

    /**
     * Called whenever an update is received from the server
     */
    @Override
    public void updateFromUIDL(final UIDL uidl,
            final ApplicationConnection client) {
        // This call should be made first.
        // It handles sizes, captions, tooltips, etc. automatically.
        if (client.updateComponent(this, uidl, true)) {
            // If client.updateComponent returns true there has been no changes
            // and we do not need to update anything.
            return;
        }

        // Save reference to server connection object to be able to send
        // user interaction later
        this.client = client;

        // Save the client side identifier (paintable id) for the widget
        paintableId = uidl.getId();

        attachResizeHandler();

        scrollComponent = getScrollComponent(uidl);
        attachScrollHandlerIfNeeded();

        if (overlay == null) {
            overlay = new FloatingBarOverlay(this);
        }
        updateAlignment(uidl);
        overlay.show();
        overlay.updateFromUIDL(uidl, client);
        overlay.setVisible(!isScrollComponentVisible());
    }

    private void updateAlignment(final UIDL uidl) {
        if (uidl.hasAttribute(ATTR_ALIGNMENT)) {
            final String newAlignment = uidl.getStringAttribute(ATTR_ALIGNMENT);
            if (!newAlignment.equals(alignment)) {
                // update style names
                overlay.removeStyleName(PREFIX_ALIGNMENT_CLASSNAME + alignment);
                overlay.addStyleName(PREFIX_ALIGNMENT_CLASSNAME + newAlignment);

                // assign the new alignment
                alignment = newAlignment;
            }
        }
    }

    private Widget getScrollComponent(final UIDL uidl) {
        if (client == null) {
            throw new IllegalStateException(
                    "The client must be set before calling this method.");
        }

        if (uidl.hasAttribute(ATTR_SCROLL_COMPONENT)) {
            return (Widget) client.getPaintable(uidl
                    .getStringAttribute(ATTR_SCROLL_COMPONENT));
        }
        return null;
    }

    private void attachResizeHandler() {
        if (resizeHandlerRegistration == null) {
            resizeHandlerRegistration = Window.addResizeHandler(this);
        }
    }

    private void attachScrollHandlerIfNeeded() {
        if (scrollComponent != null) {
            if (scrollHandlerRegistration == null) {
                // Cannot use Window.addWindowScrollHandler() in Vaadin apps,
                // but we must listen for scroll events in the VView instance
                // instead...
                final ScrollHandler handler = new ScrollHandler() {
                    @Override
                    public void onScroll(final ScrollEvent event) {
                        overlay.setVisible(!isScrollComponentVisible());
                    }
                };
                scrollHandlerRegistration = client.getView().addDomHandler(
                        handler, ScrollEvent.getType());
            }
            if (scrollHandlerRegistrationWin == null) {
                // ...but within embedded apps (portlet) we do actually scroll
                // the Window, so we need also the ScrollHandler for the Window.
                final Window.ScrollHandler handler = new Window.ScrollHandler() {
                    @Override
                    public void onWindowScroll(
                            final com.google.gwt.user.client.Window.ScrollEvent event) {
                        overlay.setVisible(!isScrollComponentVisible());
                    }
                };
                scrollHandlerRegistrationWin = Window
                        .addWindowScrollHandler(handler);
            }
        }
    }

    @Override
    protected void onDetach() {
        removeAllHandlers();
        overlay.hide();
        super.onDetach();
    }

    private void removeAllHandlers() {
        if (scrollHandlerRegistration != null) {
            scrollHandlerRegistration.removeHandler();
        }
        if (scrollHandlerRegistrationWin != null) {
            scrollHandlerRegistrationWin.removeHandler();
        }
        if (resizeHandlerRegistration != null) {
            resizeHandlerRegistration.removeHandler();
        }
    }

    @Override
    public void replaceChildComponent(final Widget oldComponent,
            final Widget newComponent) {
        overlay.setWidget(newComponent);
    }

    @Override
    public boolean hasChildComponent(final Widget component) {
        if (overlay.getWidget() != null) {
            return overlay.getWidget() == component;
        } else {
            return false;
        }
    }

    @Override
    public void updateCaption(final Paintable component, final UIDL uidl) {
        // NOP
    }

    @Override
    public boolean requestLayout(final Set<Paintable> children) {
        overlay.updateShadowSizeAndPosition();
        return true;
    }

    @Override
    public RenderSpace getAllocatedSpace(final Widget child) {
        final Element pe = overlay.getElement();
        final Element ipe = overlay.getContainerElement();

        // border + padding
        final int width = Util.getRequiredWidth(pe)
                - Util.getRequiredWidth(ipe);
        final int height = Util.getRequiredHeight(pe)
                - Util.getRequiredHeight(ipe);

        return new RenderSpace(Window.getClientWidth() - width,
                Window.getClientHeight() - height);
    }

    private boolean isScrollComponentVisible() {
        if (scrollComponent != null) {
            final Element scrollComponentElem = scrollComponent.getElement();
            final int componentTop = scrollComponentElem.getOffsetTop();
            final int componentHeight = scrollComponentElem.getClientHeight();

            final Element vViewElem = client.getView().getElement();
            final int vViewScrollTop = vViewElem.getScrollTop();
            final int vViewTop = vViewElem.getAbsoluteTop();

            // Decide whether to use:
            // 1) VView scroll position (normal Vaadin app)
            // -- or --
            // 2) Window scroll minus VView top offset (embedded app, portlet).
            final int windowScrollTop = (vViewScrollTop == 0 ? (Window
                    .getScrollTop() - vViewTop) : vViewScrollTop);

            if (alignment.equals(ALIGNMENT_TOP)) {
                return windowScrollTop < (componentTop + componentHeight);
            } else {
                // bottom alignment
                final int windowHeight = Window.getClientHeight();
                return (windowScrollTop + windowHeight) > (componentTop + componentHeight);
            }
        }
        return true;
    }

    @Override
    public void onResize(final ResizeEvent event) {
        // resizing the window might also reveal/hide the scroll component
        overlay.setVisible(!isScrollComponentVisible());

        client.runDescendentsLayout(this);
        overlay.updateShadowSizeAndPosition();
    }

    @Override
    public Iterator<Widget> iterator() {
        final List<Widget> wrapper = new ArrayList<Widget>(1);
        wrapper.add(overlay);
        return wrapper.iterator();
    }

    @Override
    public void add(final Widget w) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Widget w) {
        throw new UnsupportedOperationException();
    }

    public void visibilityChanged(final boolean visible) {
        client.updateVariable(paintableId, VAR_VISIBILITY, visible, true);
    }

}
