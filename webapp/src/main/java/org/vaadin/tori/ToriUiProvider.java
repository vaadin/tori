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

import org.apache.log4j.Logger;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
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
        } else {
            Logger.getLogger(getClass()).warn(
                    "Created UI is not an instance of "
                            + ToriUI.class.getName() + " but "
                            + ui.getClass().getName()
                            + ". Might cause problems.");
        }
        return ui;
    }

    @Override
    public String getTheme(final UICreateEvent event) {
        return themeName;
    }
}