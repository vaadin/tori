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

import org.vaadin.tori.ToriNavigator;

public interface View {

    /**
     * Initializes the UI components used by this View.
     */
    void initView();

    /**
     * Init view.
     * 
     * Convenience method which navigator call before slightly before the view
     * is first time rendered. This is called only once in the lifetime of each
     * view instance. In many cases it is better to construct UI within this
     * method than in constructor as you are guaranteed to get references to
     * application and navigator here.
     * 
     * @param navigator
     *            Reference to navigator that controls the window where this
     *            view is attached to.
     */
    public void init(ToriNavigator navigator);

    /**
     * This view is navigated to.
     * 
     * This method is always called before the view is shown on screen. If there
     * is any additional id to data what should be shown in the view, it is also
     * optionally passed as parameter.
     * 
     * @param arguments
     *            The arguments passed to this view. If no arguments are passed,
     *            the array is empty.
     */
    public void navigateTo(String[] arguments);
}
