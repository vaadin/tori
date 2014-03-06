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

package org.vaadin.tori.view.listing;

import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.view.listing.category.CategoryListingViewImpl;
import org.vaadin.tori.view.listing.thread.ThreadListingViewImpl;

import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ListingViewImpl extends
        AbstractView<ListingView, ListingPresenter> implements ListingView {

    private VerticalLayout layout;

    private String categoryName;
    private CategoryListingViewImpl categoryListingView;
    private ThreadListingViewImpl threadListingView;

    private Long categoryId;

    @Override
    protected ListingPresenter createPresenter() {
        return new ListingPresenter(this);
    }

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void initView() {
        setStyleName("listingview");

        categoryListingView = new CategoryListingViewImpl();
        categoryListingView.init();
        getPresenter().setCategoryListingPresenter(
                categoryListingView.getPresenter());
        layout.addComponent(categoryListingView);

        threadListingView = new ThreadListingViewImpl();
        threadListingView.init();
        getPresenter().setThreadListingPresenter(
                threadListingView.getPresenter());
        layout.addComponent(threadListingView);
    }

    @Override
    public void displayCategoryNotFoundError(final String requestedCategoryId) {
        layout.removeAllComponents();
        showError("No such category found. You probably followed a broken link...");
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

    @Override
    public void showError(final String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void setCategory(final Category category) {
        categoryName = category != null ? category.getName() : null;
        categoryId = category != null ? category.getId() : null;
    }

    @Override
    public Long getUrlParameterId() {
        return categoryId;
    }

    @Override
    public String getTitle() {
        return categoryName;
    }

    @Override
    public void setThreadsVisible(final boolean showThreads) {
        threadListingView.setVisible(showThreads);
    }
}
