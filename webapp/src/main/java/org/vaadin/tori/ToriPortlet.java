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

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;

public class ToriPortlet extends ApplicationPortlet2 {

    private static final String USER_AGENT = "User-Agent";
    private static final String INDEXER_USER_AGENT_FRAGMENT = "Firefox";

    @Override
    protected void handleRequest(final PortletRequest request,
            final PortletResponse response) throws PortletException,
            IOException {
        final HttpServletRequest servletRequest = getServletRequest(request);
        if (servletRequest != null && request instanceof RenderRequest
                && isIndexerBot(servletRequest)) {

            printPage(servletRequest, response);
        } else {
            super.handleRequest(request, response);
        }
    }

    private boolean isIndexerBot(final HttpServletRequest servletRequest) {
        return servletRequest.getHeader(USER_AGENT).contains(
                INDEXER_USER_AGENT_FRAGMENT);
    }

    private void printPage(final HttpServletRequest servletRequest,
            final PortletResponse response) throws IOException {
        final RenderResponse renderResponse = (RenderResponse) response;
        renderResponse.setContentType("text/html");
        final OutputStream out = renderResponse.getPortletOutputStream();
        final PrintWriter outWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(out, "UTF-8")));
        outWriter.print("<html><body>dummy page</body></html>");
        outWriter.close();
    }

    private static HttpServletRequest getServletRequest(
            final PortletRequest request) {
        try {
            final Class<?> portalUtil = Class
                    .forName("com.liferay.portal.util.PortalUtil");
            final Method method = portalUtil.getMethod("getHttpServletRequest",
                    PortletRequest.class);
            return (HttpServletRequest) method.invoke(null, request);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
