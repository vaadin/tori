package org.vaadin.tori;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.log4j.Logger;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinPortletSession.PortletListener;
import com.vaadin.server.VaadinServiceSession;
import com.vaadin.ui.UI;

public class ToriUiProvider extends DefaultUIProvider {

    private final String themeName;

    @SuppressWarnings("deprecation")
    class PortletModeListener implements PortletListener {
        private static final long serialVersionUID = -2798198335357491405L;
        private final ToriUI toriUi;
        private PortletMode previousPortletMode;

        public PortletModeListener(final ToriUI toriUi) {
            this.toriUi = toriUi;
        }

        @Override
        public void handleResourceRequest(final ResourceRequest request,
                final ResourceResponse response, final UI uI) {
        }

        @Override
        public void handleRenderRequest(final RenderRequest request,
                final RenderResponse response, final UI uI) {
            final PortletMode newPortletMode = request.getPortletMode();
            if (!newPortletMode.equals(previousPortletMode)) {
                toriUi.setPortletMode(newPortletMode);
                previousPortletMode = newPortletMode;
            }
        }

        @Override
        public void handleEventRequest(final EventRequest request,
                final EventResponse response, final UI uI) {
        }

        @Override
        public void handleActionRequest(final ActionRequest request,
                final ActionResponse response, final UI uI) {
        }
    };

    public ToriUiProvider(final String themeName) {
        this.themeName = themeName;
    }

    @Override
    public Class<? extends UI> getUIClass(final UIClassSelectionEvent event) {
        return ToriUI.class;
    }

    @SuppressWarnings("deprecation")
    @Override
    public UI createInstance(final UICreateEvent event) {

        final UI ui = super.createInstance(event);

        ui.setSession(VaadinServiceSession.getCurrent());
        if (ui instanceof ToriUI) {
            final ToriUI toriUi = (ToriUI) ui;
            toriUi.initApiLoader(event.getRequest());
            final VaadinServiceSession session = VaadinServiceSession
                    .getCurrent();
            if (session instanceof VaadinPortletSession) {
                final VaadinPortletSession portletSession = (VaadinPortletSession) session;
                portletSession.addPortletListener(new PortletModeListener(
                        toriUi));

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

    /*-
    private boolean shouldRenderEditUI(final VaadinRequest request) {
        final PortletRequest portletRequest = getPortletRequest(request);
        return portletRequest != null
                && portletRequest.getPortletMode() == PortletMode.EDIT;
    }
     */

    /*-
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
     */

    @Override
    public String getTheme(final UICreateEvent event) {
        return themeName;
    }
}