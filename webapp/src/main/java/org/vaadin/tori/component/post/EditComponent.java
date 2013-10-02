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

package org.vaadin.tori.component.post;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextArea;

@SuppressWarnings("serial")
public class EditComponent extends CssLayout {

    public interface EditListener {
        void postEdited(String newPostBody);
    }

    private final String body;
    private Component editLayout;

    private final PopupButton editPopup;

    private final EditListener editListener;

    public EditComponent(final String originalBody,
            final EditListener editListener) {
        this.body = originalBody;
        this.editListener = editListener;

        editPopup = new PopupButton();
        editPopup.setWidth("0");
        editPopup.setHeight("0");
        addComponent(editPopup);
    }

    private Component newEditLayout(final String body) {
        final CssLayout layout = new CssLayout();
        layout.setWidth("400px");

        final TextArea editArea = new TextArea();
        editArea.setValue(body);
        editArea.setWidth("100%");
        editArea.setHeight("100px");
        layout.addComponent(editArea);

        layout.addComponent(new Button("Edit", new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                editListener.postEdited(editArea.getValue());
                editPopup.setPopupVisible(false);
            }
        }));

        layout.addComponent(new Button("Close Editor", new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                editPopup.setPopupVisible(false);
            }
        }));

        return layout;

    }

    public void open() {
        if (editLayout == null) {
            editLayout = newEditLayout(body);
            editPopup.setComponent(editLayout);
        }
        editPopup.setPopupVisible(true);
    }
}
