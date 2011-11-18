package org.vaadin.tori.component;

import org.vaadin.tori.ToriApplication;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextArea;

@SuppressWarnings("serial")
public class ReplyComponent extends CustomComponent {
    private final CustomLayout layout = new CustomLayout(
            ToriApplication.CUSTOM_LAYOUT_PATH + "replylayout");

    public ReplyComponent(final String formattingSyntaxXhtml) {
        setCompositionRoot(layout);
        setStyleName("reply");
        layout.setWidth("100%");
        setWidth("100%");

        layout.addComponent(new PopupView("Show Formatting Syntax",
                new Label() {
                    {
                        setWidth("200px");
                        setValue(formattingSyntaxXhtml);
                        setContentMode(CONTENT_XHTML);
                    }
                }), "formattingsyntax");

        final TextArea input = new TextArea();
        input.setWidth("100%");
        layout.addComponent(input, "input");

        layout.addComponent(new Label(), "preview");
        layout.addComponent(new NativeButton("Post"), "postbutton");
        layout.addComponent(new NativeButton("Clear"), "clearbutton");
    }
}
