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

package org.vaadin.tori.data.spi;

import java.util.ServiceLoader;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.ToriActivityMessaging;
import org.vaadin.tori.util.ToriMailService;

/**
 * This interface needs to be implemented for the datasource project included in
 * Tori's WAR. Otherwise, errors will ensue upon launch. Each datasource
 * provider must place a configuration file in {@code META-INF/services}
 * directory as defined in the documentation of {@link ServiceLoader} class.
 * 
 * <br />
 * <br />
 * All factory methods in this interface are called only once per Application
 * instance (i.e. session) and the instance is reused by that instance. This
 * means that the returned service implementations are allowed to be stateful.
 * 
 * @see ServiceLoader
 */
public interface ServiceProvider {

    String IMPLEMENTING_CLASSNAME = "org.vaadin.tori.data.ServiceProviderImpl";

    /**
     * Returns a new {@link DataSource} instance.
     */
    DataSource createDataSource();

    /**
     * Returns a new {@link PostFormatter} instance.
     */
    PostFormatter createPostFormatter();

    /**
     * Returns a new {@link AuthorizationService} instance.
     */
    AuthorizationService createAuthorizationService();

    /**
     * Returns a new {@link ToriActivityMessaging} instance.
     */
    ToriActivityMessaging createToriActivityMessaging();

    /**
     * Returns a new {@link ToriMailService} instance.
     */
    ToriMailService createToriMailService();

}
