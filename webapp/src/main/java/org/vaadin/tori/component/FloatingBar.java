package org.vaadin.tori.component;

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

    private FloatingAlignment alignment = FloatingAlignment.getDefault();
    private Component scrollComponent;

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        // add the component to follow
        if (scrollComponent != null) {
            target.addAttribute(VFloatingBar.ATTR_SCROLL_COMPONENT,
                    scrollComponent);
        }

        // add the alignment
        target.addAttribute(VFloatingBar.ATTR_ALIGNMENT, alignment.toString());
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
}
