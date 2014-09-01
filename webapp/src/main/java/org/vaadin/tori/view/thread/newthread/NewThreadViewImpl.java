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
import org.vaadin.tori.component.RecentBar;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.util.InputCacheUtil;
import org.vaadin.tori.util.InputCacheUtil.Callback;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.thread.AuthoringData;

import com.vaadin.shared.ui.MarginInfo;
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

    private static final String CACHE_KEY_PREFIX = "new_";
    private VerticalLayout layout;

    private AuthoringComponent authoringComponent;

    private TextField topicField;

    private ViewData viewData;

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
        layout.addStyleName("post");
        layout.addStyleName("editing");
    }

    private Component buildAuthoringComponent() {
        AuthoringListener listener = new AuthoringListener() {

            @Override
            public void submit(final String rawBody,
                    final Map<String, byte[]> attachments, final boolean follow) {
                String topic = topicField.getValue();
                getPresenter().saveNewThread(topic, rawBody, attachments,
                        follow);
                RecentBar.getCurrent().refresh();
            }

            @Override
            public void inputValueChanged(final String value) {
                if (viewData != null) {
                    InputCacheUtil.put(
                            CACHE_KEY_PREFIX + viewData.getCategoryId(), value);
                }
            }
        };

        authoringComponent = new AuthoringComponent(listener);
        authoringComponent.setPostButtonCaption("Submit");
        return authoringComponent;
    }

    private Component buildTopicLayout() {
        VerticalLayout result = new VerticalLayout();

        final HorizontalLayout topicLayout = new HorizontalLayout();
        topicLayout.setWidth(100.0f, Unit.PERCENTAGE);
        topicLayout.setMargin(new MarginInfo(true, true, false, true));
        topicLayout.setStyleName("newthreadtopic");

        topicField = new TextField();

        topicField.setStyleName("topicfield");
        topicField.setInputPrompt("Write your topic title here...");
        topicField.setWidth(100.0f, Unit.PERCENTAGE);
        topicField.setHeight(48.0f, Unit.PIXELS);
        topicLayout.addComponent(topicField);
        topicLayout.setExpandRatio(topicField, 1.0f);

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
        return null;
    }

    @Override
    public void showNotification(final String message) {
        Notification.show(message);
    }

    @Override
    public void showError(final String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void newThreadCreated(final long threadId) {
        InputCacheUtil.remove(CACHE_KEY_PREFIX + viewData.getCategoryId());

        ToriUI.getCurrent().trackAction("new-thread");
        ToriNavigator.getCurrent().navigateToThread(threadId);
    }

    @Override
    public void setViewData(final ViewData viewData,
            final AuthoringData authoringData) {
        this.viewData = viewData;
        authoringComponent.setAuthoringData(authoringData);

        InputCacheUtil.get(CACHE_KEY_PREFIX + viewData.getCategoryId(),
                new Callback() {
                    @Override
                    public void execute(final String value) {
                        ToriScheduler.get().scheduleDeferred(
                                new ScheduledCommand() {
                                    @Override
                                    public void execute() {
                                        authoringComponent
                                                .insertIntoMessage(value);
                                    }
                                });
                    }
                });
    }

    @Override
    public Long getUrlParameterId() {
        return viewData.getCategoryId();
    }

    @Override
    public void authoringFailed() {
        authoringComponent.reEnablePosting();
    }
}
