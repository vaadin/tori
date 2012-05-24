package org.vaadin.tori;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.vaadin.tori.indexing.ToriIndexableApplication;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;

public class ToriPortlet extends ApplicationPortlet2 {

    private static final String PORTAL_UTIL_CLASS = "com.liferay.portal.util.PortalUtil";

    @Override
    protected void handleRequest(final PortletRequest request,
            final PortletResponse response) throws PortletException,
            IOException {
        final HttpServletRequest servletRequest = getServletRequest(request);
        final HttpServletResponse servletResponse = getServletResponse(response);
        if (servletRequest != null && request instanceof RenderRequest
                && ToriIndexableApplication.isIndexerBot(servletRequest)
                && ToriIndexableApplication.isIndexableRequest(servletRequest)) {

            final String xhtmlPage = ToriIndexableApplication
                    .getResultInXhtml(servletRequest);

            final RenderResponse renderResponse = (RenderResponse) response;
            renderResponse.setContentType("text/html");
            final OutputStream out = renderResponse.getPortletOutputStream();
            final PrintWriter outWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(out, "UTF-8")));
            outWriter.print(xhtmlPage);
            outWriter.close();
        } else {
            super.handleRequest(request, response);
        }
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

    private static HttpServletResponse getServletResponse(
            final PortletResponse response) {
        try {
            final Class<?> portalUtil = Class.forName(PORTAL_UTIL_CLASS);
            final Method method = portalUtil.getMethod(
                    "getHttpServletResponse", PortletResponse.class);
            return (HttpServletResponse) method.invoke(null, response);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
