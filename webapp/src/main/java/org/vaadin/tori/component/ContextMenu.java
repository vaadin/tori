package org.vaadin.tori.component;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.component.MenuPopup.ContextAction;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.component.MenuPopup.MenuClickListener;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
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

    private static final String POPUP_WIDTH = "200px";
    private static final String OPENED_CLASS_NAME = "opened";
    private static final String ICON_SIZE = "16px";

    private final CssLayout layout = new CssLayout();
    private final MenuPopup popupLayout;
    private final PopupButton contextComponent;
    private final Button settingsIcon;

    private final PopupVisibilityListener popupListener = new PopupVisibilityListener() {
        @Override
        public void popupVisibilityChange(final PopupVisibilityEvent event) {
            if (popupLayout.getParent() != contextComponent) {
                contextComponent.setComponent(popupLayout);
            }

            if (!event.isPopupVisible()) {
                settingsIcon.removeStyleName(OPENED_CLASS_NAME);
                popupLayout.beingOpenedHook();
            } else {
                settingsIcon.addStyleName(OPENED_CLASS_NAME);
                popupLayout.beingClosedHook();

                /*
                 * a switcher may have modified the size of the popup, so we
                 * reset it here
                 */
                popupLayout.setWidth(POPUP_WIDTH);
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

        popupLayout = new MenuPopup();
        popupLayout.addListener(new MenuClickListener() {
            @Override
            public void menuItemClicked() {
                contextComponent.setPopupVisible(false);
            }
        });

        contextComponent = newContextComponent();
        contextComponent.setStyleName("contextmenu");
        settingsIcon = newSettingsIcon(contextComponent);
        layout.addComponent(settingsIcon);
        layout.addComponent(contextComponent);
    }

    public void add(final Resource icon, final String caption,
            final ContextAction action) {
        popupLayout.add(icon, caption, action);
        setVisible(true);
    }

    public void add(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        popupLayout.add(icon, caption, swapper);
        setVisible(true);
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
        popupLayout.setWidth(POPUP_WIDTH);
        return popupButton;
    }

    /** Closes the context menu */
    public void close() {
        contextComponent.setPopupVisible(false);
    }

    public void swap(final ContextAction oldToSwapOut, final Resource icon,
            final String caption, final ContextAction newAction) {
        popupLayout.swap(oldToSwapOut, icon, caption, newAction);
    }

    public void swap(final ContextComponentSwapper oldToSwapOut,
            final Resource icon, final String caption,
            final ContextAction newAction) {
        popupLayout.swap(oldToSwapOut, icon, caption, newAction);
    }

    public void swap(final ContextAction oldToSwapOut,
            final ThemeResource icon, final String caption,
            final ContextComponentSwapper newSwapper) {
        popupLayout.swap(oldToSwapOut, icon, caption, newSwapper);
    }
}
