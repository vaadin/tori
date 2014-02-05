/*
 * Copyright 2014 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;

public class ToriScheduler {

    public interface ScheduledCommand {
        void execute();
    }

    private static final String TORI_SCHEDULER = "TORI_SCHEDULER";
    private static final String DEFERRED_COMMAND_FUNCTION_NAME = "org.vaadin.tori.deferredcommand";
    private static final String FUNCTION = "()";
    private final Collection<ScheduledCommand> deferredCommands = new ArrayList<ScheduledCommand>();
    private final Collection<ScheduledCommand> manualCommands = new ArrayList<ScheduledCommand>();

    public void scheduleDeferred(final ScheduledCommand command) {
        if (deferredCommands.isEmpty()) {
            JavaScript.eval(DEFERRED_COMMAND_FUNCTION_NAME + FUNCTION);
            JavaScript.getCurrent().addFunction(DEFERRED_COMMAND_FUNCTION_NAME,
                    new JavaScriptFunction() {
                        @Override
                        public void call(final JSONArray arguments)
                                throws JSONException {
                            JavaScript.getCurrent().removeFunction(
                                    DEFERRED_COMMAND_FUNCTION_NAME);
                            executeCommands(deferredCommands);
                        }
                    });
        }
        deferredCommands.add(command);
    }

    public void scheduleManual(final ScheduledCommand command) {
        manualCommands.add(command);
    }

    public void executeManualCommands() {
        executeCommands(manualCommands);
    }

    private void executeCommands(Collection<ScheduledCommand> commands) {
        final Collection<ScheduledCommand> executableCommands = new ArrayList<ScheduledCommand>(
                commands);
        commands.clear();

        for (ScheduledCommand command : executableCommands) {
            command.execute();
        }
    }

    public static ToriScheduler get() {
        final String id = TORI_SCHEDULER + UI.getCurrent().getUIId();
        ToriScheduler scheduler = (ToriScheduler) VaadinSession.getCurrent()
                .getAttribute(id);
        if (scheduler == null) {
            scheduler = new ToriScheduler();
            VaadinSession.getCurrent().setAttribute(id, scheduler);
        }
        return scheduler;
    }

}
