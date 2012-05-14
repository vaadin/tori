package org.vaadin.tori.component.post;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class EditComponent extends CssLayout {

    public interface EditListener {
        void postEdited(String newPostBody);
    }

    private final ClickListener listener = new ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            if (editLayout == null) {
                editLayout = newEditLayout(body);
                editPopup.setComponent(editLayout);
            }
            editPopup.setPopupVisible(true);
        }
    };

    private final String body;
    private Component editLayout;

    private final PopupButton editPopup;

    private final EditListener editListener;

    public EditComponent(final String originalBody,
            final EditListener editListener) {
        this.body = originalBody;
        this.editListener = editListener;

        final Button editButton = new Button("Edit Post", listener);
        editButton.setStyleName(BaseTheme.BUTTON_LINK);
        editButton.setIcon(new ThemeResource("images/icon-link-edit.png"));
        addComponent(editButton);

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
}
