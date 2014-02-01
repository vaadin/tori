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

package org.vaadin.tori.mvp;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.ToriActivityMessaging;

public abstract class Presenter<V extends View> {

    protected final Logger log = Logger.getLogger(getClass());
    protected final V view;

    protected DataSource dataSource;
    protected AuthorizationService authorizationService;
    protected PostFormatter postFormatter;
    protected ToriActivityMessaging messaging;

    public Presenter(final V view) {
        this.view = view;

        ToriApiLoader toriApiLoader = getApiLoader();
        dataSource = toriApiLoader.getDataSource();
        authorizationService = toriApiLoader.getAuthorizationService();
        postFormatter = toriApiLoader.getPostFormatter();
        messaging = toriApiLoader.getToriActivityMessaging();
    }

    protected ToriApiLoader getApiLoader() {
        return ToriApiLoader.getCurrent();
    }

    public void navigationFrom() {
        // NOP, subclasses may override
    }

    public void navigationTo(String[] arguments) {
        // NOP, subclasses may override
    }

}
