package org.vaadin.tori;

import java.io.IOException;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.server.AbstractUIProvider;
import com.vaadin.server.CombinedRequest;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinPortletRequest;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSessionInitializationListener;
import com.vaadin.server.VaadinSessionInitializeEvent;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

@SuppressWarnings("serial")
public class ToriServlet extends VaadinServlet {

    public class ToriUiProvider extends AbstractUIProvider {
        @Override
        public Class<? extends UI> getUIClass(final VaadinRequest request) {
            if (shouldRenderEditUI(request)) {
                return ToriEditUI.class;
            } else {
                return ToriUI.class;
            }
        }

        private boolean shouldRenderEditUI(final VaadinRequest request) {
            final PortletRequest portletRequest = getPortletRequest(request);
            return portletRequest != null
                    && portletRequest.getPortletMode() == PortletMode.EDIT;
        }

        private PortletRequest getPortletRequest(final VaadinRequest request) {
            if (request instanceof VaadinPortletRequest) {
                return ((VaadinPortletRequest) request).getPortletRequest();
            } else if (request instanceof CombinedRequest) {
                final VaadinRequest VaadinRequest = ((CombinedRequest) request)
                        .getSecondRequest();
                if (VaadinRequest instanceof VaadinPortletRequest) {
                    return ((VaadinPortletRequest) VaadinRequest)
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
    protected void servletInitialized() {
        getVaadinService().addVaadinSessionInitializationListener(
                new VaadinSessionInitializationListener() {
                    @Override
                    public void vaadinSessionInitialized(
                            final VaadinSessionInitializeEvent event)
                            throws ServiceException {
                        event.getVaadinSession().addUIProvider(
                                new ToriUiProvider());
                    }
                });
    }
}
