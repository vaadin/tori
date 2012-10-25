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

package org.vaadin.tori.widgetset.client.ui.lazylayout;

import com.vaadin.shared.annotations.DelegateToWidget;
import com.vaadin.shared.ui.AbstractLayoutState;

@SuppressWarnings("serial")
public class LazyLayoutState extends AbstractLayoutState {
    @DelegateToWidget
    public double renderDistanceMultiplier = 1;
    @DelegateToWidget
    public int renderDelay = 700;

    /* this isn't delegated to widget, since order of execution matters */
    public int amountOfComponents = 0;
    public String placeholderHeight;
    public String placeholderWidth;
}
