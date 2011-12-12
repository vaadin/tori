package org.vaadin.tori.component;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class PanicComponent extends CustomComponent {
    private final VerticalLayout layout = new VerticalLayout();

    public PanicComponent() {
        setCompositionRoot(layout);
        setSizeFull();
        layout.setSizeFull();

        final Embedded panicImage = new Embedded(null, new ThemeResource(
                "images/panic.png"));
        layout.addComponent(panicImage);
        layout.setComponentAlignment(panicImage, Alignment.MIDDLE_CENTER);
    }
}
