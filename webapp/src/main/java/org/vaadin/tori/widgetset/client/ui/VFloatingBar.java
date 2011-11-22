package org.vaadin.tori.widgetset.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.VOverlay;
import com.vaadin.terminal.gwt.client.ui.VPopupView;

/**
 * The client-side implementation for server-side {@code FloatingBar} component.
 */
public class VFloatingBar extends Widget implements Container, HasWidgets,
        ResizeHandler {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-floatingbar";

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    protected ApplicationConnection client;

    private FloatingContent content;

    public VFloatingBar() {
        setElement(Document.get().createDivElement());
        Window.addResizeHandler(this);

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

        if (content == null) {
            content = new FloatingContent();
        }
        content.setVisible(true);
        content.show();
        content.updateFromUIDL(uidl, client);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        content.setVisible(false);
        content.hide();
    }

    @Override
    public void replaceChildComponent(final Widget oldComponent,
            final Widget newComponent) {
        content.setWidget(newComponent);
    }

    @Override
    public boolean hasChildComponent(final Widget component) {
        if (content.getWidget() != null) {
            return content.getWidget() == component;
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
        content.updateShadowSizeAndPosition();
        return true;
    }

    @Override
    public RenderSpace getAllocatedSpace(final Widget child) {
        final Element pe = content.getElement();
        final Element ipe = content.getContainerElement();

        // border + padding
        final int width = Util.getRequiredWidth(pe)
                - Util.getRequiredWidth(ipe);
        final int height = Util.getRequiredHeight(pe)
                - Util.getRequiredHeight(ipe);

        return new RenderSpace(RootPanel.get().getOffsetWidth() - width,
                RootPanel.get().getOffsetHeight() - height);
    }

    @Override
    public void onResize(final ResizeEvent event) {
        client.runDescendentsLayout(this);
        content.updateShadowSizeAndPosition();
    }

    @Override
    public Iterator<Widget> iterator() {
        final List<Widget> wrapper = new ArrayList<Widget>(1);
        wrapper.add(content);
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

    /**
     * The actual VOverlay that displays the content for the floating bar.
     */
    private class FloatingContent extends VOverlay {

        private Widget contentWidget;

        private static final String CLASSNAME = VPopupView.CLASSNAME + " "
                + VFloatingBar.CLASSNAME + " " + VPopupView.CLASSNAME
                + "-popup";

        public FloatingContent() {
            setStyleName(CLASSNAME);
            setShadowEnabled(true);
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
         * We need a hack make content act as a child of VFloatingBar in
         * Vaadin's component tree, but work in default GWT manner when closing
         * or opening.
         * 
         * (non-Javadoc)
         * 
         * @see com.google.gwt.user.client.ui.Widget#getParent()
         */
        @Override
        public Widget getParent() {
            if (!isAttached()) {
                return super.getParent();
            } else {
                return VFloatingBar.this;
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
}
