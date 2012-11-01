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

package org.vaadin.tori.widgetset.client.ui.floatingbar;

import org.vaadin.tori.component.FloatingBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(FloatingBar.class)
public final class FloatingBarConnector extends
        AbstractComponentContainerConnector {

    private final FloatingBarRpc rpc = RpcProxy.create(FloatingBarRpc.class,
            this);

    @Override
    protected Widget createWidget() {
        final FloatingBarWidget widget = GWT.create(FloatingBarWidget.class);
        widget.setListener(rpc);
        return widget;
    }

    @Override
    public void updateCaption(final ComponentConnector connector) {
        // NOP
    }

    @Override
    public FloatingBarState getState() {
        return (FloatingBarState) super.getState();
    }

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        final FloatingBarState state = getState();
        final FloatingBarWidget widget = (FloatingBarWidget) getWidget();

        widget.setRootWidget(getConnection().getRootConnector().getWidget());
        widget.setPortlet(state.isPortlet());
        widget.setTopAligned(state.isTopAligned());
        widget.setContentWidget(((ComponentConnector) state.getContent())
                .getWidget());
        widget.setscrollTreshold(state.getScrollThreshold());
        widget.setScrollWidget(((ComponentConnector) state.getScrollComponent())
                .getWidget());
    }

    @Override
    public void onConnectorHierarchyChange(
            final ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        // ignore?
    }
}
