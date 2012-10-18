package org.vaadin.tori;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;

import com.vaadin.server.CombinedRequest;
import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinPortletRequest;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServiceSession;
import com.vaadin.ui.UI;

public class ToriUiProvider extends DefaultUIProvider {

    private final String themeName;

    public ToriUiProvider(final String themeName) {
        this.themeName = themeName;
    }

    @Override
    public Class<? extends UI> getUIClass(final UIClassSelectionEvent event) {
        return ToriUI.class;
    }

    @Override
    public UI createInstance(final UICreateEvent event) {
        final UI ui = super.createInstance(event);
        ui.setSession(VaadinServiceSession.getCurrent());
        if (ui instanceof ToriUI) {
            final ToriUI toriUi = (ToriUI) ui;
            toriUi.initApiLoader(event.getRequest());
            if (shouldRenderEditUI(event.getRequest())) {
                toriUi.setPortletMode(PortletMode.EDIT);
            } else {
                toriUi.setPortletMode(PortletMode.VIEW);
            }
        } else {
            Logger.getLogger(getClass()).warn(
                    "Created UI is not an instance of "
                            + ToriUI.class.getName() + " but "
                            + ui.getClass().getName()
                            + ". Might cause problems.");
        }
        return ui;
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

    @Override
    public String getTheme(final UICreateEvent event) {
        return themeName;
    }
}