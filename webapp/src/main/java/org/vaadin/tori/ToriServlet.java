package org.vaadin.tori;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSessionInitializationListener;
import com.vaadin.server.VaadinSessionInitializeEvent;
import com.vaadin.util.CurrentInstance;

@SuppressWarnings("serial")
public class ToriServlet extends VaadinServlet {

    @Override
    protected void service(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        super.service(request, response);
        final ToriUI ui = CurrentInstance.get(ToriUI.class);

        /*
         * Is this still needed? We have similar logic already in
         * ToriUI.initApiLoader()
         */
        if (ui != null) {
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
                        String theme = getInitParameter("theme");
                        theme = (theme != null) ? theme : "tori";

                        event.getVaadinSession().addUIProvider(
                                new ToriUiProvider(theme));
                    }
                });
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriServlet.class);
    }
}
