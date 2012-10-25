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

package org.vaadin.tori.mvp;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * This class is only for placeholder purposes for development.
 */
@SuppressWarnings("serial")
public class NullViewImpl extends AbstractView<NullView, NullPresenter>
        implements NullView {

    @Override
    public void initView() {
        // NOP
    }

    @Override
    protected Component createCompositionRoot() {
        return new Label("Nothing here!");
    }

    @Override
    protected NullPresenter createPresenter() {
        return new NullPresenter();
    }

    @Override
    protected void navigationTo(final String[] arguments) {
        // NOP
    }

}
