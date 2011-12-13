package org.vaadin.tori.component;

import java.util.Collection;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PopupView;

@SuppressWarnings("serial")
public abstract class AuthoringComponent extends CustomComponent {
    public static final class ToolbarUtil {

        public static Component createFontWidget(
                final Collection<FontFace> fontFaces) {
            final NativeSelect select = new NativeSelect();
            // TODO Auto-generated method stub
            return select;
        }

        public static Component createSizeWidget(
                final Collection<FontSize> fontSizes) {
            final NativeSelect select = new NativeSelect();
            // TODO Auto-generated method stub
            return select;
        }

    }

    private static final int MAX_ROWS_EXPANDED = 10;
    private static final int MAX_ROWS_COMPACT = 2;

    protected interface AuthoringListener {
        void submit(String rawBody);
    }

    private final ClickListener CLEAR_LISTENER = new NativeButton.ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            resetInput();
            input.focus();
        }
    };

    private final ClickListener POST_LISTENER = new NativeButton.ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            listener.submit((String) input.getValue());
            resetInput();
        }
    };

    private final TextChangeListener INPUT_CHANGE_LISTENER = new FieldEvents.TextChangeListener() {
        @Override
        public void textChange(final TextChangeEvent event) {
            final String previewText = event.getText();
            updatePreview(previewText);
        }
    };

    private final ValueChangeListener VALUE_CHANGE_LISTENER = new Property.ValueChangeListener() {
        @Override
        public void valueChange(final ValueChangeEvent event) {
            // update the preview also on value change
            final String previewText = (String) event.getProperty().getValue();
            updatePreview(previewText);
        }
    };

    private final LayoutClickListener COLLAPSE_LISTENER = new LayoutEvents.LayoutClickListener() {
        @Override
        public void layoutClick(final LayoutClickEvent event) {
            // toggle compact mode
            setCompactMode(!compactMode);
        }
    };

    private final CustomLayout layout = new CustomLayout("replylayout");
    private final AuthoringListener listener;
    private final ToriExpandingTextArea input;
    private final Label preview;
    private boolean compactMode;
    private final CssLayout captionLayout;

    /**
     * @param formattingSyntaxXhtml
     *            The forum post formatting reference that will be shown as-is
     *            to the user, when she clicks on the designated help button.
     *            The string must be formatted in valid XHTML.
     */
    public AuthoringComponent(final AuthoringListener listener,
            final String formattingSyntaxXhtml, final String captionText) {
        this.listener = listener;

        setCompositionRoot(layout);
        setStyleName("authoring");
        layout.setWidth("100%");
        setWidth("100%");

        captionLayout = new CssLayout();
        final Label captionLabel = new Label(captionText);
        captionLabel.setWidth(null);
        captionLayout.addComponent(captionLabel);
        layout.addComponent(captionLayout, "captionlabel");

        layout.addComponent(createToolbar(), "toolbar");
        layout.addComponent(new PopupView("Show Formatting Syntax",
                getSyntaxLabel(formattingSyntaxXhtml)), "formattingsyntax");

        input = new ToriExpandingTextArea();
        input.setMaxRows(MAX_ROWS_EXPANDED);
        input.setWidth("100%");
        input.setImmediate(true);
        layout.addComponent(input, "input");

        preview = new Label("<br/>", Label.CONTENT_XHTML);
        layout.addComponent(preview, "preview");
        layout.addComponent(new NativeButton("Post", POST_LISTENER),
                "postbutton");
        layout.addComponent(new NativeButton("Clear", CLEAR_LISTENER),
                "clearbutton");

        input.addListener(INPUT_CHANGE_LISTENER);
        input.addListener(VALUE_CHANGE_LISTENER);
        setCompactMode(false);
    }

    private Component createToolbar() {

        final HorizontalLayout layout = new HorizontalLayout();

        final FontsInfo fontsInfo = ToriApplication.getCurrent()
                .getPostFormatter().getFontsInfo();

        layout.addComponent(ToolbarUtil.createFontWidget(fontsInfo
                .getFontFaces()));
        layout.addComponent(ToolbarUtil.createSizeWidget(fontsInfo
                .getFontSizes()));

        return layout;
    }

    public ToriExpandingTextArea getInput() {
        return input;
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

    private void updatePreview(final String unformattedText) {
        final String formattedPreview = ToriApplication.getCurrent()
                .getPostFormatter().format(unformattedText);
        preview.setValue(formattedPreview + "<br/>");
    }

    protected void setCollapsible(final boolean collapsible) {
        if (collapsible) {
            addStyleName("collapsible");
            captionLayout.addListener(COLLAPSE_LISTENER);
        } else {
            removeStyleName("collapsible");
            captionLayout.removeListener(COLLAPSE_LISTENER);
        }
    }

    protected void setCompactMode(final boolean compact) {
        if (compact) {
            input.setReadOnly(true);
            input.setMaxRows(MAX_ROWS_COMPACT);
            addStyleName("compact");
        } else {
            input.setReadOnly(false);
            input.setMaxRows(MAX_ROWS_EXPANDED);
            removeStyleName("compact");
        }
        compactMode = compact;
    }

}
