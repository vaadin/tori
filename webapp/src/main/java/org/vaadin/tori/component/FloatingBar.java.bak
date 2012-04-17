package org.vaadin.tori.component;

import java.lang.reflect.Method;
import java.util.Map;

import org.vaadin.tori.widgetset.client.ui.floatingbar.VFloatingBar;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

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
@com.vaadin.ui.ClientWidget(org.vaadin.tori.widgetset.client.ui.floatingbar.VFloatingBar.class)
public class FloatingBar extends CustomComponent {

    /**
     * The alignment of the FloatingBar on screen.
     */
    public enum FloatingAlignment {
        TOP(VFloatingBar.ALIGNMENT_TOP), BOTTOM(VFloatingBar.ALIGNMENT_BOTTOM);

        private final String stringRepresentation;

        private FloatingAlignment(final String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        private static FloatingAlignment getDefault() {
            return TOP;
        }

        @Override
        public String toString() {
            return stringRepresentation;
        }
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

    private static final int DEFAULT_SCROLL_THRESHOLD = 200;

    private FloatingAlignment alignment = FloatingAlignment.getDefault();
    private Component scrollComponent;
    private int scrollThreshold = DEFAULT_SCROLL_THRESHOLD;

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        // add the component to follow
        if (scrollComponent != null) {
            target.addAttribute(VFloatingBar.ATTR_SCROLL_COMPONENT,
                    scrollComponent);
            target.addAttribute(VFloatingBar.ATTR_SCROLL_THRESHOLD,
                    scrollThreshold);
        }

        // add the alignment
        target.addAttribute(VFloatingBar.ATTR_ALIGNMENT, alignment.toString());
    }

    @Override
    public void changeVariables(final Object source,
            final Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if (variables.containsKey(VFloatingBar.VAR_VISIBILITY)) {
            // visibility has changed -> fire appropriate event
            if ((Boolean) variables.get(VFloatingBar.VAR_VISIBILITY)) {
                fireEvent(new DisplayEvent());
            } else {
                fireEvent(new HideEvent());
            }
        }
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
        this.scrollThreshold = threshold;
    }

    /**
     * Sets the {@link Component} to be displayed as the content of this
     * {@link FloatingBar}.
     * 
     * @param component
     */
    public void setContent(final Component component) {
        setCompositionRoot(component);
        requestRepaint();
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
        scrollComponent = component;
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
        this.alignment = alignment;
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
}
