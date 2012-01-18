package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;

@SuppressWarnings("serial")
public class ThreadModifiersComponent extends CustomComponent {

    private static final String ICON_WIDTH = "8px";
    private static final String ICON_HEIGHT = "8px";

    private final CssLayout layout = new CssLayout();

    public ThreadModifiersComponent(final DiscussionThread thread) {
        ToriUtil.checkForNull(thread, "thread may not be null");

        setCompositionRoot(layout);
        setStyleName("modifiers");
        setSizeUndefined();
        layout.setSizeUndefined();

        if (thread.isSticky()) {
            addIcon("icon-small-sticky.png", "Thread is sticky");
        }
        if (thread.isLocked()) {
            addIcon("icon-small-locked.png", "Thread is locked");
        }
    }

    private void addIcon(final String iconName, final String popupText) {
        final Embedded icon = new Embedded(null, new ThemeResource("images/"
                + iconName));
        icon.setDescription(popupText);
        icon.setWidth(ICON_WIDTH);
        icon.setHeight(ICON_HEIGHT);
        layout.addComponent(icon);
    }
}
