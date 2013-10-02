/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.component;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityEvent;
import org.vaadin.hene.popupbutton.PopupButton.PopupVisibilityListener;
import org.vaadin.tori.component.MenuPopup.ContextAction;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.component.MenuPopup.MenuClickListener;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

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

    private final PopupVisibilityListener popupListener = new PopupVisibilityListener() {
        @Override
        public void popupVisibilityChange(final PopupVisibilityEvent event) {
            if (popupLayout.getParent() != contextComponent) {
                contextComponent.setComponent(popupLayout);
            }

            if (!event.isPopupVisible()) {
                popupLayout.beingOpenedHook();
            } else {
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
        layout.addComponent(contextComponent);
    }

    public void add(final String iconName, final String caption,
            final ContextAction action) {
        popupLayout.add(iconName, caption, action);
        setVisible(true);
    }

    public void add(final String iconName, final String caption,
            final ContextComponentSwapper swapper) {
        popupLayout.add(iconName, caption, swapper);
        setVisible(true);
    }

    private PopupButton newContextComponent() {
        final PopupButton popupButton = new PopupButton();
        popupButton.setWidth("0");
        popupButton.setHeight("0");
        popupButton.addPopupVisibilityListener(popupListener);
        // popupButton.setShadowEnabled(false);
        popupLayout.setWidth(POPUP_WIDTH);
        return popupButton;
    }

    /** Closes the context menu */
    public void close() {
        contextComponent.setPopupVisible(false);
    }

    public void swap(final ContextAction oldToSwapOut, final String iconName,
            final String caption, final ContextAction newAction) {
        popupLayout.swap(oldToSwapOut, iconName, caption, newAction);
    }

    public void swap(final ContextComponentSwapper oldToSwapOut,
            final String iconName, final String caption,
            final ContextAction newAction) {
        popupLayout.swap(oldToSwapOut, iconName, caption, newAction);
    }

    public void swap(final ContextAction oldToSwapOut, final String iconName,
            final String caption, final ContextComponentSwapper newSwapper) {
        popupLayout.swap(oldToSwapOut, iconName, caption, newSwapper);
    }

    public void open() {
        contextComponent.setPopupVisible(true);
    }
}
