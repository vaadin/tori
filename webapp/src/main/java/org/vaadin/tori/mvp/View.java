package org.vaadin.tori.mvp;

import org.vaadin.navigator.Navigator;

public interface View extends Navigator.View {

    /**
     * Initializes the UI components used by this View.
     */
    void initView();

}
