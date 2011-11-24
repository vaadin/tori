package org.vaadin.tori.component.thread;

import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;

@SuppressWarnings("serial")
public class ThreadMoveComponent extends CustomComponent {

    private final CssLayout layout = new CssLayout();
    private final DiscussionThread thread;

    public ThreadMoveComponent(final DiscussionThread thread,
            final ContextMenu menu) {
        this.thread = thread;
        setCompositionRoot(layout);
        setWidth("300px");

        final Panel panel = new Panel("Move Thread to Category...");
        panel.setWidth("100%");
        panel.setHeight("250px");
        panel.setScrollable(true);

        layout.addComponent(panel);
        layout.addComponent(new NativeButton("Move Thread",
                new NativeButton.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        getApplication().getMainWindow().showNotification(
                                "move not implemented");
                        menu.close();
                    }
                }));
        layout.addComponent(new NativeButton("Cancel",
                new NativeButton.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        menu.close();
                    }
                }));
    }
}
