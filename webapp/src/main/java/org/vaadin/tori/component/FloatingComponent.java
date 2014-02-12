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

package org.vaadin.tori.component;

import org.vaadin.tori.widgetset.client.ui.floatingcomponent.FloatingComponentClientRpc;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractComponent;

@SuppressWarnings("serial")
public class FloatingComponent extends AbstractExtension {
    public final void extend(final AbstractComponent target) {
        super.extend(target);
        target.addStyleName("floatingcomponent");
    }

    public void flashIfNotVisible(AbstractComponent otherComponent) {
        getRpcProxy(FloatingComponentClientRpc.class).flashIfNotVisible(
                otherComponent);
    }
}
