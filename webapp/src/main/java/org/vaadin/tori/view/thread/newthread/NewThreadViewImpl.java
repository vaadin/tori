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

import java.util.Map;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.AuthoringComponent;
import org.vaadin.tori.component.AuthoringComponent.AuthoringListener;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.view.thread.AuthoringData;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class NewThreadViewImpl extends
        AbstractView<NewThreadView, NewThreadPresenter> implements
        NewThreadView {

    private VerticalLayout layout;

    private AuthoringComponent authoringComponent;

    private TextField topicField;

    @Override
    protected Component createCompositionRoot() {
        layout = new VerticalLayout();
        layout.setSpacing(true);
        return layout;
    }

    @Override
    public void initView() {
        setStyleName("newthreadview");
        layout.setWidth(100.0f, Unit.PERCENTAGE);
        layout.addComponent(buildTopicLayout());
        layout.addComponent(buildAuthoringComponent());

    }

    private Component buildAuthoringComponent() {
        AuthoringListener listener = new AuthoringListener() {

            @Override
            public void submit(String rawBody, Map<String, byte[]> attachments,
                    boolean follow) {
                String topic = topicField.getValue();
                getPresenter().saveNewThread(topic, rawBody, attachments,
                        follow);
            }

            @Override
            public void inputValueChanged(String value) {
                // Ignore
            }
        };

        authoringComponent = new AuthoringComponent(listener);
        return authoringComponent;
    }

    private Component buildTopicLayout() {
        VerticalLayout result = new VerticalLayout();

        final HorizontalLayout topicLayout = new HorizontalLayout();
        topicLayout.setWidth(100.0f, Unit.PERCENTAGE);
        topicLayout.setMargin(true);
        topicLayout.setStyleName("newthread");

        topicField = new TextField("Topic");
        topicField.setStyleName("topicfield");
        topicField.setWidth("70%");
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
    public void newThreadCreated(long threadId) {
        ToriUI.getCurrent().trackAction("new-thread");
        ToriNavigator.getCurrent().navigateToThread(threadId);
    }

    @Override
    public void setViewData(AuthoringData authoringData) {
        authoringComponent.setAuthoringData(authoringData);
    }
}
