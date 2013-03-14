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

import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.Connector;

@SuppressWarnings("serial")
public class FloatingBarState extends AbstractComponentState {
    private static final int DEFAULT_SCROLL_THRESHOLD = 200;
    private int scrollThreshold = DEFAULT_SCROLL_THRESHOLD;
    private Connector scrollComponent;
    private Connector content;
    private boolean portlet;
    private boolean topAligned;

    public final int getScrollThreshold() {
        return scrollThreshold;
    }

    public final void setScrollThreshold(final int scrollThreshold) {
        this.scrollThreshold = scrollThreshold;
    }

    public final Connector getScrollComponent() {
        return scrollComponent;
    }

    public final void setScrollComponent(final Connector scrollComponent) {
        this.scrollComponent = scrollComponent;
    }

    public final Connector getContent() {
        return content;
    }

    public final void setContent(final Connector content) {
        this.content = content;
    }

    public final boolean isPortlet() {
        return portlet;
    }

    public final void setPortlet(final boolean portlet) {
        this.portlet = portlet;
    }

    public final boolean isTopAligned() {
        return topAligned;
    }

    public final void setTopAligned(final boolean topAligned) {
        this.topAligned = topAligned;
    }

}
