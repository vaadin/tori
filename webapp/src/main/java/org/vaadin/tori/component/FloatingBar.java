package org.vaadin.tori.component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.vaadin.tori.widgetset.client.ui.floatingbar.FloatingBarRpc;
import org.vaadin.tori.widgetset.client.ui.floatingbar.FloatingBarState;

import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;

/**
 * Displays a floating bar that is displayed as floating on top of all other
 * components. <br />
 * <br />
 * Before this component can be displayed, you must set the content by calling
 * {@link #setContent(Component)} method. <br />
 * <br />
 * If you wish to display this component only after some other component has
 * been scrolled out of the browser viewport, you can do this by setting a
 * reference to the component in {@link #setScrollComponent(Component)}.
 */
@SuppressWarnings("serial")
public class FloatingBar extends AbstractComponentContainer implements
        FloatingBarRpc {

    /**
     * The alignment of the FloatingBar on screen.
     */
    public enum FloatingAlignment {
        TOP, BOTTOM;
    }

    private static final Method ON_HIDE_METHOD;
    private static final Method ON_DISPLAY_METHOD;
    static {
        try {
            ON_HIDE_METHOD = VisibilityListener.class.getDeclaredMethod(
                    "onHide", new Class[] { HideEvent.class });
            ON_DISPLAY_METHOD = VisibilityListener.class.getDeclaredMethod(
                    "onDisplay", new Class[] { DisplayEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding methods in "
                            + FloatingBar.class.getSimpleName());
        }
    }

    public FloatingBar() {
        registerRpc(this);
    }

    /**
     * Sets the amount of pixels that must be scrolled past the scroll component
     * so that content of this FloatingBar is fully visible. If no scroll
     * component is set, this setting has no effect. The default setting for
     * this is {@value #DEFAULT_SCROLL_THRESHOLD} pixels.
     * 
     * @param threshold
     *            threshold in pixels, must be >= 0.
     * @see #setScrollComponent(Component)
     */
    public void setScrollThreshold(final int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException(
                    "The threshold must be a positive integer.");
        }
        getState().setScrollThreshold(threshold);
    }

    @Override
    public FloatingBarState getState() {
        return (FloatingBarState) super.getState();
    }

    /**
     * Sets the {@link Component} to be displayed as the content of this
     * {@link FloatingBar}.
     * 
     * @param component
     */
    public void setContent(final Component component) {
        getState().setContent(component);
        addComponent(component);
        requestRepaint();
    }

    @Override
    public void updateState() {
        getState()
                .setPortlet(
                        getApplication().getContext() instanceof PortletApplicationContext2);
        super.updateState();
    }

    /**
     * Sets the component which defines the visibility of this
     * {@link FloatingBar} depending also on the current
     * {@link FloatingAlignment}. <br />
     * <br />
     * For {@link FloatingAlignment#TOP} this bar only will appear if the
     * viewport is scrolled down and the given {@code component} is pushed out
     * from the top of the view.<br />
     * <br />
     * {@link FloatingAlignment#BOTTOM} works the other way around and displays
     * this bar only when the browser viewport is scrolled up and the given
     * {@code component} is left out from the bottom of the view.
     * 
     * @param component
     * @see #setAlignment(FloatingAlignment)
     */
    public void setScrollComponent(final Component component) {
        getState().setScrollComponent(component);
        requestRepaint();
    }

    /**
     * Sets the alignment on where this {@link FloatingBar} should be displayed
     * on the screen. This setting also has an effect on how the visibility is
     * defined if the scrolling component is set (see
     * {@link #setScrollComponent(Component)} method for more details).
     * 
     * @param alignment
     * @see #setScrollComponent(Component)
     */
    public void setAlignment(final FloatingAlignment alignment) {
        getState().setTopAligned(alignment == FloatingAlignment.TOP);
        requestRepaint();
    }

    public void addListener(final VisibilityListener listener) {
        addListener(HideEvent.class, listener, ON_HIDE_METHOD);
        addListener(DisplayEvent.class, listener, ON_DISPLAY_METHOD);
    }

    public interface VisibilityListener {

        void onHide(HideEvent event);

        void onDisplay(DisplayEvent event);

    }

    public class HideEvent extends Component.Event {

        public HideEvent() {
            super(FloatingBar.this);
        }

        @Override
        public FloatingBar getSource() {
            return (FloatingBar) super.getSource();
        }

    }

    public class DisplayEvent extends Component.Event {

        public DisplayEvent() {
            super(FloatingBar.this);
        }

        @Override
        public FloatingBar getSource() {
            return (FloatingBar) super.getSource();
        }

    }

    @Override
    public void visibilityChanged(final boolean visible) {
        if (visible) {
            fireEvent(new DisplayEvent());
        } else {
            fireEvent(new HideEvent());
        }
    }

    @Override
    public void replaceComponent(final Component oldComponent,
            final Component newComponent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getComponentCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        return Arrays.asList((Component) getState().getContent()).iterator();
    }
}
