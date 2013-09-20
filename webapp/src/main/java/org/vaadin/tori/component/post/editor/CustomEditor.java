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

package org.vaadin.tori.component.post.editor;

import java.util.Collection;

import org.vaadin.tori.ToriUI;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.component.ToriExpandingTextArea;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;
import org.vaadin.tori.util.PostFormatter.FormatInfo;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.server.ClassResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("serial")
public class CustomEditor extends CustomField<String> {
    private static final class ToolbarUtil {

        private static final String TOOLBAR_ICON_HEIGHT = "18px";
        private static final String TOOLBAR_ICON_WIDTH = TOOLBAR_ICON_HEIGHT;

        @NonNull
        public static Component createToolbar(@NonNull final CustomEditor parent) {

            final HorizontalLayout layout = new HorizontalLayout();
            layout.setSpacing(true);
            layout.setMargin(true);

            final PostFormatter postFormatter = ToriUI.getCurrent()
                    .getPostFormatter();

            final FormatInfo boldInfo = postFormatter.getBoldInfo();
            final FormatInfo italicInfo = postFormatter.getItalicInfo();

            initializeShortcuts(boldInfo, italicInfo, parent);
            addFontWidgets(layout, postFormatter, parent);
            addBoldAndItalicButtons(layout, postFormatter, parent);
            addOtherButtons(layout, postFormatter, parent);

            return layout;
        }

        private static void initializeShortcuts(
                @CheckForNull final FormatInfo boldInfo,
                @CheckForNull final FormatInfo italicInfo,
                @NonNull final CustomEditor parent) {
            if (boldInfo != null) {
                parent.boldSyntax = boldInfo.getFormatSyntax();
            }

            if (italicInfo != null) {
                parent.italicSyntax = italicInfo.getFormatSyntax();
            }
        }

        @NonNull
        private static Component createFontWidget(
                @NonNull final Collection<FontFace> fontFaces,
                @NonNull final CustomEditor authoringComponent) {
            ToriUtil.checkForNullAndEmpty(fontFaces,
                    "fontFaces may not be null", "fontFaces may not be empty");

            final AbstractSelect select = createSelect("Font face", "Font",
                    authoringComponent);

            for (final FontFace font : fontFaces) {
                select.addItem(font);
                select.setItemCaption(font, font.getFontName());
            }

            return select;
        }

        private static AbstractSelect createSelect(
                @NonNull final String description,
                @NonNull final String caption,
                @NonNull final CustomEditor authoringComponent) {

            final NativeSelect select = new NativeSelect();
            select.setImmediate(true);
            select.setDescription(description);

            final Object nullId = new Object();
            select.addItem(nullId);
            select.setItemCaption(nullId, caption);
            select.setNullSelectionItemId(nullId);

            select.addValueChangeListener(new ValueChangeListener() {
                private boolean ignoreEvent = false;

                @Override
                public void valueChange(
                        com.vaadin.data.Property.ValueChangeEvent event) {
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

                            authoringComponent
                                    .insertIntoMessage(appendedString);

                            // Reset selection.
                            final Object nullId = select
                                    .getNullSelectionItemId();
                            select.select(nullId);
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
                @NonNull final CustomEditor authoringComponent) {

            ToriUtil.checkForNullAndEmpty(fontSizes,
                    "fontSizes may not be null", "fontSizes may not be empty");

            final AbstractSelect select = createSelect("Font size", "Size",
                    authoringComponent);

            for (final FontSize size : fontSizes) {
                select.addItem(size);
                select.setItemCaption(size, size.getFontSizeName());
            }

            return select;
        }

        private static Embedded createButton(
                @NonNull final FormatInfo formatInfo,
                @NonNull final CustomEditor authoringComponent) {

            final Embedded button = new Embedded(null, new ClassResource(
                    FormatInfo.ICON_PACKAGE + formatInfo.getFormatIcon()));
            button.setWidth(TOOLBAR_ICON_WIDTH);
            button.setHeight(TOOLBAR_ICON_HEIGHT);

            button.setDescription(formatInfo.getFormatName());
            button.addClickListener(new MouseEvents.ClickListener() {
                @Override
                public void click(final MouseEvents.ClickEvent event) {
                    authoringComponent.insertIntoMessage(formatInfo
                            .getFormatSyntax());
                }
            });
            return button;
        }

        @CheckForNull
        private static Component createBoldButton(
                @CheckForNull final FormatInfo boldInfo,
                @NonNull final WebBrowser browser,
                @NonNull final CustomEditor authoringComponent) {

            if (boldInfo == null) {
                return null;
            }

            final Embedded button = createButton(boldInfo, authoringComponent);
            final String description = button.getDescription();
            if (!browser.isMacOSX()) {
                button.setDescription(description + " <i>(Ctrl+B)</i>");
            } else {
                button.setDescription(description + " <i>(\u2318-B)</i>");
            }
            return button;
        }

        @CheckForNull
        private static Component createItalicButton(
                @CheckForNull final FormatInfo italicInfo,
                @NonNull final WebBrowser browser,
                @NonNull final CustomEditor authoringComponent) {

            if (italicInfo == null) {
                return null;
            }

            final Embedded button = createButton(italicInfo, authoringComponent);
            final String description = button.getDescription();
            if (!browser.isMacOSX()) {
                button.setDescription(description + " <i>(Ctrl-I)</i>");
            } else {
                button.setDescription(description + " <i>(\u2318-I)</i>");
            }
            return button;
        }

        private static void addBoldAndItalicButtons(
                @NonNull final HorizontalLayout layout,
                @NonNull final PostFormatter postFormatter,
                @NonNull final CustomEditor authoringComponent) {

            final WebBrowser browser = VaadinSession.getCurrent().getBrowser();

            final Component boldButton = createBoldButton(
                    postFormatter.getBoldInfo(), browser, authoringComponent);
            if (boldButton != null) {
                layout.addComponent(boldButton);
            }

            final Component italicButton = createItalicButton(
                    postFormatter.getItalicInfo(), browser, authoringComponent);
            if (italicButton != null) {
                layout.addComponent(italicButton);
            }
        }

        private static void addFontWidgets(
                @NonNull final HorizontalLayout layout,
                @NonNull final PostFormatter postFormatter,
                @NonNull final CustomEditor authoringComponent) {
            final FontsInfo fontsInfo = postFormatter.getFontsInfo();

            final Collection<FontFace> fontFaces = fontsInfo.getFontFaces();
            if (fontFaces != null && !fontFaces.isEmpty()) {
                layout.addComponent(CustomEditor.ToolbarUtil.createFontWidget(
                        fontFaces, authoringComponent));
            }

            final Collection<FontSize> fontSizes = fontsInfo.getFontSizes();
            if (fontSizes != null && !fontSizes.isEmpty()) {
                layout.addComponent(CustomEditor.ToolbarUtil.createSizeWidget(
                        fontSizes, authoringComponent));
            }
        }

        private static void addOtherButtons(
                @NonNull final HorizontalLayout layout,
                @NonNull final PostFormatter postFormatter,
                @NonNull final CustomEditor authoringComponent) {

            final Collection<? extends FormatInfo> info = postFormatter
                    .getOtherFormattingInfo();

            if (info != null) {
                for (final FormatInfo format : info) {
                    layout.addComponent(createButton(format, authoringComponent));
                }
            }
        }
    }

    private static final int MAX_ROWS_EXPANDED = 10;
    private static final int MAX_ROWS_COMPACT = 2;

    private final TextChangeListener INPUT_CHANGE_LISTENER = new FieldEvents.TextChangeListener() {
        @Override
        public void textChange(final TextChangeEvent event) {
            final String previewText = event.getText();
            updatePreview(previewText);
            setValue(previewText);
        }
    };

    private final ValueChangeListener VALUE_CHANGE_LISTENER = new Property.ValueChangeListener() {
        @Override
        public void valueChange(
                final com.vaadin.data.Property.ValueChangeEvent event) {
            // update the preview also on value change
            final String previewText = (String) event.getProperty().getValue();
            updatePreview(previewText);
            setValue(previewText);
        }
    };

    private final LayoutClickListener COLLAPSE_LISTENER = new LayoutEvents.LayoutClickListener() {
        @Override
        public void layoutClick(final LayoutClickEvent event) {
            // toggle compact mode
            setCompactMode(!compactMode);
        }
    };

    private final class BoldAndItalicShortcutHandler implements Handler {

        private final Action[] actions;
        private final ShortcutAction boldAction;
        private final ShortcutAction italicAction;

        public BoldAndItalicShortcutHandler() {
            final WebBrowser browser = VaadinSession.getCurrent().getBrowser();

            if (browser.isMacOSX()) {
                boldAction = new ShortcutAction("&Bold",
                        new int[] { ModifierKey.META });
                italicAction = new ShortcutAction("&Italic",
                        new int[] { ModifierKey.META });
            } else {
                boldAction = new ShortcutAction("&Bold",
                        new int[] { ModifierKey.CTRL });
                italicAction = new ShortcutAction("&Italic",
                        new int[] { ModifierKey.CTRL });
            }
            actions = new Action[] { boldAction, italicAction };

        }

        @Override
        public Action[] getActions(final Object target, final Object sender) {
            return actions;
        }

        @Override
        public void handleAction(final Action action, final Object sender,
                final Object target) {
            if (action == boldAction) {
                insertBold();
            } else if (action == italicAction) {
                insertItalic();
            }
        }
    }

    private final CustomLayout layout = new CustomLayout("replylayout");
    private final ToriExpandingTextArea input;
    private final Label preview;
    private boolean compactMode;
    private final CssLayout captionLayout;
    private final Handler actionHandler = new BoldAndItalicShortcutHandler();

    @CheckForNull
    private String italicSyntax;
    @CheckForNull
    private String boldSyntax;
    private final Panel panel;

    /**
     * @param formattingSyntaxXhtml
     *            The forum post formatting reference that will be shown as-is
     *            to the user, when she clicks on the designated help button.
     *            The string must be formatted in valid XHTML.
     */
    public CustomEditor(final String formattingSyntaxXhtml,
            final String captionText, final String inputPrompt) {

        panel = new Panel(layout);
        panel.addActionHandler(actionHandler);

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

        preview = new Label("<br/>", ContentMode.HTML);
        layout.addComponent(preview, "preview");

        layout.addComponent(ToolbarUtil.createToolbar(this), "toolbar");

        input.addTextChangeListener(INPUT_CHANGE_LISTENER);
        input.addValueChangeListener(VALUE_CHANGE_LISTENER);
        setCompactMode(false);

        addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                input.setValue((String) event.getProperty().getValue());
            }
        });
    }

    public ToriExpandingTextArea getInput() {
        return input;
    }

    private static Label getSyntaxLabel(final String formattingSyntaxXhtml) {
        final Label label = new Label(formattingSyntaxXhtml, ContentMode.HTML);
        label.setWidth("250px");
        return label;
    }

    private void updatePreview(final String unformattedText) {
        final String formattedPreview = ToriUI.getCurrent().getPostFormatter()
                .format(unformattedText);
        preview.setValue(formattedPreview + "<br/>");
    }

    protected void setCollapsible(final boolean collapsible) {
        if (collapsible) {
            addStyleName("collapsible");
            captionLayout.addLayoutClickListener(COLLAPSE_LISTENER);
        } else {
            removeStyleName("collapsible");
            captionLayout.removeLayoutClickListener(COLLAPSE_LISTENER);
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

    private void insertBold() {
        if (boldSyntax != null) {
            insertIntoMessage(boldSyntax);
        }
    }

    private void insertItalic() {
        if (italicSyntax != null) {
            insertIntoMessage(italicSyntax);
        }
    }

    public void insertIntoMessage(final String unformattedText) {
        final String text = input.getValue();
        input.setValue(text + unformattedText);
        input.focus();
    }

    public void setUserMayAddFiles(final boolean userMayAddFiles) {
        layout.getComponent("uploadbutton").setVisible(userMayAddFiles);
    }

    @Override
    protected Component initContent() {
        return panel;
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
    }
}
