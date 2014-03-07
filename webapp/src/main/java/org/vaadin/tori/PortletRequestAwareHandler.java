package org.vaadin.tori;

import java.io.IOException;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

@SuppressWarnings("serial")
public class PortletRequestAwareHandler implements RequestHandler {

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
        final ToriApiLoader apiLoader = session
                .getAttribute(ToriApiLoader.class);
        if (apiLoader != null) {
            apiLoader.setRequest(request);
        }
        return false;
    }

}
