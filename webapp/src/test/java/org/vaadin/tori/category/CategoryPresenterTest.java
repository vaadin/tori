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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;

public class CategoryPresenterTest {

    private CategoryPresenter presenter;

    private CategoryView mockView;
    private DataSource mockDataSource;

    @Before
    public void setup() {
        // create mocks
        mockView = mock(CategoryView.class);
        mockDataSource = mock(DataSource.class);

        // create the presenter to test
        presenter = new CategoryPresenter(mockDataSource, null);
        presenter.setView(mockView);
    }

    @Test
    public void invalidCategoryIdFormat() {
        presenter.setCurrentCategoryById("qwerty");
        verify(mockView).displayCategoryNotFoundError("qwerty");
    }

    @Test
    public void nonExistingCategoryId() throws DataSourceException {
        when(mockDataSource.getCategory(-1)).thenReturn(null);

        presenter.setCurrentCategoryById("-1");
        verify(mockView).displayCategoryNotFoundError("-1");
    }

    @Test
    public void recentPostsCategory() throws DataSourceException {
        presenter.setCurrentCategoryById(SpecialCategory.RECENT_POSTS.getId());
        assertEquals(SpecialCategory.RECENT_POSTS.getInstance(),
                presenter.getCurrentCategory());
        assertFalse(presenter.userMayStartANewThread());

        verify(mockView).displayThreads(presenter.recentPostsProvider);
    }

    @Test
    public void myPostsCategory() throws DataSourceException {
        presenter.setCurrentCategoryById(SpecialCategory.MY_POSTS.getId());
        assertEquals(SpecialCategory.MY_POSTS.getInstance(),
                presenter.getCurrentCategory());
        assertFalse(presenter.userMayStartANewThread());

        verify(mockView).displayThreads(presenter.myPostsProvider);
    }

    @Test
    public void existingCategoryId() throws DataSourceException {
        final Category category = new Category();
        final List<Category> subCategories = Collections.emptyList();
        final List<org.vaadin.tori.data.entity.DiscussionThread> threads = Collections
                .emptyList();

        when(mockDataSource.getCategory(1)).thenReturn(category);
        when(mockDataSource.getSubCategories(category)).thenReturn(
                subCategories);
        when(mockDataSource.getThreads(category)).thenReturn(threads);

        // assert that that category is actually set
        presenter.setCurrentCategoryById("1");
        assertEquals(category, presenter.getCurrentCategory());

        // verify that the category (and subcategories) is displayed)
        verify(mockView).displaySubCategories(subCategories, false);
        /*-verify(mockView).displayThreads(threads);*/
    }

}
