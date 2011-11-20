package org.vaadin.tori.component;

import org.vaadin.hene.expandingtextarea.ExpandingTextArea;
import org.vaadin.tori.ToriApplication;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;

@SuppressWarnings("serial")
public class ReplyComponent extends CustomComponent {

    public interface ReplyListener {
        void sendReply(String rawBody);
    }

    private final ClickListener CLEAR_LISTENER = new NativeButton.ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            resetInput();
        }
    };

    private final ClickListener POST_LISTENER = new NativeButton.ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            listener.sendReply((String) input.getValue());
            resetInput();
        }
    };

    private final TextChangeListener INPUT_CHANGE_LISTENER = new FieldEvents.TextChangeListener() {
        @Override
        public void textChange(final TextChangeEvent event) {
            final String previewText = event.getText();
            final String formattedPreview = ToriApplication.getCurrent()
                    .getPostFormatter().format(previewText);
            preview.setValue(formattedPreview + "<br/>");
        }
    };

    private final CustomLayout layout = new CustomLayout(
            ToriApplication.CUSTOM_LAYOUT_PATH + "replylayout");
    private final ReplyListener listener;
    private final ExpandingTextArea input;
    private final Label preview;

    /**
     * @param formattingSyntaxXhtml
     *            The forum post formatting reference that will be shown as-is
     *            to the user, when she clicks on the designated help button.
     *            The string must be formatted in valid XHTML.
     */
    public ReplyComponent(final ReplyListener listener,
            final String formattingSyntaxXhtml) {
        this.listener = listener;

        setCompositionRoot(layout);
        setStyleName("reply");
        layout.setWidth("100%");
        setWidth("100%");

        layout.addComponent(new PopupView("Show Formatting Syntax",
                getSyntaxLabel(formattingSyntaxXhtml)), "formattingsyntax");

        input = new ExpandingTextArea();
        input.setWidth("100%");
        layout.addComponent(input, "input");

        preview = new Label("<br/>", Label.CONTENT_XHTML);
        layout.addComponent(preview, "preview");
        layout.addComponent(new NativeButton("Post", POST_LISTENER),
                "postbutton");
        layout.addComponent(new NativeButton("Clear", CLEAR_LISTENER),
                "clearbutton");

        input.addListener(INPUT_CHANGE_LISTENER);

    }

    private static Label getSyntaxLabel(final String formattingSyntaxXhtml) {
        final Label label = new Label(formattingSyntaxXhtml,
                Label.CONTENT_XHTML);
        label.setWidth("200px");
        return label;
    }

    private void resetInput() {
        input.setValue("");
        preview.setValue("<br/>");
    }
}
