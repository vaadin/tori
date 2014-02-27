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

package org.vaadin.tori;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ServiceLoader;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.ToriActivityMessaging;
import org.vaadin.tori.util.UrlConverter;
import org.vaadin.tori.util.UserBadgeProvider;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;

@SuppressWarnings("serial")
public class ToriApiLoader implements Serializable {

    private final ServiceProvider spi;
    private final DataSource ds;
    private final PostFormatter postFormatter;
    private final AuthorizationService authorizationService;

    private final UserBadgeProvider userBadgeProvider;
    private final UrlConverter urlConverter;
    private final ToriActivityMessaging toriActivityMessaging;

    public ToriApiLoader() {
        checkThatCommonIsLoaded();
        spi = newServiceProvider();
        ds = createDataSource();
        postFormatter = createPostFormatter();
        authorizationService = createAuthorizationService();
        toriActivityMessaging = createToriActivityMessaging();
        userBadgeProvider = createService(UserBadgeProvider.class);
        urlConverter = createService(UrlConverter.class);
    }

    private <T> T createService(final Class<T> clazz) {
        T service = null;
        final ServiceLoader<T> loader = ServiceLoader.load(clazz);
        if (loader.iterator().hasNext()) {
            service = loader.iterator().next();
            getLogger()
                    .debug(String.format("Using %s implementation: %s",
                            clazz.getSimpleName(), service.getClass().getName()));
        } else {
            getLogger().debug(
                    String.format("No implementation for %s found",
                            clazz.getSimpleName()));
        }
        return service;
    }

    public final void setRequest(final Object request) {
        if (request != null) {
            for (final Object aware : Arrays.asList(ds, authorizationService,
                    toriActivityMessaging, postFormatter)) {
                if (aware instanceof PortletRequestAware
                        && request instanceof PortletRequest) {
                    ((PortletRequestAware) aware)
                            .setRequest((PortletRequest) request);
                } else if (aware instanceof HttpServletRequestAware
                        && request instanceof HttpServletRequest) {
                    ((HttpServletRequestAware) aware)
                            .setRequest((HttpServletRequest) request);
                }
            }
        }
    }

    /**
     * Verifies that the common project is in the classpath
     * 
     * @throws RuntimeException
     *             if Common is not in the classpath
     */
    private static void checkThatCommonIsLoaded() {
        try {
            Class.forName("org.vaadin.tori.data.spi.ServiceProvider");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException("Your project was "
                    + "apparently deployed without the Common "
                    + "project (common.jar) in its classpath", e);
        }
    }

    private static ServiceProvider newServiceProvider() {
        final ServiceLoader<ServiceProvider> loader = ServiceLoader
                .load(ServiceProvider.class);
        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            throw new RuntimeException(
                    "It seems you don't have a DataSource in your classpath, "
                            + "or the added data source is misconfigured (see JavaDoc for "
                            + ServiceProvider.class.getName() + ").");
        }
    }

    private DataSource createDataSource() {
        final DataSource ds = spi.createDataSource();
        getLogger().debug(
                String.format("Using %s implementation: %s", DataSource.class
                        .getSimpleName(), ds.getClass().getName()));
        return ds;
    }

    private PostFormatter createPostFormatter() {
        final PostFormatter postFormatter = spi.createPostFormatter();
        getLogger().debug(
                String.format("Using %s implementation: %s",
                        PostFormatter.class.getSimpleName(), postFormatter
                                .getClass().getName()));
        return postFormatter;
    }

    private AuthorizationService createAuthorizationService() {
        final AuthorizationService authorizationService = spi
                .createAuthorizationService();
        getLogger().debug(
                String.format("Using %s implementation: %s",
                        PostFormatter.class.getSimpleName(),
                        authorizationService.getClass().getName()));
        return authorizationService;
    }

    private ToriActivityMessaging createToriActivityMessaging() {
        final ToriActivityMessaging toriActivityMessaging = spi
                .createToriActivityMessaging();
        getLogger().debug(
                String.format("Using %s implementation: %s",
                        ToriActivityMessaging.class.getSimpleName(),
                        toriActivityMessaging.getClass().getName()));
        return toriActivityMessaging;
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriApiLoader.class);
    }

    public DataSource getDataSource() {
        return ds;
    }

    public PostFormatter getPostFormatter() {
        return postFormatter;
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public UserBadgeProvider getUserBadgeProvider() {
        return userBadgeProvider;
    }

    public UrlConverter getUrlConverter() {
        return urlConverter;
    }

    public ToriActivityMessaging getToriActivityMessaging() {
        return toriActivityMessaging;
    }

    public static ToriApiLoader getCurrent() {
        final ToriApiLoader apiLoader = VaadinSession.getCurrent()
                .getAttribute(ToriApiLoader.class);
        if (apiLoader != null) {
            return apiLoader;
        } else {
            throw new IllegalStateException(ToriApiLoader.class.getName()
                    + " was not found in the state. This is bad...");
        }
    }

    public static void init(final VaadinRequest request) {
        ToriApiLoader toriApiLoader = VaadinSession.getCurrent().getAttribute(
                ToriApiLoader.class);
        if (toriApiLoader == null) {
            toriApiLoader = new ToriApiLoader();
            VaadinSession.getCurrent().setAttribute(ToriApiLoader.class,
                    toriApiLoader);
        }
        toriApiLoader.setRequest(request);
    }
}
