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

package org.vaadin.tori.component.category;

import java.util.List;
import java.util.Set;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.View;

interface CategoryListingView extends View {

    /**
     * Displays or hides the controls for rearranging categories.
     * 
     * @param visible
     */
    void setRearrangeVisible(boolean visible);

    /**
     * Displays or hides the controls for creating new categories.
     * 
     * @param visible
     */
    void setCreateVisible(boolean visible);

    /**
     * Displays all given categories in the hierarchical CategoryListing.
     * 
     * @param categories
     *            {@link Category Categories} to display.
     */
    void displayCategories(List<Category> categories);

    /**
     * Returns a {@link Set} of modified {@link Category Categories}.
     * 
     * @return a {@link Set} of modified {@link Category Categories}.
     */
    Set<Category> getModifiedCategories();

    /**
     * Hides the controls used for creating a new {@link Category}.
     */
    void hideCreateCategoryForm();

}
