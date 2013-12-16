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

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public abstract class AbstractView<V extends View, P extends Presenter<V>>
        extends CustomComponent implements View, com.vaadin.navigator.View {

    protected final Logger log = Logger.getLogger(getClass());

    private P presenter;

    /**
     * Creates the composition root for this View, do the actual initialization
     * of the view components in {@link #initView()} method.
     * 
     * @return composition root for this View
     */
    protected abstract Component createCompositionRoot();

    /**
     * Instantiates the Presenter for this View.
     * 
     * @return Presenter for this View.
     */
    protected abstract P createPresenter();

    @Override
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing view " + getClass().getName());
        }

        // initialize the view first
        setCompositionRoot(createCompositionRoot());
        initView();

        // then initialize the presenter
        presenter = createPresenter();
        if (log.isDebugEnabled()) {
            log.debug("Initializing presenter "
                    + presenter.getClass().getName());
        }
        presenter.init();
    }

    protected P getPresenter() {
        if (presenter == null) {
            init();
        }
        return presenter;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        final String[] arguments = event.getParameters().split("/");
        if (log.isDebugEnabled()) {
            log.debug("Activating view "
                    + getClass().getName()
                    + (arguments != null ? " with params: "
                            + Arrays.toString(arguments) : ""));
        }
        navigationTo(arguments);
    }

    /**
     * This method is called on each visit of this view.
     * <p/>
     * <strong>Tip:</string> use this method to pass the viewed object id to the
     * presenter for parsing.
     * 
     * @param arguments
     *            the {@link String} parameter passed to this view.
     */
    protected abstract void navigationTo(String[] arguments);

    protected ToriNavigator getNavigator() {
        return (ToriNavigator) UI.getCurrent().getNavigator();
    }

    /**
     * Get the title for the current view. <code>null</code> and empty Strings
     * are valid return values.
     */
    public abstract String getTitle();
}
