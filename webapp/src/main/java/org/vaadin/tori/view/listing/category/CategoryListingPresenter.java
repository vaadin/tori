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

package org.vaadin.tori.view.listing.category;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.listing.SpecialCategory;
import org.vaadin.tori.view.listing.category.CategoryListingView.CategoryData;

public class CategoryListingPresenter extends Presenter<CategoryListingView> {

    private Long parentCategoryId;

    public CategoryListingPresenter(final CategoryListingView view) {
        super(view);

    }

    public void saveNewCategory(final String name, final String description) {
        if (name != null && !name.trim().isEmpty()) {
            try {
                dataSource.saveNewCategory(parentCategoryId, name, description);
            } catch (final DataSourceException e) {
                displayError(e);
            }
        } else {
            view.showError("Name required");
        }
        // refresh the categories
        updateView();
    }

    public void deleteCategory(final long categoryId) {
        try {
            dataSource.deleteCategory(categoryId);
        } catch (final DataSourceException e) {
            displayError(e);
        }

        // FIXME: This is here temporarily and only to avoid a Vaadin bug which
        // caused the confirm window not to close after deleting a category.
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // refresh the categories
                updateView();
            }
        });

    }

    public void updateCategory(final long categoryId, final String name,
            final String description) {
        try {
            dataSource.updateCategory(categoryId, name, description);
        } catch (final DataSourceException e) {
            displayError(e);
            // refresh the categories
            updateView();
        }
    }

    private void updateView() {
        view.setCategories(getCategorySubCategories(parentCategoryId));
        view.setMayCreateCategories(authorizationService.mayEditCategories());
    }

    public void categorySelected(final Category category) {
        if (!SpecialCategory.isSpecialCategory(category)) {
            this.parentCategoryId = category != null ? category.getId() : null;
            updateView();
        }
    }

    private List<CategoryData> getCategorySubCategories(final Long categoryId) {
        List<CategoryData> result = new ArrayList<CategoryData>();
        try {
            for (Category subCategory : dataSource.getSubCategories(categoryId)) {
                if (authorizationService.mayViewCategory(subCategory.getId())) {
                    result.add(getCategoryData(subCategory));
                }
            }
        } catch (DataSourceException e) {
            displayError(e);
        }
        return result;
    }

    private void displayError(final DataSourceException e) {
        log.error(e);
        e.printStackTrace();
        view.showError(DataSourceException.GENERIC_ERROR_MESSAGE);
    }

    private CategoryData getCategoryData(final Category category) {
        final long categoryId = category.getId();
        return new CategoryData() {

            @Override
            public long getId() {
                return categoryId;
            }

            @Override
            public boolean mayEditCategory() {
                return authorizationService.mayEditCategory(categoryId);
            }

            @Override
            public boolean mayDeleteCategory() {
                return authorizationService.mayDeleteCategory(categoryId);
            }

            @Override
            public int getThreadCount() {
                int result = 0;
                try {
                    result = dataSource.getThreadCountRecursively(categoryId);
                } catch (final DataSourceException e) {
                    displayError(e);
                }
                return result;
            }

            @Override
            public int getUnreadThreadCount() {
                int result = 0;
                try {
                    result = dataSource.getUnreadThreadCount(categoryId);
                } catch (final DataSourceException e) {
                    displayError(e);
                }
                return result;
            }

            @Override
            public String getName() {
                return category.getName();
            }

            @Override
            public String getDescription() {
                return category.getDescription();
            }

            @Override
            public List<CategoryData> getSubCategories() {
                return getCategorySubCategories(categoryId);
            }

            @Override
            public void setName(final String name) {
                category.setName(name);
            }

            @Override
            public void setDescription(final String description) {
                category.setDescription(description);
            }
        };
    }

}
