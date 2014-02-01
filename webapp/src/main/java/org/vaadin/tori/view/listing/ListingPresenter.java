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

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchCategoryException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.view.listing.category.CategoryListingPresenter;
import org.vaadin.tori.view.listing.thread.ThreadListingPresenter;

public class ListingPresenter extends Presenter<ListingView> {

    private CategoryListingPresenter categoryListingPresenter;

    private ThreadListingPresenter threadListingPresenter;

    public ListingPresenter(ListingView view) {
        super(view);
    }

    public void setCategoryListingPresenter(
            CategoryListingPresenter categoryListingPresenter) {
        this.categoryListingPresenter = categoryListingPresenter;
    }

    public void setThreadListingPresenter(
            ThreadListingPresenter threadListingPresenter) {
        this.threadListingPresenter = threadListingPresenter;
    }

    @Override
    public void navigationTo(String[] arguments) {
        String categoryIdString = arguments[0];
        Category category = null;
        if (!categoryIdString.isEmpty()) {
            if (categoryIdString.equals(SpecialCategory.RECENT_POSTS.getId())) {
                category = SpecialCategory.RECENT_POSTS.getInstance();
            } else if (categoryIdString
                    .equals(SpecialCategory.MY_POSTS.getId())) {
                category = SpecialCategory.MY_POSTS.getInstance();
            } else {
                final long categoryId = Long.valueOf(categoryIdString);
                try {
                    category = dataSource.getCategory(categoryId);

                } catch (final NumberFormatException e) {
                    log.error("Invalid category id format: " + categoryId);
                } catch (final NoSuchCategoryException e) {
                    view.displayCategoryNotFoundError(String.valueOf(e
                            .getCategoryId()));
                } catch (final DataSourceException e) {
                    e.printStackTrace();
                    view.panic();
                }
            }
        }
        view.setCategory(category);

        categoryListingPresenter.categorySelected(category);
        threadListingPresenter.categorySelected(category);

    };

}
