package org.vaadin.tori;

import java.io.IOException;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.server.AbstractUIProvider;
import com.vaadin.server.CombinedRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletSession;
import com.vaadin.server.WrappedHttpServletRequest;
import com.vaadin.server.WrappedPortletRequest;
import com.vaadin.server.WrappedRequest;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

@SuppressWarnings("serial")
public class ToriServlet extends VaadinServlet {

    public class ToriUiProvider extends AbstractUIProvider {
        @Override
        public Class<? extends UI> getUIClass(final WrappedRequest request) {
            if (shouldRenderEditUI(request)) {
                return ToriEditUI.class;
            } else {
                return ToriUI.class;
            }
        }

        private boolean shouldRenderEditUI(final WrappedRequest request) {
            final PortletRequest portletRequest = getPortletRequest(request);
            return portletRequest != null
                    && portletRequest.getPortletMode() == PortletMode.EDIT;
        }

        private PortletRequest getPortletRequest(final WrappedRequest request) {
            if (request instanceof WrappedPortletRequest) {
                return ((WrappedPortletRequest) request).getPortletRequest();
            } else if (request instanceof CombinedRequest) {
                final WrappedRequest wrappedRequest = ((CombinedRequest) request)
                        .getSecondRequest();
                if (wrappedRequest instanceof WrappedPortletRequest) {
                    return ((WrappedPortletRequest) wrappedRequest)
                            .getPortletRequest();
                }
            }
            return null;
        }
    }

    @Override
    protected void service(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        super.service(request, response);
        final ToriUI ui = CurrentInstance.get(ToriUI.class);
        if (ui != null) {
            System.out.println("ToriServlet.service()");
            ui.setRequest(request);
        }
    }

    @Override
    protected void onVaadinSessionStarted(
            final WrappedHttpServletRequest request,
            final VaadinServletSession session) throws ServletException {
        session.addUIProvider(new ToriUiProvider());
        super.onVaadinSessionStarted(request, session);
    }

}
