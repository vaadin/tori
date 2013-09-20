/*
 * Copyright 2013 Vaadin Ltd.
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

package org.vaadin.tori.component.post;

import org.json.JSONArray;
import org.json.JSONException;

import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class LazyVerticalLayout extends VerticalLayout {

    private static final int RENDER_BATCH_SIZE = 30;

    public LazyVerticalLayout() {
        JavaScript.getCurrent().addFunction(
                "org.vaadin.tori.lazyverticallayout.rendered",
                new JavaScriptFunction() {
                    @Override
                    public void call(final JSONArray arguments)
                            throws JSONException {
                        renderBatch();
                    }
                });

    }

    private void renderBatch() {
        int rendered = 0;
        for (int index = 0; index < getComponentCount(); index++) {
            Component component = getComponent(index);
            if (!component.isVisible()) {
                component.setVisible(true);
                rendered++;
            }
            if (rendered > RENDER_BATCH_SIZE) {
                break;
            }
        }
    }

    @Override
    public void addComponent(Component c) {
        if (getComponentCount() > RENDER_BATCH_SIZE) {
            c.setVisible(false);
        }
        super.addComponent(c);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        JavaScript.eval("org.vaadin.tori.lazyverticallayout.rendered()");
        super.beforeClientResponse(initial);
    }

}
