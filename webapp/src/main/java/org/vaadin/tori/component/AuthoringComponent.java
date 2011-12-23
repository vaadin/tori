package org.vaadin.tori.component;

import java.util.Collection;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;
import org.vaadin.tori.util.PostFormatter.FormatInfo;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.terminal.gwt.server.AbstractWebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
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
import com.vaadin.ui.TextArea;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("serial")
public abstract class AuthoringComponent extends CustomComponent {
    private static final class ToolbarUtil {

        @NonNull
        private static Component createFontWidget(
                @NonNull final Collection<FontFace> fontFaces,
                final TextArea textArea) {
            ToriUtil.checkForNullAndEmpty(fontFaces,
                    "fontFaces may not be null", "fontFaces may not be empty");

            final AbstractSelect select = createSelect("Font face", "Font",
                    textArea);

            for (final FontFace font : fontFaces) {
                select.addItem(font);
                select.setItemCaption(font, font.getFontName());
            }

            return select;
        }

        private static AbstractSelect createSelect(final String description,
                final String caption, final TextArea textArea) {
            final NativeSelect select = new NativeSelect();
            select.setImmediate(true);
            select.setDescription(description);

            final Object nullId = new Object();
            select.addItem(nullId);
            select.setItemCaption(nullId, caption);
            select.setNullSelectionItemId(nullId);

            select.addListener(new ValueChangeListener() {
                private boolean ignoreEvent = false;

                @Override
                public synchronized void valueChange(
                        final ValueChangeEvent event) {
                    if (!ignoreEvent) {
                        ignoreEvent = true;
                        try {
                            final String appendedString;

                            final Object value = event.getProperty().getValue();
                            if (value instanceof FontFace) {
                                appendedString = ((FontFace) value)
                                        .getFontSyntax();
                            } else if (value instanceof FontSize) {
                                appendedString = ((FontSize) value)
                                        .getFontSizeSyntax();
                            } else {
                                throw new IllegalStateException(value
                                        .getClass() + " is an unexpected type");
                            }

                            final String text = (String) textArea.getValue();
                            textArea.setValue(text + appendedString);

                            // Reset selection.
                            final Object nullId = select
                                    .getNullSelectionItemId();
                            select.select(nullId);

                            textArea.focus();
                        }

                        finally {
                            ignoreEvent = false;
                        }
                    }
                }
            });

            return select;
        }

        @NonNull
        private static Component createSizeWidget(
                @NonNull final Collection<FontSize> fontSizes,
                final TextArea textArea) {
            ToriUtil.checkForNullAndEmpty(fontSizes,
                    "fontSizes may not be null", "fontSizes may not be empty");

            final AbstractSelect select = createSelect("Font size", "Size",
                    textArea);

            for (final FontSize size : fontSizes) {
                select.addItem(size);
                select.setItemCaption(size, size.getFontSizeName());
            }

            return select;
        }

        private static Button createButton(final FormatInfo formatInfo,
                final TextArea textArea) {
            final NativeButton button = new NativeButton(
                    formatInfo.getFormatName());
            button.setDescription(formatInfo.getFormatName());
            button.addListener(new ClickListener() {
                @Override
                public void buttonClick(final ClickEvent event) {
                    final String text = (String) textArea.getValue();
                    textArea.setValue(text + formatInfo.getFormatSyntax());
                    textArea.focus();
                }
            });
            return button;
        }

        @CheckForNull
        private static Component createBoldButton(
                @CheckForNull final FormatInfo boldInfo,
                @NonNull final WebBrowser browser,
                @NonNull final TextArea textArea) {

            if (boldInfo == null) {
                return null;
            }

            final Button button = createButton(boldInfo, textArea);
            final String description = button.getDescription();
            if (!browser.isMacOSX()) {
                button.setClickShortcut(KeyCode.B, ModifierKey.CTRL);
                button.setDescription(description + " <i>(Ctrl+B)</i>");
            } else {
                button.setClickShortcut(KeyCode.B, ModifierKey.META);
                button.setDescription(description + " <i>(\u2318-B)</i>");
            }
            return button;
        }

        @CheckForNull
        private static Component createItalicButton(
                @CheckForNull final FormatInfo italicInfo,
                @NonNull final WebBrowser browser,
                @NonNull final TextArea textArea) {

            if (italicInfo == null) {
                return null;
            }

            final Button button = createButton(italicInfo, textArea);
            final String description = button.getDescription();
            if (!browser.isMacOSX()) {
                button.setClickShortcut(KeyCode.I, ModifierKey.CTRL);
                button.setDescription(description + " <i>(Ctrl-I)</i>");
            } else {
                button.setClickShortcut(KeyCode.I, ModifierKey.META);
                button.setDescription(description + " <i>(\u2318-I)</i>");
            }
            return button;
        }

        public static void addBoldAndItalicWidgets(
                @NonNull final TextArea textArea,
                @NonNull final HorizontalLayout layout,
                @NonNull final PostFormatter postFormatter) {

            final WebBrowser browser = ((AbstractWebApplicationContext) ToriApplication
                    .getCurrent().getContext()).getBrowser();

            final Component boldButton = createBoldButton(
                    postFormatter.getBoldInfo(), browser, textArea);
            if (boldButton != null) {
                layout.addComponent(boldButton);
            }

            final Component italicButton = createItalicButton(
                    postFormatter.getItalicInfo(), browser, textArea);
            if (italicButton != null) {
                layout.addComponent(italicButton);
            }
        }

        public static void addFontWidgets(@NonNull final TextArea textArea,
                @NonNull final HorizontalLayout layout,
                @NonNull final PostFormatter postFormatter) {
            final FontsInfo fontsInfo = postFormatter.getFontsInfo();

            final Collection<FontFace> fontFaces = fontsInfo.getFontFaces();
            if (fontFaces != null && !fontFaces.isEmpty()) {
                layout.addComponent(AuthoringComponent.ToolbarUtil
                        .createFontWidget(fontFaces, textArea));
            }

            final Collection<FontSize> fontSizes = fontsInfo.getFontSizes();
            if (fontSizes != null && !fontSizes.isEmpty()) {
                layout.addComponent(AuthoringComponent.ToolbarUtil
                        .createSizeWidget(fontSizes, textArea));
            }
        }

        public static void addOtherWidgets(@NonNull final TextArea textArea,
                @NonNull final HorizontalLayout layout,
                @NonNull final PostFormatter postFormatter) {

            final Collection<FormatInfo> info = postFormatter
                    .getOtherFormattingInfo();

            if (info != null) {
                for (final FormatInfo format : info) {
                    layout.addComponent(createButton(format, textArea));
                }
            }
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

    public AuthoringComponent(final AuthoringListener listener,
            final String formattingSyntaxXhtml, final String captionText) {
        this(listener, formattingSyntaxXhtml, captionText, null);
    }

    /**
     * @param formattingSyntaxXhtml
     *            The forum post formatting reference that will be shown as-is
     *            to the user, when she clicks on the designated help button.
     *            The string must be formatted in valid XHTML.
     */
    public AuthoringComponent(final AuthoringListener listener,
            final String formattingSyntaxXhtml, final String captionText,
            final String inputPrompt) {
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

        layout.addComponent(new PopupView("Show Formatting Syntax",
                getSyntaxLabel(formattingSyntaxXhtml)), "formattingsyntax");

        input = new ToriExpandingTextArea();
        if (inputPrompt != null) {
            input.setInputPrompt(inputPrompt);
        }
        input.setMaxRows(MAX_ROWS_EXPANDED);
        input.setImmediate(true);
        layout.addComponent(input, "input");

        preview = new Label("<br/>", Label.CONTENT_XHTML);
        preview.setWidth(null);
        layout.addComponent(preview, "preview");
        layout.addComponent(new NativeButton("Post", POST_LISTENER),
                "postbutton");
        layout.addComponent(new NativeButton("Clear", CLEAR_LISTENER),
                "clearbutton");

        layout.addComponent(createToolbar(input), "toolbar");

        input.addListener(INPUT_CHANGE_LISTENER);
        input.addListener(VALUE_CHANGE_LISTENER);
        setCompactMode(false);
    }

    private Component createToolbar(final TextArea textArea) {

        final HorizontalLayout layout = new HorizontalLayout();

        final PostFormatter postFormatter = ToriApplication.getCurrent()
                .getPostFormatter();

        ToolbarUtil.addFontWidgets(textArea, layout, postFormatter);
        ToolbarUtil.addBoldAndItalicWidgets(textArea, layout, postFormatter);
        ToolbarUtil.addOtherWidgets(textArea, layout, postFormatter);

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
            input.setMaxRows(MAX_ROWS_COMPACT);
            addStyleName("compact");
        } else {
            input.setMaxRows(MAX_ROWS_EXPANDED);
            removeStyleName("compact");
        }
        compactMode = compact;
    }

}
