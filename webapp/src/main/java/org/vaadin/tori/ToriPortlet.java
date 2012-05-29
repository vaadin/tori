package org.vaadin.tori;

import javax.portlet.PortletRequest;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import com.vaadin.terminal.gwt.server.Constants;
import com.vaadin.terminal.gwt.server.WrappedPortletRequest;

@SuppressWarnings("serial")
public class ToriPortlet extends ApplicationPortlet2 {

    @Override
    protected WrappedPortletRequest createWrappedRequest(
            final PortletRequest request) {
        WrappedPortletRequest wrapped = super.createWrappedRequest(request);

        final String portalInfo = request.getPortalContext().getPortalInfo()
                .toLowerCase();
        if (portalInfo.contains("liferay")) {
            wrapped = new WrappedLiferayRequest(request,
                    wrapped.getDeploymentConfiguration()) {
                @Override
                public String getPortalProperty(final String name) {
                    if (Constants.PORTAL_PARAMETER_VAADIN_RESOURCE_PATH
                            .equals(name)) {
                        return request.getContextPath();
                    }
                    return super.getPortalProperty(name);
                }
            };
        }
        return wrapped;
    }

}
