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

import com.vaadin.server.Constants;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletRequest;
import com.vaadin.server.VaadinPortletService;

public class ToriPortlet extends VaadinPortlet {
    private static final long serialVersionUID = 1394935675720995291L;

    private static final String PORTAL_UTIL_CLASS = "com.liferay.portal.util.PortalUtil";

    private class ToriPortletService extends VaadinPortletService {
        private static final long serialVersionUID = -2117466377821216962L;

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
    @SuppressWarnings("serial")
    protected VaadinPortletRequest createVaadinRequest(
            final PortletRequest request) {

        /*
         * This method seems to be responsible for redirecting widgetset and
         * theme fetching from the deployed servlet instead of the portlet
         * environment.
         * 
         * This allows us to package the widgetset and theme inside the war,
         * without requiring the end-user to modify the portal environment at
         * all.
         */

        VaadinPortletRequest wrapped = super.createVaadinRequest(request);

        final String portalInfo = request.getPortalContext().getPortalInfo()
                .toLowerCase();
        if (portalInfo.contains("liferay")) {
            wrapped = new VaadinLiferayRequest(request, wrapped.getService()) {
                @Override
                public String getPortalProperty(final String name) {
                    // catch the query of where vaadin resources are located at.
                    if (Constants.PORTAL_PARAMETER_VAADIN_RESOURCE_PATH
                            .equals(name)) {
                        return request.getContextPath();
                    } else {
                        return super.getPortalProperty(name);
                    }
                }
            };
        }
        return wrapped;
    }

    @Override
    protected void portletInitialized() {
        getService()
                .setSystemMessagesProvider(ToriSystemMessagesProvider.get());
    }
}
