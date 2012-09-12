/*
 * Copyright 2011 Vaadin Ltd.
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
package org.vaadin.tori.widgetset.client.ui.threadlistingrow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.UIDL;
import com.vaadin.client.VCaptionWrapper;
import com.vaadin.client.VConsole;
import com.vaadin.client.ui.VOverlay;
import com.vaadin.client.ui.richtextarea.VRichTextArea;

public class VThreadListingRow extends HTML {

    public static final String CLASSNAME = "v-thread-listing-row";

    /** For server-client communication */
    String uidlId;
    ApplicationConnection client;

    /** This variable helps to communicate popup visibility to the server */
    boolean hostPopupVisible;

    final CustomPopup popup;
    private final Label loading = new Label();

    private int x = -1;
    private int y = -1;

    /**
     * loading constructor
     */
    public VThreadListingRow() {
        super();
        popup = new CustomPopup();

        setStyleName(CLASSNAME);
        popup.setStyleName(CLASSNAME + "-popup");
        loading.setStyleName(CLASSNAME + "-loading");

        setHTML("");
        popup.setWidget(loading);

        // When we click to open the popup...
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final com.google.gwt.dom.client.Element elem = Element.as(event
                        .getNativeEvent().getEventTarget());
                if (elem.getClassName().contains("menutrigger")) {
                    // bottom right corner of the menutrigger
                    x = elem.getAbsoluteLeft() + elem.getOffsetWidth();
                    y = elem.getAbsoluteTop() + elem.getOffsetHeight();
                    // x = event.getClientX() + Window.getScrollLeft();
                    // y = event.getClientY() + Window.getScrollTop();
                    updateState(true);
                } else {
                    /*
                     * not at all sure if this else-block is needed at all. It
                     * might've been trying to solve the same problem as
                     * fixIphoneClickBug() does.
                     */
                    final String threadURI = getThreadURI(getElement());
                    if (threadURI != null) {
                        event.preventDefault();
                        event.stopPropagation();
                        Window.Location.assign(threadURI);
                    } else {
                        VConsole.error("Thread was clicked, but no URI was found for the thread.");
                    }
                }
            }
        });

        // ..and when we close it
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(final CloseEvent<PopupPanel> event) {
                x = -1;
                y = -1;
                updateState(false);
            }
        });

        popup.setAnimationEnabled(true);
    }

    private String getThreadURI(final Element thisElement) {
        final NodeList<Node> childNodes = thisElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Element e = (Element) childNodes.getItem(i);
            final String tagName = e.getTagName();
            if ("a".equalsIgnoreCase(tagName)) {
                return e.getPropertyString("href");
            }
        }
        return null;
    }

    /**
     * Update popup visibility to server
     * 
     * @param y
     * @param x
     * 
     * @param visibility
     */
    private void updateState(final boolean visible) {
        // If we know the server connection
        // then update the current situation
        if (uidlId != null && client != null && isAttached()) {
            client.updateVariable(uidlId, "popupVisibility", visible, true);
        }
    }

    void preparePopup(final CustomPopup popup) {
        popup.setVisible(false);
        popup.show();
    }

    /**
     * Determines the correct position for a popup and displays the popup at
     * that position.
     * 
     * By default, the popup is shown centered relative to its host component,
     * ensuring it is visible on the screen if possible.
     * 
     * Can be overridden to customize the popup position.
     * 
     * @param popup
     */
    protected void showPopup(final CustomPopup popup) {
        popup.setPopupPosition(0, 0);

        popup.setVisible(true);
    }

    void reposition() {
        if (x > 0 && y > 0) {
            popup.setPopupPosition(x, y);
        } else {
            VConsole.error("Can't position " + getClass().getName() + " popup.");
        }
    }

    /**
     * Make sure that we remove the popup when the main widget is removed.
     * 
     * @see com.google.gwt.user.client.ui.Widget#onUnload()
     */
    @Override
    protected void onDetach() {
        popup.hide();
        super.onDetach();
    }

    private static native void nativeBlur(Element e)
    /*-{
        if(e && e.blur) {
            e.blur();
        }
    }-*/;

    /**
     * This class is only protected to enable overriding showPopup, and is
     * currently not intended to be extended or otherwise used directly. Its API
     * (other than it being a VOverlay) is to be considered private and
     * potentially subject to change.
     */
    protected class CustomPopup extends VOverlay {

        private ComponentConnector popupComponentPaintable = null;
        Widget popupComponentWidget = null;
        VCaptionWrapper captionWrapper = null;

        private boolean hasHadMouseOver = false;
        private boolean hideOnMouseOut = true;
        private final Set<Element> activeChildren = new HashSet<Element>();
        private boolean hiding = false;

        public CustomPopup() {
            super(true, false, true); // autoHide, not modal, dropshadow
        }

        // For some reason ONMOUSEOUT events are not always received, so we have
        // to use ONMOUSEMOVE that doesn't target the popup
        @SuppressWarnings("deprecation")
        @Override
        public boolean onEventPreview(final Event event) {
            final Element target = DOM.eventGetTarget(event);
            final boolean eventTargetsPopup = DOM.isOrHasChild(getElement(),
                    target);
            final int type = DOM.eventGetType(event);

            // Catch children that use keyboard, so we can unfocus them when
            // hiding
            if (eventTargetsPopup && type == Event.ONKEYPRESS) {
                activeChildren.add(target);
            }

            if (eventTargetsPopup && type == Event.ONMOUSEMOVE) {
                hasHadMouseOver = true;
            }

            if (!eventTargetsPopup && type == Event.ONMOUSEMOVE) {
                if (hasHadMouseOver && hideOnMouseOut) {
                    hide();
                    return true;
                }
            }

            // Was the TAB key released outside of our popup?
            if (!eventTargetsPopup && type == Event.ONKEYUP
                    && event.getKeyCode() == KeyCodes.KEY_TAB) {
                // Should we hide on focus out (mouse out)?
                if (hideOnMouseOut) {
                    hide();
                    return true;
                }
            }

            return super.onEventPreview(event);
        }

        @Override
        public void hide(final boolean autoClosed) {
            hiding = true;
            syncChildren();
            if (popupComponentWidget != null && popupComponentWidget != loading) {
                remove(popupComponentWidget);
            }
            hasHadMouseOver = false;
            super.hide(autoClosed);
        }

        /**
         * Try to sync all known active child widgets to server
         */
        public void syncChildren() {
            // Notify children with focus
            if ((popupComponentWidget instanceof Focusable)) {
                ((Focusable) popupComponentWidget).setFocus(false);
            } else {

                checkForRTE(popupComponentWidget);
            }

            // Notify children that have used the keyboard
            for (final Element e : activeChildren) {
                try {
                    nativeBlur(e);
                } catch (final Exception ignored) {
                }
            }
            activeChildren.clear();
        }

        private void checkForRTE(final Widget popupComponentWidget2) {
            if (popupComponentWidget2 instanceof VRichTextArea) {
                ((VRichTextArea) popupComponentWidget2)
                        .synchronizeContentToServer();
            } else if (popupComponentWidget2 instanceof HasWidgets) {
                final HasWidgets hw = (HasWidgets) popupComponentWidget2;
                final Iterator<Widget> iterator = hw.iterator();
                while (iterator.hasNext()) {
                    checkForRTE(iterator.next());
                }
            }
        }

        @Override
        public boolean remove(final Widget w) {

            popupComponentPaintable = null;
            popupComponentWidget = null;
            captionWrapper = null;

            return super.remove(w);
        }

        public void updateFromUIDL(final UIDL uidl,
                final ApplicationConnection client) {

            @SuppressWarnings("deprecation")
            final ComponentConnector newPopupComponent = client
                    .getPaintable(uidl.getChildUIDL(0));

            if (newPopupComponent != popupComponentPaintable) {
                final Widget newWidget = newPopupComponent.getWidget();
                setWidget(newWidget);
                popupComponentWidget = newWidget;
                popupComponentPaintable = newPopupComponent;
            }
        }

        public void setHideOnMouseOut(final boolean hideOnMouseOut) {
            this.hideOnMouseOut = hideOnMouseOut;
        }

        /*
         * 
         * We need a hack make popup act as a child of VPopupView in Vaadin's
         * component tree, but work in default GWT manner when closing or
         * opening.
         * 
         * (non-Javadoc)
         * 
         * @see com.google.gwt.user.client.ui.Widget#getParent()
         */
        @Override
        public Widget getParent() {
            if (!isAttached() || hiding) {
                return super.getParent();
            } else {
                return VThreadListingRow.this;
            }
        }

        @Override
        protected void onDetach() {
            super.onDetach();
            hiding = false;
        }

        @Override
        public Element getContainerElement() {
            return super.getContainerElement();
        }

    }// class CustomPopup

    @Override
    public void setHTML(final String html) {
        super.setHTML(html);
        fixIphoneClickBug();
    }

    private void fixIphoneClickBug() {
        final Element bodyElement = RootPanel.getBodyElement();
        final String classNames = bodyElement.getClassName();
        if (classNames.contains("v-ios") || classNames.contains("v-android")) {
            final NodeList<Node> childNodes = getElement().getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Element e = (Element) childNodes.getItem(i);
                if ("menutrigger".equals(e.getClassName())) {
                    getElement().removeChild(e);
                    break;
                }
            }
        }
    }
}// class VPopupView
