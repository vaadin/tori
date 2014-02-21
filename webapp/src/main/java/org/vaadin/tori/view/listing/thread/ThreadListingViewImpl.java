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

package org.vaadin.tori.view.listing.thread;

import java.util.List;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.view.listing.thread.ThreadMoveComponent.ThreadMoveComponentListener;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ThreadListingViewImpl extends
        AbstractView<ThreadListingView, ThreadListingPresenter> implements
        ThreadListingView {

    private VerticalLayout layout;
    private ThreadListing threadListing;
    private Button createTopicButton;
    private Label noTopicsLabel;

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void initView() {
        setStyleName("threadlistingview");
        layout.addComponent(buildTopicHeader());
        layout.addComponent(buildTopicListing());
    }

    private Component buildTopicListing() {
        threadListing = new ThreadListing(getPresenter());
        return threadListing;
    }

    private Component buildTopicHeader() {
        HorizontalLayout result = ComponentUtil.getHeaderLayout("Topics");
        noTopicsLabel = new Label("No topics");
        noTopicsLabel.setSizeUndefined();
        result.addComponent(noTopicsLabel);
        result.setComponentAlignment(noTopicsLabel, Alignment.MIDDLE_CENTER);

        createTopicButton = new Button("+ Create Topic", new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                getPresenter().createTopicRequested();
            }
        });

        Component buttonWrapper = new HorizontalLayout(createTopicButton);
        result.addComponent(buttonWrapper);
        result.setComponentAlignment(buttonWrapper, Alignment.MIDDLE_RIGHT);
        return result;
    }

    @Override
    protected ThreadListingPresenter createPresenter() {
        return new ThreadListingPresenter(this);
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

    @Override
    public void setThreadProvider(final ThreadProvider threadProvider) {
        boolean hasThreads = threadProvider.getThreadCount() > 0;
        noTopicsLabel.setVisible(!hasThreads);
        threadListing.setVisible(hasThreads);
        threadListing.setThreadProvider(threadProvider);
    }

    @Override
    public void showThreadMovePopup(final long threadId,
            final Long threadCategoryId, final List<Category> allCategories) {
        final Window window = new ThreadMoveComponent(threadId,
                threadCategoryId, allCategories,
                new ThreadMoveComponentListener() {
                    @Override
                    public void commit(final long threadId,
                            final Long newCategoryId) {
                        threadListing.removeThreadRow(threadId);
                        getPresenter().move(threadId, newCategoryId);
                    }
                });
        window.center();
        getUI().addWindow(window);
    }

    @Override
    public void showError(final String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void showNotification(final String message) {
        Notification.show(message);
    }

    @Override
    public void updateThread(final ThreadData thread) {
        threadListing.updateThreadRow(thread);
    }

    @Override
    public void setMayCreateThreads(final boolean mayCreateThreads) {
        createTopicButton.setVisible(mayCreateThreads);
    }

    @Override
    public void navigateToNewThreadView(final Long categoryId) {
        ToriNavigator.getCurrent().navigateToNewThread(categoryId);
    }

}
