/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.util.CurrentInstance;

@SuppressWarnings("serial")
public class ToriServlet extends VaadinServlet {

    private static final String DEFAULT_THEME = "tori";

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
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(final SessionInitEvent event)
                    throws ServiceException {
                String theme = getInitParameter("theme");
                theme = (theme != null) ? theme : DEFAULT_THEME;

                event.getSession().addUIProvider(new ToriUiProvider(theme));
            }
        });
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriServlet.class);
    }
}
