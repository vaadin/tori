/*
 * Copyright 2013 Vaadin Ltd.
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

package org.vaadin.tori;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;

public class TestUtil extends ToriApiLoader {

    public static ToriApiLoader mockApiLoader() {
        ToriApiLoader apiLoader = mock(ToriApiLoader.class);
        DataSource dataSource = mock(DataSource.class);
        AuthorizationService authorizationService = mock(AuthorizationService.class);

        when(apiLoader.getDataSource()).thenReturn(dataSource);
        when(apiLoader.getAuthorizationService()).thenReturn(
                authorizationService);
        return apiLoader;
    }
}
