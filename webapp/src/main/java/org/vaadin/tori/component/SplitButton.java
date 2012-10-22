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

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

@SuppressWarnings("serial")
public class SplitButton extends CustomComponent {
    public interface PopupVisibilityEvent {
        boolean isPopupVisible();

        SplitButton getSplitButton();
    }

    public interface PopupVisibilityListener {
        void splitButtonPopupVisibilityChange(final PopupVisibilityEvent event);
    }

    public interface ClickListener {
        void splitButtonClick();
    }

    private final Button button;
    private final PopupButton popupButton;

    public SplitButton(final String caption) {
        button = new Button(caption);
        button.setStyleName("s-button");
        popupButton = new PopupButton(null);
        popupButton.setStyleName("s-popup");

        setStyleName("splitbutton");

        final Layout root = new HorizontalLayout();
        root.addComponent(button);
        root.addComponent(popupButton);
        setCompositionRoot(root);
    }

    public void addClickListener(final ClickListener listener) {
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                listener.splitButtonClick();
            }
        });
    }

    public void addPopupVisibilityListener(
            final PopupVisibilityListener popupVisibilityListener) {
        popupButton
                .addPopupVisibilityListener(new PopupButton.PopupVisibilityListener() {
                    @Override
                    public void popupVisibilityChange(
                            final PopupButton.PopupVisibilityEvent event) {

                        final PopupVisibilityEvent event2 = new PopupVisibilityEvent() {
                            @Override
                            public boolean isPopupVisible() {
                                return popupButton.isPopupVisible();
                            }

                            @Override
                            public SplitButton getSplitButton() {
                                return SplitButton.this;
                            }
                        };

                        popupVisibilityListener
                                .splitButtonPopupVisibilityChange(event2);
                    }
                });
    }

    public void setComponent(final Component component) {
        popupButton.setComponent(component);
    }

    public void setPopupVisible(final boolean visible) {
        popupButton.setPopupVisible(visible);
    }
}
