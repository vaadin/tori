package org.vaadin.tori.component;

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
 * been scrolled out of the browser view port, you can do this by setting a
 * reference to the component in {@link #setScrollComponent(Component)}.
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.tori.widgetset.client.ui.VFloatingBar.class)
public class FloatingBar extends CustomComponent {

    private Component scrollComponent;

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        // add the component to follow
        if (scrollComponent != null) {
            target.addAttribute("scrollComponent", scrollComponent);
        }
    }

    public void setContent(final Component component) {
        setCompositionRoot(component);
    }

    public void setScrollComponent(final Component component) {
        scrollComponent = component;
    }

}
