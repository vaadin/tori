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

package org.vaadin.tori.view.listing.category;

import java.util.List;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.view.listing.category.EditCategoryForm.EditCategoryListener;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

/**
 * UI component for displaying a vertical hierarchical list of categories.
 */
@SuppressWarnings("serial")
public class CategoryListingViewImpl extends
        AbstractView<CategoryListingView, CategoryListingPresenter> implements
        CategoryListingView {

    private CategoryTreeTable categoryTree;
    private VerticalLayout layout;
    private PopupButton createCategoryButton;
    private Label noCategoriesLabel;

    @Override
    protected Component createCompositionRoot() {
        layout = new VerticalLayout();
        return layout;
    }

    @Override
    public void initView() {
        setStyleName("categorylistingview");
        setVisible(false);
        layout.addComponent(buildCategoryHeader());
        layout.addComponent(buildCategoryListing());
    }

    private Component buildCategoryListing() {
        categoryTree = new CategoryTreeTable(getPresenter());
        return categoryTree;
    }

    private Component buildCategoryHeader() {
        HorizontalLayout result = ComponentUtil.getHeaderLayout("Categories");
        noCategoriesLabel = new Label("No categories");
        noCategoriesLabel.setVisible(false);
        noCategoriesLabel.setSizeUndefined();
        result.addComponent(noCategoriesLabel);
        result.setComponentAlignment(noCategoriesLabel, Alignment.MIDDLE_CENTER);

        createCategoryButton = new PopupButton("New Category");
        createCategoryButton.setContent(new EditCategoryForm(
                new EditCategoryListener() {
                    @Override
                    public void cancel() {
                        createCategoryButton.setPopupVisible(false);
                    }

                    @Override
                    public void commit(final String name,
                            final String description) {
                        getPresenter().saveNewCategory(name, description);
                        createCategoryButton.setPopupVisible(false);
                    }
                }));
        Component buttonWrapper = new HorizontalLayout(createCategoryButton);
        result.addComponent(buttonWrapper);
        result.setComponentAlignment(buttonWrapper, Alignment.MIDDLE_RIGHT);
        return result;
    }

    @Override
    protected CategoryListingPresenter createPresenter() {
        return new CategoryListingPresenter(this);
    }

    @Override
    public void showError(final String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void setCategories(final List<CategoryData> categories) {
        boolean hasCategories = !categories.isEmpty();
        setVisible(true);
        noCategoriesLabel.setVisible(!hasCategories);
        categoryTree.setVisible(hasCategories);
        categoryTree.setCategories(categories);
    }

    @Override
    public void setMayCreateCategories(final boolean mayEditCategories) {
        createCategoryButton.setVisible(mayEditCategories);
    }
}
