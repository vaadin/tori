package org.vaadin.tori.component;

import org.vaadin.hene.expandingtextarea.ExpandingTextArea;
import org.vaadin.tori.ToriApplication;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;

@SuppressWarnings("serial")
public class ReplyComponent extends CustomComponent {
    private final CustomLayout layout = new CustomLayout(
            ToriApplication.CUSTOM_LAYOUT_PATH + "replylayout");

    /**
     * @param formattingSyntaxXhtml
     *            The forum post formatting reference that will be shown as-is
     *            to the user, when she clicks on the designated help button.
     *            The string must be formatted in valid XHTML.
     */
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

        final ExpandingTextArea input = new ExpandingTextArea();
        input.setWidth("100%");
        layout.addComponent(input, "input");

        final Label preview = new Label("<br/>", Label.CONTENT_XHTML);
        layout.addComponent(preview, "preview");
        layout.addComponent(new NativeButton("Post"), "postbutton");
        layout.addComponent(new NativeButton("Clear"), "clearbutton");

        input.addListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(final TextChangeEvent event) {
                final String previewText = event.getText();
                final String formattedPreview = ToriApplication.getCurrent()
                        .getPostFormatter().format(previewText);
                preview.setValue(formattedPreview + "<br/>");
            }
        });

    }
}
