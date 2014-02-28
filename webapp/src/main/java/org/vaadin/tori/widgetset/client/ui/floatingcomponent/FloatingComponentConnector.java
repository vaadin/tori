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

package org.vaadin.tori.widgetset.client.ui.floatingcomponent;

import org.vaadin.tori.component.FloatingComponent;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(FloatingComponent.class)
public class FloatingComponentConnector extends AbstractExtensionConnector
        implements FloatingComponentClientRpc {

    private static final String STYLE_VISIBLE = "floatingcomponent-visible";
    private Widget widget = null;
    private Timer timer;
    private static final int DELAY = 10000;
    private HandlerRegistration mouseOverHandler;
    private HandlerRegistration mouseOutHandler;

    @Override
    protected void init() {
        super.init();
        registerRpc(FloatingComponentClientRpc.class, this);
    }

    @Override
    protected void extend(final ServerConnector target) {
        widget = ((ComponentConnector) target).getWidget();

        mouseOverHandler = widget.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(final MouseOverEvent event) {
                cancelTimer();
            }
        }, MouseOverEvent.getType());

        mouseOutHandler = widget.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(final MouseOutEvent event) {
                scheduleTimer();
            }
        }, MouseOutEvent.getType());
    }

    @Override
    public void onUnregister() {
        mouseOverHandler.removeHandler();
        mouseOutHandler.removeHandler();
        super.onUnregister();
    }

    @Override
    public void flash() {
        widget.addStyleName(STYLE_VISIBLE);
        scheduleTimer();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void scheduleTimer() {
        cancelTimer();
        timer = new Timer() {
            @Override
            public void run() {
                widget.removeStyleName(STYLE_VISIBLE);
            }
        };
        timer.schedule(DELAY);
    }

}
