package org.vaadin.tori.component;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;

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

    public static class Builder {
        private final ContextMenu contextMenu;

        public Builder() {
            contextMenu = new ContextMenu();
        }

        public Builder add(final Resource icon, final String caption,
                final ContextAction action) {
            contextMenu.add(icon, caption, action);
            return this;
        }

        public Builder add(final Resource icon, final String caption,
                final ContextComponentSwapper swapper) {
            contextMenu.add(icon, caption, swapper);
            return this;
        }

        public ContextMenu build() {
            return contextMenu;
        }
    }

    private final HorizontalLayout layout;
    private final CssLayout popupLayout;
    private final PopupButton contextComponent;

    private final PopupVisibilityListener popupResetter = new PopupVisibilityListener() {
        @Override
        public void popupVisibilityChange(final PopupVisibilityEvent event) {
            if (!event.isPopupVisible()) {
                event.getPopupButton().setComponent(popupLayout);
                event.getPopupButton().removePopupVisibilityListener(this);
            }
        }
    };

    private ContextMenu() {
        setCompositionRoot(layout = new HorizontalLayout());
        layout.setSizeFull();
        setWidth("16px");
        setHeight("16px");
        setStyleName("contextmenu");

        popupLayout = new CssLayout();

        contextComponent = newContextComponent();
        contextComponent.setStyleName("contextmenu");
        final Button settingsIcon = getSettingsIcon(contextComponent);
        layout.addComponent(settingsIcon);
        layout.addComponent(contextComponent);
    }

    protected void add(final Resource icon, final String caption,
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
        popupLayout.addComponent(button);
    }

    protected void add(final Resource icon, final String caption,
            final ContextComponentSwapper swapper) {
        final Button button = new Button(caption + '\u2026',
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        contextComponent.removeAllComponents();
                        contextComponent.setComponent(swapper
                                .swapContextComponent());
                        contextComponent
                                .addPopupVisibilityListener(popupResetter);
                    }
                });
        button.setIcon(icon);
        button.setStyleName(Reindeer.BUTTON_LINK);
        popupLayout.addComponent(button);
    }

    private static Button getSettingsIcon(final PopupButton contextComponent) {
        final Button button = new Button();
        button.setStyleName(Reindeer.BUTTON_LINK);
        button.setIcon(new ThemeResource("images/icon-settings.gif"));
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
        popupLayout.setWidth("200px");
        return popupButton;
    }
}
