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

package org.vaadin.tori.component.category;

import org.vaadin.tori.data.entity.Category;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
class EditCategoryForm extends CustomComponent {

    private final TextField nameField;

    public EditCategoryForm(final EditCategoryListener listener) {
        this(listener, null);
    }

    public EditCategoryForm(final EditCategoryListener listener,
            final Category categoryToEdit) {
        setData(categoryToEdit);

        final VerticalLayout newCategoryLayout = new VerticalLayout();
        newCategoryLayout.setSpacing(true);
        newCategoryLayout.setMargin(true);
        newCategoryLayout.setWidth("100%");

        nameField = new TextField();
        nameField.setInputPrompt("Category name");
        nameField.setWidth("100%");
        if (categoryToEdit != null) {
            nameField.setValue(categoryToEdit.getName());
        }
        newCategoryLayout.addComponent(nameField);

        final TextArea descriptionField = new TextArea();
        descriptionField.setInputPrompt("Description");
        descriptionField.setRows(3);
        descriptionField.setWidth("100%");
        if (categoryToEdit != null) {
            descriptionField.setValue(categoryToEdit.getDescription());
        }
        newCategoryLayout.addComponent(descriptionField);

        final Button saveButton = new Button(
                (categoryToEdit == null ? "Create Category" : "Save"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        listener.commit(nameField.getValue(),
                                descriptionField.getValue());

                        /*
                         * exceptions would be thrown by now. If everything went
                         * okay, clear the inputs.
                         */
                        nameField.setValue("");
                        descriptionField.setValue("");
                    }
                });
        final Button cancelButton = new Button(("cancel"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        listener.cancel();
                        nameField.setValue("");
                        descriptionField.setValue("");
                    }
                });

        HorizontalLayout buttonsLayout = new HorizontalLayout(cancelButton,
                saveButton);
        buttonsLayout.setWidth(100.0f, Unit.PERCENTAGE);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setExpandRatio(cancelButton, 1.0f);
        buttonsLayout.setComponentAlignment(cancelButton,
                Alignment.BOTTOM_RIGHT);

        newCategoryLayout.addComponent(buttonsLayout);

        setWidth("300px");
        setCompositionRoot(newCategoryLayout);
    }

    interface EditCategoryListener {

        void commit(String name, String description);

        void cancel();

    }

    public Focusable getTitleField() {
        return nameField;
    }

}
