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

package org.vaadin.tori.component.category;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
class RearrangeControls extends CustomComponent {

    public RearrangeControls(final RearrangeListener listener) {
        final HorizontalLayout rearrangeControls = new HorizontalLayout();

        rearrangeControls.setSpacing(true);
        rearrangeControls.addComponent(new Button("Apply rearrangement",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        if (listener != null) {
                            listener.applyRearrangement();
                        }
                    }
                }));
        rearrangeControls.addComponent(new Button("Cancel",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        if (listener != null) {
                            listener.cancelRearrangement();
                        }
                    }
                }));

        setCompositionRoot(rearrangeControls);
        setWidth(null);
    }

    interface RearrangeListener {

        void applyRearrangement();

        void cancelRearrangement();

    }
}
