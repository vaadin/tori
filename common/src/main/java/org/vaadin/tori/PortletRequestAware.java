package org.vaadin.tori;

import javax.portlet.PortletRequest;

/**
 * Implement this interface to signal that the service requires the
 * {@link PortletRequest} instance to be set for each request.
 */
public interface PortletRequestAware {

    void setRequest(PortletRequest request);

}