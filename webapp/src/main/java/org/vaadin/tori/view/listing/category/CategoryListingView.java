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

import java.util.List;

import org.vaadin.tori.mvp.View;

public interface CategoryListingView extends View {

    void showError(String message);

    void setCategories(List<CategoryData> categories);

    void setMayCreateCategories(boolean mayEditCategories);

    public interface CategoryData {

        long getId();

        boolean mayEditCategory();

        boolean mayDeleteCategory();

        int getUnreadThreadCount();

        int getThreadCount();

        String getName();

        String getDescription();

        List<CategoryData> getSubCategories();

        void setName(String name);

        void setDescription(String description);

    }

}
