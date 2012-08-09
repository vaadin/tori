package org.vaadin.tori.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriUtil;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
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
    private ArrayList<Component> componentsBeforeSwapping = null;

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
        final Button button = new Button(caption + '\u2026',
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        componentsBeforeSwapping = getCurrentComponents();
                        layout.removeAllComponents();
                        final Component swappedComponent = swapper
                                .swapContextComponent();
                        layout.addComponent(swappedComponent);
                        if (swappedComponent.getWidth() >= 0) {
                            /*
                             * We need to copy the width of the swapped
                             * component, otherwise the popup will clip the
                             * contents. The size is reset in
                             * ContextMenu.popupListener
                             */
                            layout.getParent().setWidth(
                                    swappedComponent.getWidth(),
                                    swappedComponent.getWidthUnits());
                        }
                    }
                });
        button.setIcon(icon);
        button.setStyleName(Reindeer.BUTTON_LINK);
        return button;
    }

    private ArrayList<Component> getCurrentComponents() {
        final ArrayList<Component> components = new ArrayList<Component>();
        final Iterator<Component> i = layout.getComponentIterator();
        while (i.hasNext()) {
            components.add(i.next());
        }
        return components;
    }

    public void swap(final ContextAction oldToSwapOut, final Resource icon,
            final String caption, final ContextAction newAction) {
        final Component oldComponent = actions.remove(oldToSwapOut);
        if (oldComponent != null) {
            final Button newButton = createButton(icon, caption, newAction);
            _replaceComponents(oldComponent, newButton);
            actions.put(newAction, newButton);
        }
    }

    public void swap(final ContextComponentSwapper oldToSwapOut,
            final Resource icon, final String caption,
            final ContextAction newAction) {
        final Component oldComponent = swappers.remove(oldToSwapOut);
        if (oldComponent != null) {
            final Button newButton = createButton(icon, caption, newAction);
            _replaceComponents(oldComponent, newButton);
            actions.put(newAction, newButton);
        }
    }

    public void swap(final ContextAction oldToSwapOut,
            final ThemeResource icon, final String caption,
            final ContextComponentSwapper newSwapper) {
        final Component oldComponent = actions.remove(oldToSwapOut);
        if (oldComponent != null) {
            final Button newButton = createButton(icon, caption, newSwapper);
            _replaceComponents(oldComponent, newButton);
            swappers.put(newSwapper, newButton);
        }
    }

    public void swap(final ContextComponentSwapper oldToSwapOut,
            final ThemeResource icon, final String caption,
            final ContextComponentSwapper newSwapper) {
        final Component oldComponent = swappers.remove(oldToSwapOut);
        if (oldComponent != null) {
            final Button newButton = createButton(icon, caption, newSwapper);
            _replaceComponents(oldComponent, newButton);
            swappers.put(newSwapper, newButton);
        }
    }

    private void _replaceComponents(final Component oldComponent,
            final Component newComponent) {
        if (oldComponent.getParent() == layout) {
            System.out.println("MenuPopup._replaceComponents() 1");
            layout.replaceComponent(oldComponent, newComponent);
        } else {
            System.out.println("MenuPopup._replaceComponents() 2");
            final int i = componentsBeforeSwapping.indexOf(oldComponent);
            if (i >= 0) {
                System.out.println("MenuPopup._replaceComponents() 3");
                componentsBeforeSwapping.remove(i);
                componentsBeforeSwapping.add(i, newComponent);
            }
        }
    }

    public void beingOpenedHook() {
    }

    public void beingClosedHook() {
        if (componentsBeforeSwapping != null) {
            layout.removeAllComponents();
            for (final Component c : componentsBeforeSwapping) {
                layout.addComponent(c);
            }
            componentsBeforeSwapping = null;
        }
    }

}
