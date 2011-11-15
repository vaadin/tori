package org.vaadin.tori.component.category;

import org.vaadin.tori.util.StyleConstants;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
class CreateCategoryForm extends CustomComponent {

    public CreateCategoryForm(final CreateCategoryListener listener) {
        final VerticalLayout newCategoryLayout = new VerticalLayout();
        newCategoryLayout.addStyleName(StyleConstants.HALF_MARGIN);
        newCategoryLayout.setSpacing(true);
        newCategoryLayout.setMargin(true);
        newCategoryLayout.setWidth("300px");

        final TextField nameField = new TextField();
        nameField.setInputPrompt("Category name");
        nameField.setWidth("100%");
        newCategoryLayout.addComponent(nameField);

        final TextArea descriptionField = new TextArea();
        descriptionField.setInputPrompt("Description");
        descriptionField.setRows(3);
        descriptionField.setWidth("100%");
        newCategoryLayout.addComponent(descriptionField);

        final Button saveButton = new Button("Create Category",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        if (listener != null) {
                            listener.createCategory(
                                    (String) nameField.getValue(),
                                    (String) descriptionField.getValue());
                        }
                    }
                });
        newCategoryLayout.addComponent(saveButton);
        newCategoryLayout.setComponentAlignment(saveButton,
                Alignment.BOTTOM_RIGHT);

        setCompositionRoot(newCategoryLayout);
    }

    interface CreateCategoryListener {

        void createCategory(String name, String description);

    }

}
