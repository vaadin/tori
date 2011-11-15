package org.vaadin.tori.component.category;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
class RearrangeControls extends CustomComponent {

    public RearrangeControls(final RearrangeListener listener) {
        final HorizontalLayout rearrangeControls = new HorizontalLayout();

        rearrangeControls.setSpacing(true);
        rearrangeControls.addComponent(new Button("Apply rearrangement",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        if (listener != null) {
                            listener.applyRearrangement();
                        }
                    }
                }));
        rearrangeControls.addComponent(new Button("Cancel",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        if (listener != null) {
                            listener.cancelRearrangement();
                        }
                    }
                }));

        setCompositionRoot(rearrangeControls);
    }

    interface RearrangeListener {

        void applyRearrangement();

        void cancelRearrangement();

    }
}
