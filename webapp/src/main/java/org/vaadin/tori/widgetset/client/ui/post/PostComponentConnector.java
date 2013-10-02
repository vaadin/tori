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

package org.vaadin.tori.widgetset.client.ui.post;

import org.vaadin.tori.component.post.PostComponent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PostComponent.class)
public final class PostComponentConnector extends
        AbstractComponentContainerConnector {

    private final PostComponentRpc rpc = RpcProxy.create(
            PostComponentRpc.class, this);

    @Override
    protected Widget createWidget() {
        final PostWidget widget = GWT.create(PostWidget.class);
        widget.setListener(rpc);
        return widget;
    }

    @Override
    public PostComponentState getState() {
        return (PostComponentState) super.getState();
    }

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        final PostWidget widget = (PostWidget) getWidget();
        widget.updatePostData(getState(), getResourceUrl("avatar"));
    }

    @Override
    public void onConnectorHierarchyChange(
            final ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        // Ignore
    }

    @Override
    public void updateCaption(final ComponentConnector connector) {
        // Ignore
    }

}
