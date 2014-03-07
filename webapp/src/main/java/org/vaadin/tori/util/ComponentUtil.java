/*
 * Copyright 2014 Vaadin Ltd.
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

package org.vaadin.tori.util;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.ConfirmDialog.Factory;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class ComponentUtil {

    public enum HeadingLevel {
        H1, H2, H3, H4
    }

    public static Label getHeadingLabel(final String content,
            final HeadingLevel level) {
        final Label label = new Label(content);
        label.addStyleName(level.name().toLowerCase());
        return label;
    }

    public static HorizontalLayout getHeaderLayout(final String titleString) {
        final HorizontalLayout result = new HorizontalLayout();
        result.setWidth(100.0f, Unit.PERCENTAGE);
        result.setHeight(56.0f, Unit.PIXELS);
        result.setSpacing(true);
        result.setMargin(true);
        result.addStyleName("headerlayout");
        Component title = getHeadingLabel(titleString, HeadingLevel.H2);
        result.addComponent(title);
        result.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
        return result;
    }

    public static MenuBar getDropdownMenu() {
        final MenuBar result = new MenuBar();
        MenuItem rootItem = result.addItem("", null);
        result.setMoreMenuItem(rootItem);
        result.addStyleName("dropdown");
        return result;
    }

    public static Button getSecondaryButton(final String caption,
            final ClickListener clickListener) {
        Button button = new Button(caption, clickListener);
        button.addStyleName("secondarybutton");
        return button;
    }

    public static Factory getConfirmDialogFactory() {
        return new DefaultConfirmDialogFactory() {
            @Override
            public ConfirmDialog create(final String caption,
                    final String message, final String okCaption,
                    final String cancelCaption) {
                ConfirmDialog confirmDialog = super.create(caption, message,
                        okCaption, cancelCaption);
                confirmDialog.getOkButton().addStyleName("secondarybutton");
                HasComponents parent = confirmDialog.getOkButton().getParent();
                return confirmDialog;
            }
        };
    }
}
