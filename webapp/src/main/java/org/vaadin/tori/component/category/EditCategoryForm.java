package org.vaadin.tori.component.category;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.util.StyleConstants;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
class EditCategoryForm extends CustomComponent {

    public EditCategoryForm(final EditCategoryListener listener) {
        this(listener, null);
    }

    public EditCategoryForm(final EditCategoryListener listener,
            final Category categoryToEdit) {
        setData(categoryToEdit);

        final VerticalLayout newCategoryLayout = new VerticalLayout();
        newCategoryLayout.addStyleName(StyleConstants.HALF_MARGIN);
        newCategoryLayout.setSpacing(true);
        newCategoryLayout.setMargin(true);
        newCategoryLayout.setWidth("100%");

        final TextField nameField = new TextField();
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
                    }
                });
        newCategoryLayout.addComponent(saveButton);
        newCategoryLayout.setComponentAlignment(saveButton,
                Alignment.BOTTOM_RIGHT);

        setWidth("300px");
        setCompositionRoot(newCategoryLayout);
    }

    interface EditCategoryListener {

        void commit(String name, String description);

    }

}
