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

package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.category.CategoryListing;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.component.thread.ThreadListing2;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.thread.ThreadPresenter;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CategoryViewImpl extends
        AbstractView<CategoryView, CategoryPresenter> implements CategoryView {

    private final HeadingLabel NO_THREADS = new HeadingLabel(
            "There are no threads in this category", HeadingLevel.H1);
    private final HeadingLabel THREADS = new HeadingLabel("Threads",
            HeadingLevel.H2);

    private VerticalLayout layout;
    private Component threadListingPlaceHolder;
    private ThreadListing2 threadListing;
    private CategoryListing categoryListing;
    private VerticalLayout categoryLayout;
    private HeadingLabel threadsLabel;
    private Button newThreadButton1;
    private Button newThreadButton2;

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void initView() {
        setStyleName("categoryview");

        categoryLayout = new VerticalLayout();
        layout.addComponent(categoryLayout);

        categoryLayout.addComponent(new HeadingLabel("Contained Categories",
                HeadingLevel.H2));
        categoryLayout.addComponent(categoryListing = new CategoryListing(
                Mode.SINGLE_COLUMN));

        final VerticalLayout threadLayout = new VerticalLayout();
        threadLayout.setMargin(new MarginInfo(true, false, false, false));
        threadLayout.setSpacing(true);
        layout.addComponent(threadLayout);

        final HorizontalLayout threadHeaderLayout = new HorizontalLayout();
        threadHeaderLayout.setWidth("100%");
        threadLayout.addComponent(threadHeaderLayout);

        threadsLabel = THREADS;
        threadHeaderLayout.addComponent(threadsLabel);

        newThreadButton1 = createNewThreadButton();
        threadHeaderLayout.addComponent(newThreadButton1);
        threadHeaderLayout.setComponentAlignment(newThreadButton1,
                Alignment.MIDDLE_RIGHT);

        threadLayout.addComponent(threadListingPlaceHolder = new Label(
                "placeholder"));

        newThreadButton2 = createNewThreadButton();
        threadLayout.addComponent(newThreadButton2);
    }

    private Button createNewThreadButton() {
        final Button button = new Button("Start a new thread");
        button.setIcon(new ThemeResource("images/icon-newthread.png"));
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                UI.getCurrent()
                        .getNavigator()
                        .navigateTo(
                                String.format("%s/%s/%s",
                                        ToriNavigator.ApplicationView.THREADS
                                                .getNavigatorUrl(),
                                        ThreadPresenter.NEW_THREAD_ARGUMENT,
                                        getPresenter().getCurrentCategory()
                                                .getId()));
            }
        });
        button.setWidth("163px");
        return button;
    }

    @Override
    protected CategoryPresenter createPresenter() {
        return new CategoryPresenter(this);
    }

    @Override
    public void displaySubCategories(final List<Category> subCategories,
            final boolean specialCategory) {
        if (!subCategories.isEmpty()) {
            categoryListing.replaceCreateCategoryButton();
            categoryListing.setCategories(subCategories, getCurrentCategory());
        } else {
            if (getPresenter().userCanCreateSubcategory() && !specialCategory) {
                categoryLayout.setVisible(true);
                final Component createCategoryButton = categoryListing
                        .removeAndGetCreateCategoryButton();
                categoryLayout.addComponent(createCategoryButton, 1);
                categoryListing.setCategories(subCategories,
                        getCurrentCategory());
                categoryListing.setVisible(false);
            } else {
                categoryLayout.setVisible(false);
            }
        }
    }

    private void replacePlaceholder() {
        final ComponentContainer listingParent = (ComponentContainer) threadListingPlaceHolder
                .getParent();
        if (listingParent != null) {
            listingParent.replaceComponent(threadListingPlaceHolder,
                    threadListing);
            // in case we need to replace the thread view again.
            threadListingPlaceHolder = threadListing;
        }
    }

    @Override
    public void displayCategoryNotFoundError(final String requestedCategoryId) {
        layout.removeAllComponents();
        layout.addComponent(new HeadingLabel(
                "No such category found. You probably followed a broken link...",
                HeadingLevel.H1));
    }

    @Override
    public Category getCurrentCategory() {
        return getPresenter().getCurrentCategory();
    }

    @Override
    public void confirmFollowing(final DiscussionThread thread) {
        // TODO:
        // threadListing.refreshStyles(thread);
    }

    @Override
    public void confirmUnfollowing(final DiscussionThread thread) {
        // TODO:
        // threadListing.refreshStyles(thread);
    }

    @Override
    public void confirmThreadMoved() {
        Notification.show("Thread moved");
    }

    @Override
    public void confirmThreadStickied(final DiscussionThread thread) {
        Notification.show("Thread stickied");
        // presenter will reset the view here
    }

    @Override
    public void confirmThreadUnstickied(final DiscussionThread thread) {
        Notification.show("Thread unstickied");
        // presenter will reset the view here
    }

    @Override
    public void confirmThreadLocked(final DiscussionThread thread) {
        refreshVisually(thread);
    }

    @Override
    public void confirmThreadUnlocked(final DiscussionThread thread) {
        refreshVisually(thread);
    }

    private void refreshVisually(final DiscussionThread thread) {
        // TODO:
        // threadListing.refresh(thread);
    }

    @Override
    public void confirmThreadDeleted() {
        Notification.show("Thread deleted");
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

    @Override
    public void displayThreads(final ThreadProvider threadProvider) {
        threadListing = new ThreadListing2(getPresenter(), threadProvider);
        replacePlaceholder();
        setThreadLabel(THREADS);
    }

    @Override
    public void hideThreads() {
        setThreadLabel(NO_THREADS);
        threadListingPlaceHolder.setVisible(false);
    }

    private void setThreadLabel(final HeadingLabel newLabel) {
        if (newLabel != threadsLabel) {
            ((ComponentContainer) threadsLabel.getParent()).replaceComponent(
                    threadsLabel, newLabel);
        }
    }

    @Override
    public void setUserMayStartANewThread(final boolean userMayStartANewThread) {
        newThreadButton1.setVisible(userMayStartANewThread);
        newThreadButton2.setVisible(userMayStartANewThread);
    }

    @Override
    public String getTitle() {
        return getPresenter().getCategoryName();
    }
}
