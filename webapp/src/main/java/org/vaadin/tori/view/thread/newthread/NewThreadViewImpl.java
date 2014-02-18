/*
 * Copyright 2014 Vaadin Ltd.
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

package org.vaadin.tori.view.thread.newthread;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.AuthoringComponent;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.util.ComponentUtil.HeadingLevel;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class NewThreadViewImpl extends
        AbstractView<NewThreadView, NewThreadPresenter> implements
        NewThreadView {

    private CssLayout layout;

    private AuthoringComponent newThreadComponent;

    private TextField topicField;

    @Override
    protected Component createCompositionRoot() {
        return layout = new CssLayout();
    }

    @Override
    public void initView() {
        setStyleName("newthreadview");
        layout.setWidth(100.0f, Unit.PERCENTAGE);
        layout.addComponent(buildTopicLayout());
        layout.addComponent(buildAuthoringComponent());

    }

    private Component buildAuthoringComponent() {
        // AuthoringListener listener = new AuthoringListener() {
        // @Override
        // public void submit(String rawBody, Map<String, byte[]> attachments) {
        // final String topic = topicField.getValue();
        // getPresenter().createNewThread(topic, rawBody, attachments);
        // }
        //
        // };
        // return newThreadComponent = new AuthoringComponent(listener,
        // "Post body", true);
        return null;
    }

    private Component buildTopicLayout() {
        VerticalLayout result = new VerticalLayout();

        final Label heading = ComponentUtil.getHeadingLabel(
                "Start a New Thread", HeadingLevel.H2);
        result.addComponent(heading);

        final HorizontalLayout topicLayout = new HorizontalLayout();
        topicLayout.setWidth(70.0f, Unit.PERCENTAGE);
        topicLayout.setSpacing(true);
        topicLayout.setMargin(new MarginInfo(true, false, false, false));
        topicLayout.setStyleName("newthread");

        final Label topicLabel = ComponentUtil.getHeadingLabel("Topic",
                HeadingLevel.H3);
        topicLabel.addStyleName("topiclabel");
        topicLabel.setWidth("153px");
        topicLayout.addComponent(topicLabel);

        topicField = new TextField();
        topicField.setStyleName("topicfield");
        topicField.setWidth("100%");
        topicLayout.addComponent(topicField);
        topicLayout.setExpandRatio(topicField, 1.0f);
        topicField.focus();

        result.addComponent(topicLayout);

        return result;
    }

    @Override
    protected NewThreadPresenter createPresenter() {
        return new NewThreadPresenter(this);
    }

    @Override
    public void redirectToDashboard() {
        UI.getCurrent()
                .getNavigator()
                .navigateTo(
                        ToriNavigator.ApplicationView.DASHBOARD
                                .getNavigatorUrl());
    }

    @Override
    public String getTitle() {
        return "New thread";
    }

    @Override
    public void showNotification(String message) {
        Notification.show(message);
    }

    @Override
    public void showError(String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void setViewPermissions(ViewPermissions viewPermissions) {
        // newThreadComponent.setUserMayAddFiles(viewPermissions.mayAddFiles());
        // newThreadComponent.setMaxFileSize(viewPermissions.getMaxFileSize());
    }

    @Override
    public void newThreadCreated(long threadId) {
        ToriUI.getCurrent().trackAction("new-thread");
        ToriNavigator.getCurrent().navigateToThread(threadId);
    }
}
