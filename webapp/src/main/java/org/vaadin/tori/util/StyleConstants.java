package org.vaadin.tori.util;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Style name constants to be used throughout the whole application.
 */
public final class StyleConstants {

    private StyleConstants() {
        // not instantiable
    }

    /**
     * Use for {@link HorizontalLayout} or {@link VerticalLayout} when you want
     * a smaller margin.
     */
    public static final String HALF_MARGIN = "halfMargin";

    /**
     * Use for {@link PopupButton} when you want to hide the indicator arrow.
     */
    public static final String POPUP_INDICATOR_HIDDEN = "popupIndicatorHidden";
}
