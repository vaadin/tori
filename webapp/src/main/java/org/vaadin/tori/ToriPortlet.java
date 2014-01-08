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

package org.vaadin.tori;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.vaadin.tori.indexing.ToriIndexableApplication;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;

@SuppressWarnings("serial")
public class ToriPortlet extends VaadinPortlet {

    private static final String PORTAL_UTIL_CLASS = "com.liferay.portal.util.PortalUtil";
    private static final String DEFAULT_THEME_NAME = "tori-liferay";

    private class ToriPortletService extends VaadinPortletService {

        public ToriPortletService(final VaadinPortlet portlet,
                final DeploymentConfiguration config) throws ServiceException {
            super(portlet, config);
        }

        @Override
        public boolean preserveUIOnRefresh(final UIProvider provider,
                final UICreateEvent event) {
            return false;
        }

        @Override
        public String getConfiguredTheme(VaadinRequest request) {
            String theme = getInitParameter("theme");
            return theme != null ? theme : DEFAULT_THEME_NAME;
        }

        @Override
        public String getStaticFileLocation(VaadinRequest request) {
            return request.getContextPath();
        }

        @Override
        protected List<RequestHandler> createRequestHandlers()
                throws ServiceException {
            final List<RequestHandler> requestHandlers = super
                    .createRequestHandlers();
            requestHandlers.add(new UnsupportedDeviceHandler());
            requestHandlers.add(new PortletRequestAwareHandler());
            return requestHandlers;
        }
    }

    @Override
    protected void handleRequest(final PortletRequest request,
            final PortletResponse response) throws PortletException,
            IOException {
        final HttpServletRequest servletRequest = getServletRequest(request);
        if (servletRequest != null && request instanceof RenderRequest
                && ToriIndexableApplication.isIndexerBot(servletRequest)
                && ToriIndexableApplication.isIndexableRequest(servletRequest)) {

            final ToriIndexableApplication app = new ToriIndexableApplication(
                    request);
            final String htmlPage = app.getResultInHtml(servletRequest);

            final RenderResponse renderResponse = (RenderResponse) response;
            renderResponse.setContentType("text/html");
            final OutputStream out = renderResponse.getPortletOutputStream();
            final PrintWriter outWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(out, "UTF-8")));
            outWriter.print(htmlPage);
            outWriter.close();
        } else {
            super.handleRequest(request, response);
        }
    }

    @Override
    protected VaadinPortletService createPortletService(
            final DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        final ToriPortletService toriPortletService = new ToriPortletService(
                this, deploymentConfiguration);
        toriPortletService.init();
        return toriPortletService;
    }

    private static HttpServletRequest getServletRequest(
            final PortletRequest request) {
        try {
            final Class<?> portalUtilClass = Class.forName(PORTAL_UTIL_CLASS);

            /* first get the given servletrequest */
            final HttpServletRequest httpServletRequest = (HttpServletRequest) portalUtilClass
                    .getMethod("getHttpServletRequest", PortletRequest.class)
                    .invoke(null, request);

            /*
             * but since that's some kind of a fake request, we need the
             * original one.
             */
            final HttpServletRequest originalHttpServletRequest = (HttpServletRequest) portalUtilClass
                    .getMethod("getOriginalServletRequest",
                            HttpServletRequest.class).invoke(null,
                            httpServletRequest);

            return originalHttpServletRequest;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void portletInitialized() {
        getService()
                .setSystemMessagesProvider(ToriSystemMessagesProvider.get());
    }
}
