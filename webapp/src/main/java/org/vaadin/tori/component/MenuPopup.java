package org.vaadin.tori.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriUtil;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.themes.Reindeer;

public class MenuPopup extends CustomComponent {

    public interface ContextAction {
        /** A {@link ContextAction} that does nothing upon activation. */
        ContextAction NULL = new ContextAction() {
            @Override
            public void contextClicked() {
            }
        };

        void contextClicked();
    }

    public interface ContextComponentSwapper {
        Component swapContextComponent();
    }

    public interface MenuClickListener {
        void menuItemClicked();
    }

    private final CssLayout layout = new CssLayout();

    private final Map<ContextAction, Component> actions = new HashMap<ContextAction, Component>();
    private final Map<ContextComponentSwapper, Component> swappers = new HashMap<ContextComponentSwapper, Component>();

    private final List<MenuClickListener> menuClickListeners = new ArrayList<MenuPopup.MenuClickListener>();

    public MenuPopup() {
        setCompositionRoot(layout);
        setStyleName("menupopup");
    }

    public void add(final Resource icon, final String caption,
            final ContextAction action) {
        ToriUtil.checkForNull(action, "action may not be null");
        final Button button = createButton(icon, caption, action);
        actions.put(action, button);
        layout.addComponent(button);
    }

    private Button createButton(final Resource icon, final String caption,
            final ContextAction action) {
        final Button button = new Button(caption, new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                action.contextClicked();
            }
        });
        button.setIcon(icon);
        button.setStyleName(Reindeer.BUTTON_LINK);
        return button;
    }

    public boolean hasItems() {
        return !actions.isEmpty() || !swappers.isEmpty();
    }

    public void addListener(final MenuClickListener listener) {
        menuClickListeners.add(listener);

    }

    public void removeListener(final MenuClickListener listener) {
        menuClickListeners.remove(listener);
    }

    public void add(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        ToriUtil.checkForNull(swapper, "swapper may not be null");
        final Button button = createButton(icon, caption, swapper);
        swappers.put(swapper, button);
        layout.addComponent(button);
    }

    private Button createButton(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        @SuppressWarnings("serial")
        final Button button = new Button(caption + '\u2026',
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        layout.removeAllComponents();
                        layout.addComponent(swapper.swapContextComponent());
                    }
                });
        button.setIcon(icon);
        button.setStyleName(Reindeer.BUTTON_LINK);
        return button;
    }

    public void swap(final ContextAction oldToSwapOut, final Resource icon,
            final String caption, final ContextAction newAction) {
        final Component oldComponent = actions.remove(oldToSwapOut);
        if (oldComponent != null) {
            final Button newButton = createButton(icon, caption, newAction);
            layout.replaceComponent(oldComponent, newButton);
            actions.put(newAction, newButton);
        }
    }
}
