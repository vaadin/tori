package org.vaadin.tori.component.post;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
class ReportWindow extends Window {

    private static final Object REASON_SPAM = "Spam.";
    private static final Object REASON_OFFENSIVE = "Offensive, abusive or hateful.";
    private static final Object REASON_CATEGORY = "Doesn't belong in this category.";
    private static final Object REASON_OTHER = "A moderator should take a look at it.";

    private final CssLayout explanationLayout;
    private final NativeButton reportButton;
    private final VerticalLayout layout = new VerticalLayout();

    public ReportWindow() {
        super("Report Post");
        layout.setSpacing(true);
        layout.setMargin(true);

        setContent(layout);
        setStyleName("reportwindow");
        setClosable(false);
        setCloseShortcut(KeyCode.ESCAPE);
        setResizable(false);
        getContent().setWidth("100%");
        setWidth("350px");
        focus();
        center();

        addComponent(new Label("What's wrong with this post?"));

        final CssLayout reasonLayout = new CssLayout();
        reasonLayout.setWidth("100%");
        addComponent(reasonLayout);

        final OptionGroup reason = new OptionGroup();
        reason.addItem(REASON_SPAM);
        reason.addItem(REASON_OFFENSIVE);
        reason.addItem(REASON_CATEGORY);
        reason.addItem(REASON_OTHER);
        reason.setImmediate(true);
        reason.addListener(new OptionGroup.ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                explanationLayout.setVisible(reason.getValue() == REASON_OTHER);
                reportButton.setEnabled(reason.getValue() != null);
            }
        });
        reasonLayout.addComponent(reason);

        explanationLayout = new CssLayout();
        explanationLayout.setStyleName("explanationlayout");
        explanationLayout.addComponent(new Label("Here's why:"));
        explanationLayout.addComponent(createReasonTextArea());
        explanationLayout.setVisible(false);
        explanationLayout.setWidth("100%");
        reasonLayout.addComponent(explanationLayout);

        final HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        addComponent(footer);

        reportButton = new NativeButton("Report Post");
        reportButton.setEnabled(false);
        footer.addComponent(reportButton);

        final NativeButton cancel = new NativeButton("Cancel");
        cancel.addListener(new NativeButton.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                ReportWindow.this.close();
            }
        });
        footer.addComponent(cancel);
    }

    private static TextArea createReasonTextArea() {
        final TextArea area = new TextArea();
        area.setWidth("100%");
        area.setRows(4);
        return area;
    }
}
