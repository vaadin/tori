package org.vaadin.tori.component;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.ToriUtil;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.themes.Reindeer;

/**
 * ContextMenu displays a context menu that can contain {@link ContextAction}
 * items and {@link ContextComponentSwapper} items. Notice that this component
 * is not visible until there are some actual items to display.
 * 
 * @author Henrik Paul
 */
@SuppressWarnings("serial")
public class ContextMenu extends CustomComponent {

    private static final String OPENED_CLASS_NAME = "opened";
    private static final String ICON_SIZE = "16px";

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

    private final CssLayout layout = new CssLayout();
    private final CssLayout popupLayout;
    private final PopupButton contextComponent;
    private final Button settingsIcon;

    private final Map<ContextAction, Component> actions = new HashMap<ContextAction, Component>();
    private final Map<ContextComponentSwapper, Component> swappers = new HashMap<ContextComponentSwapper, Component>();

    private final PopupVisibilityListener popupListener = new PopupVisibilityListener() {
        @Override
        public void popupVisibilityChange(final PopupVisibilityEvent event) {
            if (popupLayout.getParent() != contextComponent) {
                contextComponent.setComponent(popupLayout);
            }

            if (!event.isPopupVisible()) {
                settingsIcon.removeStyleName(OPENED_CLASS_NAME);
            } else {
                settingsIcon.addStyleName(OPENED_CLASS_NAME);
            }
        }
    };

    public ContextMenu() {
        setCompositionRoot(layout);
        layout.setWidth(ICON_SIZE);
        layout.setHeight(ICON_SIZE);
        setWidth(ICON_SIZE);
        setHeight(ICON_SIZE);
        setStyleName("contextmenu");

        /*
         * invisible by default, until it has some items. Re-set as visible in
         * the add()-methods
         */
        setVisible(false);

        popupLayout = new CssLayout();

        contextComponent = newContextComponent();
        contextComponent.setStyleName("contextmenu");
        settingsIcon = newSettingsIcon(contextComponent);
        layout.addComponent(settingsIcon);
        layout.addComponent(contextComponent);
    }

    public boolean hasItems() {
        return !actions.isEmpty() || !swappers.isEmpty();
    }

    public void add(final Resource icon, final String caption,
            final ContextAction action) {
        ToriUtil.checkForNull(action, "action may not be null");
        final Button button = createButton(icon, caption, action);
        actions.put(action, button);
        popupLayout.addComponent(button);
        setVisible(true);
    }

    private Button createButton(final Resource icon, final String caption,
            final ContextAction action) {
        final Button button = new Button(caption, new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                action.contextClicked();
                contextComponent.setPopupVisible(false);
            }
        });
        button.setIcon(icon);
        button.setStyleName(Reindeer.BUTTON_LINK);
        return button;
    }

    public void add(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        ToriUtil.checkForNull(swapper, "swapper may not be null");
        final Button button = createButton(icon, caption, swapper);
        swappers.put(swapper, button);
        popupLayout.addComponent(button);
        setVisible(true);
    }

    private Button createButton(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        final Button button = new Button(caption + '\u2026',
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        contextComponent.removeAllComponents();
                        contextComponent.setComponent(swapper
                                .swapContextComponent());
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
            popupLayout.replaceComponent(oldComponent, newButton);
            actions.put(newAction, newButton);
        }
    }

    private static Button newSettingsIcon(final PopupButton contextComponent) {
        final Button button = new Button();
        button.setStyleName(Reindeer.BUTTON_LINK);
        button.setIcon(new ThemeResource("images/icon-settings.png"));
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                contextComponent.setPopupVisible(true);
            }
        });
        return button;
    }

    private PopupButton newContextComponent() {
        final PopupButton popupButton = new PopupButton();
        popupButton.setWidth("0");
        popupButton.setHeight("0");
        popupButton.addPopupVisibilityListener(popupListener);
        popupLayout.setWidth("200px");
        return popupButton;
    }

    /** Closes the context menu */
    public void close() {
        contextComponent.setPopupVisible(false);
    }
}
