package org.vaadin.tori.component;

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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Use {@link ContextMenu.Builder} to build a new {@link ContextMenu}
 * 
 * @author Henrik Paul
 */
@SuppressWarnings("serial")
public class ContextMenu extends CustomComponent {

    public interface ContextAction {
        void contextClicked();
    }

    public interface ContextComponentSwapper {
        Component swapContextComponent();
    }

    private final HorizontalLayout layout;
    private final CssLayout popupLayout;
    private final PopupButton contextComponent;
    private final Button settingsIcon;

    private final PopupVisibilityListener popupListener = new PopupVisibilityListener() {
        @Override
        public void popupVisibilityChange(final PopupVisibilityEvent event) {
            if (!event.isPopupVisible()) {
                if (popupLayout.getParent() != contextComponent) {
                    contextComponent.setComponent(popupLayout);
                }

                settingsIcon.removeStyleName("opened");
            } else {
                settingsIcon.addStyleName("opened");
            }
        }
    };

    public ContextMenu() {
        setCompositionRoot(layout = new HorizontalLayout());
        layout.setSizeFull();
        setWidth("16px");
        setHeight("16px");
        setStyleName("contextmenu");

        popupLayout = new CssLayout();

        contextComponent = newContextComponent();
        contextComponent.setStyleName("contextmenu");
        settingsIcon = newSettingsIcon(contextComponent);
        layout.addComponent(settingsIcon);
        layout.addComponent(contextComponent);
    }

    public void add(final Resource icon, final String caption,
            final ContextAction action) {
        ToriUtil.checkForNull(action, "action may not be null");

        final Button button = new Button(caption, new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                action.contextClicked();
                contextComponent.setPopupVisible(false);
            }
        });
        button.setIcon(icon);
        button.setStyleName(Reindeer.BUTTON_LINK);
        popupLayout.addComponent(button);
    }

    public void add(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        ToriUtil.checkForNull(swapper, "swapper may not be null");

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
        popupLayout.addComponent(button);
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
        popupButton.setComponent(popupLayout);
        popupButton.addPopupVisibilityListener(popupListener);
        popupLayout.setWidth("200px");
        return popupButton;
    }

    /** Closes the context menu */
    public void close() {
        contextComponent.setPopupVisible(false);
    }
}
