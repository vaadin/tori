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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.VOverlay;
import com.vaadin.client.ui.VPopupView;

/**
 * The actual VOverlay that displays the content for the floating bar.
 */
class FloatingBarOverlay extends VOverlay {
    private static final String PREFIX_ALIGNMENT_CLASSNAME = "alignment-";
    private FloatingBarWidgetListener listener;
    private boolean currentVisibility = false;
    private boolean topAligned;
    private final SimplePanel wrapper = new SimplePanel();

    private Element paddingAdditionBar;
    private double prevVisibilityPercentage = -1;

    private static final String CLASSNAME = VPopupView.CLASSNAME + " "
            + VPopupView.CLASSNAME + "-popup";

    private static final String CLASSNAME_SHADOW = "floatingbar "
            + VOverlay.CLASSNAME_SHADOW;

    public FloatingBarOverlay() {
        // the shadow is defined in CSS instead of additional elements
        setShadowEnabled(false);
        setStyleName(CLASSNAME);
        setShadowStyle(CLASSNAME_SHADOW);
        setModal(false);

        super.setVisible(false);
        wrapper.addStyleName("contentwrapper");
        setWidget(wrapper);
    }

    public void setContentWidget(final Widget contentWidget) {
        wrapper.setWidget(contentWidget);

        if (topAligned) {
            final NodeList<Element> elements = Document.get()
                    .getElementsByTagName("nav");
            for (int i = 0; i < elements.getLength(); i++) {
                final Element e = elements.getItem(i);

                if (e.getClassName().contains("site-breadcrumbs")
                        || e.getClassName().contains("lfr-hudcrumbs")) {
                    paddingAdditionBar = e;
                    break;
                }
            }
        } else {
            final NodeList<Element> elements = Document.get()
                    .getElementsByTagName("div");

            for (int i = 0; i < elements.getLength(); i++) {
                final Element e = elements.getItem(i);
                if (e.getClassName().contains("chat-bar")) {
                    paddingAdditionBar = e;
                    break;
                }
            }
        }
    }

    public void update(final Widget rootWidget) {
        wrapper.setWidth(rootWidget.getOffsetWidth() + "px");
        getElement().getStyle().setLeft(rootWidget.getAbsoluteLeft(), Unit.PX);
    }

    public void setListener(final FloatingBarWidgetListener listener) {
        this.listener = listener;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (currentVisibility != visible) {
            listener.visibilityChanged(visible);
            currentVisibility = visible;
        }

        super.setVisible(visible);

        if (topAligned) {
            getElement().getStyle().setTop(0, Unit.PX);
        } else {
            getElement().getStyle().setBottom(0, Unit.PX);
        }
    }

    /**
     * Set this overlay to be partially visible.
     * 
     * @param percentage
     *            percentage of visibility ({@code >=1} means fully visible,
     *            {@code <= 0} means fully hidden).
     */
    public void setVisible(double percentage) {
        // constrain between (incl.) 0..1
        percentage = Math.max(0, Math.min(1, percentage));

        if (percentage > 0) {
            getElement().getStyle().setDisplay(Display.BLOCK);
        } else {
            getElement().getStyle().setDisplay(Display.NONE);
        }

        // this might help slow JS engines a bit.
        if (prevVisibilityPercentage == percentage) {
            return;
        }
        prevVisibilityPercentage = percentage;

        super.setVisible(true);
        if (percentage >= 1) {
            setVisible(true);
        } else if (percentage <= 0) {
            setVisible(false);
            // just to make sure that the element is truly off screen.
            percentage = -1;
        }

        final double visiblePixels = (getWidget().getOffsetHeight() * (1 - percentage));
        if (topAligned) {
            getElement().getStyle().setTop(-visiblePixels, Unit.PX);
        } else {
            getElement().getStyle().setBottom(-visiblePixels, Unit.PX);
        }

        if (paddingAdditionBar != null) {
            if (topAligned) {
                wrapper.getElement()
                        .getStyle()
                        .setPaddingTop(paddingAdditionBar.getOffsetHeight(),
                                Unit.PX);
            } else {
                wrapper.getElement()
                        .getStyle()
                        .setPaddingBottom(paddingAdditionBar.getOffsetHeight(),
                                Unit.PX);
            }
        }

        positionOrSizeUpdated();
    }

    // overridden for method visibility
    @Override
    public void positionOrSizeUpdated() {
        super.positionOrSizeUpdated();
    }

    public void setTopAligned(final boolean topAligned) {
        removeStyleName(PREFIX_ALIGNMENT_CLASSNAME
                + (this.topAligned ? "top" : "bottom"));
        this.topAligned = topAligned;
        addStyleName(PREFIX_ALIGNMENT_CLASSNAME
                + (this.topAligned ? "top" : "bottom"));
    }

}