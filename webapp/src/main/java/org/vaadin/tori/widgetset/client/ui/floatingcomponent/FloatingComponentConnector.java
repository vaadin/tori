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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(FloatingComponent.class)
public class FloatingComponentConnector extends AbstractExtensionConnector
        implements FloatingComponentClientRpc {

    private static final String STYLE_VISIBLE = "floatingcomponent-visible";
    private Widget widget = null;
    private Timer timer;

    @Override
    protected void init() {
        super.init();
        registerRpc(FloatingComponentClientRpc.class, this);
    }

    @Override
    protected void extend(final ServerConnector target) {
        widget = ((ComponentConnector) target).getWidget();
    }

    @Override
    public void flashIfNotVisible(Connector otherConnectort) {
        Element element = ((AbstractComponentConnector) otherConnectort)
                .getWidget().getElement();
        if (!isElementInViewport(element)) {
            widget.addStyleName(STYLE_VISIBLE);
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer() {
                @Override
                public void run() {
                    widget.removeStyleName(STYLE_VISIBLE);
                }
            };
            timer.schedule(5000);
        }
    }

    private static native boolean isElementInViewport(Element el)
    /*-{
        var rect = el.getBoundingClientRect();
        return (
            rect.top >= 0 &&
            rect.bottom <= ($wnd.innerHeight || $doc.documentElement.clientHeight)
        );
    }-*/;
}
