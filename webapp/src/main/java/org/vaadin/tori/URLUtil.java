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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource.UrlInfo;
import org.vaadin.tori.data.DataSource.UrlInfo.Destination;
import org.vaadin.tori.util.UrlConverter;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class URLUtil {

    @SuppressWarnings("deprecation")
    public static void fixUrl() {
        final URI uri = Page.getCurrent().getLocation();
        final String path = getLocaleAdjustedURI(uri.getPath());
        final String pathRoot = ToriApiLoader.getCurrent().getDataSource()
                .getPathRoot();

        if (pathRoot == null) {
            getLogger().info(
                    "Tori's path root is undefined. You probably "
                            + "should set it. Skipping URL parsing.");
            return;
        }

        if (hasInvalidPath(path, pathRoot)) {
            getLogger().warn(
                    "Path mismatch: Tori is configured for the path "
                            + pathRoot + ", but current path is " + path
                            + ". Skipping URL parsing.");
            return;
        }

        final String pathPart;
        if (path.length() > pathRoot.length() && !path.equals(pathRoot + "/")) {
            pathPart = pathRoot;
        } else {
            pathPart = null;
        }

        String fragment = null;
        final String relativePath = path.substring(pathRoot.length());
        try {
            final String query = getQueryString(uri);
            UrlInfo toriFragment = null;

            UrlConverter uc = ToriApiLoader.getCurrent().getUrlConverter();
            if (uc != null) {
                try {
                    toriFragment = uc.getToriFragment(relativePath, query);
                } catch (final Exception e) {
                    toriFragment = new UrlInfo() {
                        @Override
                        public Destination getDestination() {
                            return Destination.DASHBOARD;
                        }

                        @Override
                        public long getId() {
                            return -1;
                        }
                    };
                    Notification
                            .show("The given URL doesn't match to any content on this forum",
                                    "The content probably has been taken down since.",
                                    Type.WARNING_MESSAGE);
                }
            }

            if (toriFragment != null) {
                fragment = "";

                if (toriFragment.getDestination() == Destination.DASHBOARD) {
                    fragment = ToriNavigator.ApplicationView.DASHBOARD.getUrl()
                            + "/";
                } else {
                    switch (toriFragment.getDestination()) {
                    case CATEGORY:
                        fragment = ToriNavigator.ApplicationView.CATEGORIES
                                .getUrl();
                        break;
                    case THREAD:
                        fragment = ToriNavigator.ApplicationView.THREADS
                                .getUrl();
                        break;
                    default:
                        Notification.show("You found a URL the coders "
                                + "didn't remember to handle correctly. "
                                + "Terribly sorry about that.",
                                Notification.Type.HUMANIZED_MESSAGE);
                        throw new UnsupportedOperationException("Support for "
                                + toriFragment.getDestination()
                                + " is not implemented");
                    }

                    fragment += "/" + toriFragment.getId();
                }
            }
        } catch (final UnsupportedEncodingException e) {
            getLogger().error("URI is not in UTF-8 format");
        }

        if (pathPart != null && fragment != null) {
            Page.getCurrent().setLocation(pathPart + "#" + fragment);
        } else if (pathPart != null) {
            Page.getCurrent().setLocation(pathPart);
        } else if (fragment != null) {
            Page.getCurrent().setUriFragment(fragment);
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(URLUtil.class);
    }

    /**
     * <p>
     * Remove the Locale setting parameter in the Liferay URI.
     * <p>
     * Take an URL <code>vaadin.com/foo</code>. Liferay accepts an url
     * <code>vaadin.com/en_GB/foo</code>, and produces the same page. Some
     * Liferay features are able to take use of the locale definition in the
     * URL, and translate things. Since Tori doesn't support that currently, we
     * need to ignore that bit in the URI.
     */
    private static String getLocaleAdjustedURI(final String path) {

        // remove the prefixes locale string from the input -->
        // /fi/foo/bar -> /foo/bar
        // /en_GB/foo/bar -> /foo/bar

        final Pattern pathShortener = Pattern
                .compile("^/(?:[a-z]{2}(?:_[A-Z]{2})?/)?(.+)$");
        /*-
         * ^/              # the string needs to start with a forward slash
         * (
         *   ?:[a-z]{2}    # non-capturing group that matches two lower case letters
         *   (
         *     ?:_[A-Z]{2} # non-capturing group that, if the previous was matched, this matches the following underscore, and two upper case letters
         *   )?            # the previous group is optional (so, it's fine to match the lower case letters only
         *   /             # if the two lower case letters were found, no matter if the second group is found, a forward slash is required
         * )?              # the entire group is optional
         * (.+)$           # capture the string that comes after these groups.
         */

        final Matcher matcher = pathShortener.matcher(path);
        if (matcher.matches()) {
            return "/" + matcher.group(1);
        } else {
            getLogger().warn("Path appears weird: " + path);
            return path;
        }
    }

    /**
     * Get the decoded query part ("GET parameters") of the URI. Returns empty
     * string if <code>null</code>
     */
    private static String getQueryString(final URI uri)
            throws UnsupportedEncodingException {
        final String query;
        if (uri.getQuery() != null) {
            query = URLDecoder.decode(uri.getQuery(), "UTF-8");
        } else {
            query = "";
        }
        return query;
    }

    private static boolean hasInvalidPath(final String path,
            final String pathRoot) {
        final boolean pathStartsWithRoot = path.startsWith(pathRoot);

        // to avoid pathroot being "/foo" and path being "/foobar"
        final boolean pathIsCorrectFormat;
        if (path.length() > pathRoot.length()) {
            pathIsCorrectFormat = path.startsWith(pathRoot + "/");
        } else {
            pathIsCorrectFormat = true;
        }

        return !(pathStartsWithRoot && pathIsCorrectFormat);
    }
}
