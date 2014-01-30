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

package org.vaadin.tori.component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class AuthoringComponent extends CustomComponent {
    public interface AuthoringListener {
        void submit(String rawBody);

        void addAttachment(String attachmentFileName, byte[] data);

        void resetInput();

        void removeAttachment(String fileName);
    }

    private final ClickListener POST_LISTENER = new Button.ClickListener() {
        @Override
        public void buttonClick(final ClickEvent event) {
            listener.submit(input.getValue());
            resetInput();
        }
    };

    private final VerticalLayout layout = new VerticalLayout();
    private final AuthoringListener listener;
    private final Field<String> input;
    private final VerticalLayout attachmentsLayout;
    private String attachmentFileName;
    private ByteArrayOutputStream attachmentData;

    private String italicSyntax;

    private String boldSyntax;
    private int maxFileSize = 307200;

    private Upload attach;

    public AuthoringComponent(final AuthoringListener listener,
            final String captionText, final boolean autoGrow) {
        this.listener = listener;

        setCompositionRoot(layout);
        setStyleName("authoring");
        layout.setWidth("100%");
        layout.setSpacing(true);
        setWidth("100%");

        input = new BBCodeWysiwygEditor(captionText, autoGrow);
        layout.addComponent(input);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        Button postButton = new Button("Post", POST_LISTENER);
        postButton.addStyleName(Reindeer.BUTTON_DEFAULT);
        buttonsLayout.addComponent(postButton);

        final Receiver receiver = new Receiver() {

            @Override
            public OutputStream receiveUpload(final String filename,
                    final String mimeType) {
                attachmentData = new ByteArrayOutputStream();
                attachmentFileName = filename;
                return attachmentData;
            }
        };

        attach = new Upload(null, receiver);
        attach.setButtonCaption("Attach file");
        attach.setImmediate(true);
        attach.addSucceededListener(new Upload.SucceededListener() {
            @Override
            public void uploadSucceeded(final SucceededEvent event) {
                listener.addAttachment(attachmentFileName,
                        attachmentData.toByteArray());
                attachmentFileName = null;
                attachmentData = null;
            }
        });

        attach.addStartedListener(new Upload.StartedListener() {
            @Override
            public void uploadStarted(final StartedEvent event) {
                if (maxFileSize > 0 && event.getContentLength() > maxFileSize) {
                    attach.interruptUpload();
                    Notification.show("File size too large");
                    return;
                }
            }
        });
        buttonsLayout.addComponent(attach);

        // buttonsLayout.addComponent(new NativeButton("Clear",
        // CLEAR_LISTENER));

        layout.addComponent(buttonsLayout);

        attachmentsLayout = new VerticalLayout();
        attachmentsLayout.setVisible(false);
        attachmentsLayout.setCaption("Attachments");
        attachmentsLayout.setStyleName("attachments");
        layout.addComponent(attachmentsLayout);
    }

    public Field<String> getInput() {
        return input;
    }

    private void resetInput() {
        input.setValue("");
        listener.resetInput();
    }

    public void insertIntoMessage(final String unformattedText) {
        final String text = input.getValue();
        input.setValue(text + unformattedText);
        input.focus();
    }

    public void setUserMayAddFiles(final boolean userMayAddFiles) {
        attach.setVisible(userMayAddFiles);
    }

    public void setMaxFileSize(final int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void updateAttachmentList(
            final LinkedHashMap<String, byte[]> attachments) {
        attachmentsLayout.removeAllComponents();
        attachmentsLayout.setVisible(!attachments.isEmpty());
        for (final Entry<String, byte[]> entry : attachments.entrySet()) {
            final String fileName = entry.getKey();
            final int fileSize = entry.getValue().length;
            final String caption = String.format("%s (%s KB)", fileName,
                    fileSize / 1024);

            final TextField nameComponent = new TextField();
            nameComponent.setValue(caption);
            nameComponent.setReadOnly(true);
            nameComponent.setWidth("100%");
            try {
                nameComponent.addStyleName(fileName.substring(fileName
                        .lastIndexOf(".") + 1));
            } catch (final IndexOutOfBoundsException e) {
                // NOP
            }

            final HorizontalLayout wrapperLayout = new HorizontalLayout();
            wrapperLayout.addStyleName("filerow");
            wrapperLayout.addComponent(nameComponent);
            nameComponent.setWidth(300.0f, Unit.PIXELS);

            final Label deleteLabel = new Label();
            deleteLabel.setHeight(20.0f, Unit.PIXELS);
            deleteLabel.setWidth(20.0f, Unit.PIXELS);

            deleteLabel.addStyleName("deleteattachment");
            wrapperLayout.addComponent(deleteLabel);
            wrapperLayout.addLayoutClickListener(new LayoutClickListener() {
                @Override
                public void layoutClick(final LayoutClickEvent event) {
                    if (event.getChildComponent() == deleteLabel) {
                        listener.removeAttachment(fileName);
                    }
                }
            });

            attachmentsLayout.addComponent(wrapperLayout);

        }
    }
}
