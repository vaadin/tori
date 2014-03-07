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

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.vaadin.tori.TestUtil;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.view.listing.category.CategoryListingPresenter;
import org.vaadin.tori.view.listing.category.CategoryListingView;

public class TestCategoryListing {

    private CategoryListingPresenter presenter;

    private CategoryListingView mockView;
    private DataSource mockDataSource;
    private AuthorizationService mockAuthorizationService;

    @Before
    public void setup() {
        // create mocks
        mockView = mock(CategoryListingView.class);

        final ToriApiLoader apiLoader = TestUtil.mockApiLoader();
        mockDataSource = apiLoader.getDataSource();
        mockAuthorizationService = apiLoader.getAuthorizationService();
        // create the presenter to test
        presenter = new CategoryListingPresenter(mockView) {
            @Override
            protected ToriApiLoader getApiLoader() {
                return apiLoader;
            }
        };
    }

}
