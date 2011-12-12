package org.vaadin.tori;

import javax.servlet.http.HttpServletRequest;

/**
 * Implement this interface to signal that the service requires the
 * {@link HttpServletRequest} instance to be set for each request.
 */
public interface HttpServletRequestAware {

    void setRequest(HttpServletRequest request);

}
