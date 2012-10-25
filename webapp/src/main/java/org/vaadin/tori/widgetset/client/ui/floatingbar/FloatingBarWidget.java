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
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public final class FloatingBarWidget extends Widget implements ResizeHandler {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-floatingbar";

    private final FloatingBarOverlay overlay = new FloatingBarOverlay();
    private Widget scrollWidget;
    private int scrollThreshold;

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration scrollHandlerRegistrationWin;
    private HandlerRegistration resizeHandlerRegistration;

    private Widget rootWidget;

    private boolean portlet;
    private boolean topAligned;

    public FloatingBarWidget() {
        setElement(Document.get().createDivElement());
        attachResizeHandler();
        setStyleName(CLASSNAME);
    }

    @Override
    public void setStyleName(final String style) {
        super.setStyleName(style);
        overlay.addStyleName(style);
    }

    @Override
    public void setStyleName(final String style, final boolean add) {
        super.setStyleName(style, add);
        overlay.setStyleName(style, add);
    }

    public void setListener(final FloatingBarWidgetListener listener) {
        overlay.setListener(listener);
    }

    public void setScrollWidget(final Widget scrollWidget) {
        this.scrollWidget = scrollWidget;
        overlay.show();
        overlay.update(rootWidget);

        final Timer timer = new Timer() {
            @Override
            public void run() {
                overlay.setVisible(getVisibilityPercentage());
                overlay.update(rootWidget);
            }
        };
        timer.schedule(500);
    }

    public void setContentWidget(final Widget contentWidget) {
        overlay.setContentWidget(contentWidget);
    }

    public void setPortlet(final boolean isPortlet) {
        this.portlet = isPortlet;
    }

    public void setTopAligned(final boolean topAligned) {
        // assign the new alignment
        this.topAligned = topAligned;
        overlay.setTopAligned(topAligned);
    }

    private void attachResizeHandler() {
        if (resizeHandlerRegistration == null) {
            resizeHandlerRegistration = Window.addResizeHandler(this);
        }
    }

    public void setscrollTreshold(final int scrollThreshold) {
        this.scrollThreshold = scrollThreshold;
    }

    @Override
    protected void onDetach() {
        removeAllHandlers();
        overlay.hide();
        super.onDetach();
    }

    private void removeAllHandlers() {
        if (scrollHandlerRegistration != null) {
            scrollHandlerRegistration.removeHandler();
        }
        if (scrollHandlerRegistrationWin != null) {
            scrollHandlerRegistrationWin.removeHandler();
        }
        if (resizeHandlerRegistration != null) {
            resizeHandlerRegistration.removeHandler();
        }
    }

    /**
     * Returns a visibility percentage depending on the {@code scrollComponent},
     * {@code scrollThreshold} and the current scroll position.
     */
    private double getVisibilityPercentage() {
        double percentage = 1.0;
        if (scrollWidget != null) {
            final Element scrollWidgetElem = scrollWidget.getElement();
            final int widgetTop = scrollWidgetElem.getOffsetTop();
            final int windowScrollTop = getWindowScrollTop();

            if (topAligned) {
                final int widgetHeight = scrollWidgetElem.getClientHeight();
                final int scrollWidgetBottom = scrollWidgetElem
                        .getAbsoluteBottom();
                if (scrollThreshold == 0) {
                    // all or nothing
                    if (portlet) {
                        percentage = windowScrollTop < (widgetTop + widgetHeight) ? 0
                                : 1;
                    } else {
                        percentage = scrollWidgetBottom > 0 ? 0 : 1;
                    }
                } else {
                    // partially visible
                    if (portlet) {
                        percentage = (windowScrollTop - widgetTop - widgetHeight)
                                / (double) scrollThreshold;
                    } else {
                        percentage = (double) -scrollWidgetBottom
                                / scrollThreshold;
                    }
                }
            } else {
                // bottom alignment
                final int windowHeight = Window.getClientHeight();
                final int scrollWidgetTop = scrollWidgetElem.getAbsoluteTop();
                if (scrollThreshold == 0) {
                    // all or nothing
                    if (portlet) {
                        percentage = (windowScrollTop + windowHeight) > (widgetTop) ? 0
                                : 1;
                    } else {
                        percentage = scrollWidgetTop < windowHeight ? 0 : 1;
                    }
                } else {
                    // partially visible
                    if (portlet) {
                        percentage = -(windowScrollTop + windowHeight - widgetTop)
                                / (double) scrollThreshold;
                    } else {
                        percentage = (scrollWidgetTop - windowHeight)
                                / (double) scrollThreshold;
                    }
                }
            }
        }

        return percentage;
    }

    /**
     * Returns the scroll position on the Window or Vaadin's VView depending on
     * which one is scrolled.
     * 
     * @return scroll position in pixels.
     */
    private int getWindowScrollTop() {
        final Element vViewElem = rootWidget.getElement();
        final int vViewScrollTop = vViewElem.getScrollTop();
        final int vViewTop = vViewElem.getAbsoluteTop();

        // Decide whether to use:
        // 1) VView scroll position (normal Vaadin app)
        // -- or --
        // 2) Window scroll minus VView top offset (embedded app, portlet).
        final int windowScrollTop = (vViewScrollTop == 0 ? (Window
                .getScrollTop() - vViewTop) : vViewScrollTop);
        return windowScrollTop;
    }

    public void setRootWidget(final Widget rootWidget) {
        if (this.rootWidget == null) {
            this.rootWidget = rootWidget;
            if (scrollHandlerRegistration == null) {
                // Cannot use Window.addWindowScrollHandler() in Vaadin apps,
                // but we must listen for scroll events in the VView instance
                // instead...
                final ScrollHandler handler = new ScrollHandler() {
                    @Override
                    public void onScroll(final ScrollEvent event) {
                        overlay.setVisible(getVisibilityPercentage());
                    }
                };
                scrollHandlerRegistration = rootWidget.addDomHandler(handler,
                        ScrollEvent.getType());
            }
            if (scrollHandlerRegistrationWin == null) {
                // ...but within embedded apps (portlet) we do actually scroll
                // the Window, so we need also the ScrollHandler for the Window.
                final Window.ScrollHandler handler = new Window.ScrollHandler() {
                    @Override
                    public void onWindowScroll(
                            final com.google.gwt.user.client.Window.ScrollEvent event) {
                        overlay.setVisible(getVisibilityPercentage());
                    }
                };
                scrollHandlerRegistrationWin = Window
                        .addWindowScrollHandler(handler);
            }
        }
    }

    @Override
    public void onResize(final ResizeEvent event) {
        // resizing the window might also reveal/hide the scroll component
        overlay.setVisible(getVisibilityPercentage());
        overlay.positionOrSizeUpdated();
        overlay.update(rootWidget);
    }
}
