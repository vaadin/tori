package org.vaadin.tori.component.post;

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.service.post.PostReport;
import org.vaadin.tori.service.post.PostReport.Reason;
import org.vaadin.tori.service.post.PostReportReceiver;

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

    private final CssLayout explanationLayout;
    private final NativeButton reportButton;
    private final VerticalLayout layout = new VerticalLayout();

    public ReportWindow(final Post post, final PostReportReceiver reportReciever) {
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

        reason.addItem(Reason.SPAM);
        reason.setItemCaption(Reason.SPAM, "Spam");
        reason.addItem(Reason.OFFENSIVE);
        reason.setItemCaption(Reason.OFFENSIVE,
                "Offensive, abusive or hateful.");
        reason.addItem(Reason.WRONG_CATEGORY);
        reason.setItemCaption(Reason.WRONG_CATEGORY,
                "Doesn't belong in the category.");
        reason.addItem(Reason.MODERATOR_ALERT);
        reason.setItemCaption(Reason.MODERATOR_ALERT,
                "A moderator should take a look at it.");

        reason.setImmediate(true);
        reason.addListener(new OptionGroup.ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                explanationLayout.setVisible(reason.getValue() == Reason.MODERATOR_ALERT);
                reportButton.setEnabled(reason.getValue() != null);
            }
        });
        reasonLayout.addComponent(reason);

        explanationLayout = new CssLayout();
        explanationLayout.setStyleName("explanationlayout");
        explanationLayout.addComponent(new Label("Here's why:"));
        final TextArea reasonText = createReasonTextArea();
        explanationLayout.addComponent(reasonText);
        explanationLayout.setVisible(false);
        explanationLayout.setWidth("100%");
        reasonLayout.addComponent(explanationLayout);

        final HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        addComponent(footer);

        reportButton = new NativeButton("Report Post");
        reportButton.addListener(new NativeButton.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                final PostReport report = new PostReport(post, (Reason) reason
                        .getValue(), (String) reasonText.getValue());
                reportReciever.handlePostReport(report);
                ReportWindow.this.close();
            }
        });
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
