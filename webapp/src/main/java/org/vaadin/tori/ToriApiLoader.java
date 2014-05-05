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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ServiceLoader;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.spi.ServiceProvider;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.util.PostFormatter;
import org.vaadin.tori.util.ToriActivityMessaging;
import org.vaadin.tori.util.ToriMailService;
import org.vaadin.tori.util.UrlConverter;
import org.vaadin.tori.util.UserBadgeProvider;

import com.vaadin.server.Page;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ToriApiLoader implements Serializable, SessionDestroyListener {

    private final ServiceProvider spi;
    private final DataSource ds;
    private final PostFormatter postFormatter;
    private final AuthorizationService authorizationService;

    private final UserBadgeProvider userBadgeProvider;
    private final UrlConverter urlConverter;
    private final ToriActivityMessaging toriActivityMessaging;
    private ToriMailService toriMailService;
    private String sessionId;

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
                    toriActivityMessaging, postFormatter, toriMailService)) {
                try {
                    if (aware instanceof PortletRequestAware
                            && request instanceof PortletRequest) {
                        ((PortletRequestAware) aware)
                                .setRequest((PortletRequest) request);
                    } else if (aware instanceof HttpServletRequestAware
                            && request instanceof HttpServletRequest) {
                        ((HttpServletRequestAware) aware)
                                .setRequest((HttpServletRequest) request);
                    }
                } catch (Exception e) {
                    getLogger().warn("Unable to set request", e);
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

    private static String getToriThemeImagesURL(final VaadinRequest request)
            throws MalformedURLException {
        URL url = Page.getCurrent().getLocation().toURL();
        int port = url.getPort();

        if (url.getProtocol().equals("http") && port == 80) {
            port = -1;
        } else if (url.getProtocol().equals("https") && port == 443) {
            port = -1;
        }

        URL serverURL = new URL(url.getProtocol(), url.getHost(), port, "");
        String contextPath = request.getContextPath();
        String imagesPath = "/VAADIN/themes/tori/images/";

        return serverURL + contextPath + imagesPath;

    }

    private ToriMailService createToriMailService(final VaadinRequest request) {
        ToriMailService result = null;
        if (request != null) {
            result = spi.createToriMailService();
            try {
                String themeName = UI.getCurrent().getTheme();

                InputStream postTemplateStream = VaadinService.getCurrent()
                        .getThemeResourceAsStream(UI.getCurrent(), "tori",
                                "toripostmailtemplate.xhtml");
                InputStream themeStream = VaadinService.getCurrent()
                        .getThemeResourceAsStream(UI.getCurrent(), themeName,
                                "styles.css");

                if (postTemplateStream != null && themeStream != null) {
                    result.setPostMailTemplate(readStream(postTemplateStream));

                    String themeCss = readStream(themeStream);
                    String imagesUrl = getToriThemeImagesURL(request);
                    String quoteImageUrl = imagesUrl + "emailquote.png";
                    String anonymousImageUrl = imagesUrl + "emailanonymous.png";
                    String defaultHeaderImageUrl = imagesUrl + "tori-icon.png";

                    //@formatter:off
                    String quoteRule = "\n\n"
                        + ".v-app blockquote cite, .v-app .quote-title { \n"
                                + "background-image: url('" + quoteImageUrl + "'); \n"
                                + "background-repeat: no-repeat; \n"
                        + "}";

                    String anonymousRule = "\n\n"
                        + ".avatar.anonymous-true { \n"
                                + "background-image: url('" + anonymousImageUrl + "'); \n"
                                + "height: 100%; \n"
                        + "}"
                        + "\n\n"
                        + ".avatar.anonymous-true img { \n"
                                + "display: none; \n"
                        + "}";

                    String customHeaderImageRule = "\n\n"
                            + ".defaultheaderimage-true { \n"
                                    + "background-image: url('" + defaultHeaderImageUrl + "'); \n"
                                    + "background-repeat: no-repeat; \n"
                            + "}"
                            + "\n\n"
                            + ".defaultheaderimage-true .headerimageplaceholder { \n"
                                    + "width: 160px; \n"
                                    + "height: 40px; \n"
                            + "}";
                    //@formatter:on
                    result.setMailTheme(themeCss + quoteRule + anonymousRule
                            + customHeaderImageRule);

                    getLogger().debug(
                            String.format("Using %s implementation: %s",
                                    ToriMailService.class.getSimpleName(),
                                    result.getClass().getName()));
                } else {
                    getLogger().error("Unable to set mail service resources");
                    result = null;
                }
            } catch (IOException e) {
                getLogger().warn("Exception while closing input stream", e);
            } catch (Exception e) {
                getLogger().error("Exception while initiating ToriMailService",
                        e);
                result = null;
            }
        }

        return result;
    }

    private static String readStream(final InputStream is) throws IOException {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        is.close();
        return result;
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

    public ToriMailService getToriMailService() {
        return toriMailService;
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
            toriApiLoader.toriMailService = toriApiLoader
                    .createToriMailService(request);
            if (toriApiLoader.getToriActivityMessaging() != null) {
                toriApiLoader.getToriActivityMessaging().register();
            }
            request.getService().addSessionDestroyListener(toriApiLoader);
            VaadinSession.getCurrent().setAttribute(ToriApiLoader.class,
                    toriApiLoader);
        }
        toriApiLoader.sessionId = VaadinSession.getCurrent().getSession()
                .getId();
        toriApiLoader.setRequest(request);
    }

    @Override
    public void sessionDestroy(final SessionDestroyEvent event) {
        if (sessionId != null
                && sessionId.equals(event.getSession().getSession().getId())) {
            if (toriActivityMessaging != null) {
                toriActivityMessaging.deregister();
            }
            event.getService().removeSessionDestroyListener(this);
        }
    }

}
