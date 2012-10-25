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

import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.exception.DataSourceException;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * Simple confirmation dialog with two options, confirm or cancel.
 */
@SuppressWarnings("serial")
public class ConfirmationDialog extends CustomComponent {

    public ConfirmationDialog(final String title, final String confirmCaption,
            final String cancelCaption, final ConfirmationListener listener) {
        setWidth("310px");

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setWidth("100%");
        setCompositionRoot(layout);

        layout.addComponent(new HeadingLabel(title, HeadingLevel.H2));

        final HorizontalLayout buttonBar = new HorizontalLayout();
        layout.addComponent(buttonBar);
        layout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);

        final NativeButton ban = new NativeButton(confirmCaption,
                new ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        try {
                            listener.onConfirmed();
                        } catch (final DataSourceException e) {
                            layout.removeAllComponents();
                            layout.addComponent(new Label(
                                    DataSourceException.BORING_GENERIC_ERROR_MESSAGE));
                        }
                    }
                });
        buttonBar.addComponent(ban);

        final NativeButton cancel = new NativeButton(cancelCaption,
                new ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        listener.onCancel();
                    }
                });
        buttonBar.addComponent(cancel);
    }

    public interface ConfirmationListener {
        void onConfirmed() throws DataSourceException;

        void onCancel();
    }

}
