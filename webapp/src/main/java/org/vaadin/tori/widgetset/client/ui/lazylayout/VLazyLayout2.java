package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.StyleConstants;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VMarginInfo;
import com.vaadin.terminal.gwt.client.ui.csslayout.VCssLayout;

public class VLazyLayout2 extends SimplePanel {
    public static final String TAGNAME = "lazylayout";
    public static final String CLASSNAME = "v-" + TAGNAME;

    public interface ComponentFetcher {
        void fetchIndices(List<Integer> indicesToFetch);
    }

    public static class FlowPane extends FlowPanel {

        public FlowPane() {
            super();
            setStyleName(CLASSNAME + "-container");
        }

        void addOrMove(final Widget child, final int index) {
            if (child.getParent() == this) {
                final int currentIndex = getWidgetIndex(child);
                if (index == currentIndex) {
                    return;
                }
            }
            insert(child, index);
        }
    }

    private static class PlaceholderWidget extends HTML {
        public PlaceholderWidget(final String placeholderWidth,
                final String placeholderHeight) {
            setWidth(placeholderWidth);
            setHeight(placeholderHeight);
            setStyleName(CLASSNAME + "-placeholder");
        }
    }

    private class SecondaryFetchTimer extends Timer {
        private static final int SECONDARY_MULTIPLIER = 3;

        @Override
        public void run() {
            findAllThingsToFetchAndFetchThem(distance * SECONDARY_MULTIPLIER);
        }

        public void scheduleNew() {
            super.cancel();
            super.schedule(renderDelay * SECONDARY_MULTIPLIER);
        }
    }

    private final FlowPane panel = new FlowPane();
    private final Element margin = DOM.createDiv();

    private int totalAmountOfComponents = 0;
    private String placeholderWidth = "100%";
    private String placeholderHeight = "400px";

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration scrollHandlerRegistrationWin;

    private final Timer scrollPoller = new Timer() {
        @Override
        public void run() {
            findAllThingsToFetchAndFetchThem(distance);
            secondaryLoader.scheduleNew();
        }
    };
    private final SecondaryFetchTimer secondaryLoader = new SecondaryFetchTimer();
    private int distance;
    private int renderDelay;
    private boolean scrollingWasProgrammaticallyAdjusted = false;

    private ComponentFetcher fetcher = null;

    public VLazyLayout2() {
        super();
        getElement().appendChild(margin);
        setStyleName(CLASSNAME);
        margin.setClassName(CLASSNAME + "-margin");
        setWidget(panel);
    }

    @Override
    protected Element getContainerElement() {
        return margin;
    }

    /**
     * Sets CSS classes for margin based on the given parameters.
     * 
     * @param margins
     *            A {@link VMarginInfo} object that provides info on
     *            top/left/bottom/right margins
     */
    protected void setMarginStyles(final VMarginInfo margins) {
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_TOP, margins.hasTop());
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_RIGHT, margins.hasRight());
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_BOTTOM, margins.hasBottom());
        setStyleName(margin, VCssLayout.CLASSNAME + "-"
                + StyleConstants.MARGIN_LEFT, margins.hasLeft());
    }

    public void setComponentsAmount(final int newAmountOfComponents) {
        if (newAmountOfComponents != totalAmountOfComponents) {

            if (newAmountOfComponents < totalAmountOfComponents) {
                // TODO
                VConsole.error("LazyLayout doesn't support removing of components");
            }

            for (int i = totalAmountOfComponents; i < newAmountOfComponents; i++) {
                panel.add(new PlaceholderWidget(placeholderWidth,
                        placeholderHeight));
            }

            totalAmountOfComponents = newAmountOfComponents;
            VConsole.log("***************** COMPONENT AMOUNT CHANGED: "
                    + newAmountOfComponents);
        }
    }

    public void setPlaceholderSize(final String placeholderHeight,
            final String placeholderWidth) {
        this.placeholderHeight = placeholderHeight;
        this.placeholderWidth = placeholderWidth;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
    }

    @Override
    protected void onDetach() {
        removeScrollHandlers();
        super.onDetach();
    }

    public void attachScrollHandlersIfNeeded(final Widget rootWidget) {
        if (scrollHandlerRegistration == null) {
            // Cannot use Window.addWindowScrollHandler() in Vaadin apps,
            // but we must listen for scroll events in the VView instance
            // instead...
            final ScrollHandler handler = new ScrollHandler() {
                @Override
                public void onScroll(final ScrollEvent event) {
                    startScrollLoad();
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
                    startScrollLoad();
                }

            };
            scrollHandlerRegistrationWin = Window
                    .addWindowScrollHandler(handler);
        }
    }

    private void removeScrollHandlers() {
        if (scrollHandlerRegistration != null) {
            scrollHandlerRegistration.removeHandler();
        }
        if (scrollHandlerRegistrationWin != null) {
            scrollHandlerRegistrationWin.removeHandler();
        }
        scrollPoller.cancel();
        secondaryLoader.cancel();
        super.onDetach();
    }

    private void startScrollLoad() {
        if (!scrollingWasProgrammaticallyAdjusted) {
            scrollPoller.cancel();
            scrollPoller.schedule(renderDelay);
        }

        scrollingWasProgrammaticallyAdjusted = false;
    }

    public void setRenderDistance(final int renderDistance) {
        distance = renderDistance;
    }

    public void setRenderDelay(final int renderDelay) {
        this.renderDelay = renderDelay;
    }

    private boolean findAllThingsToFetchAndFetchThem(final int distance) {

        final Set<Widget> componentsToLoad = new HashSet<Widget>();
        for (int i = 0; i < panel.getWidgetCount(); i++) {
            final Widget child = panel.getWidget(i);
            final boolean isAPlaceholderWidget = child.getClass() == PlaceholderWidget.class;
            if (isAPlaceholderWidget && isBeingShown(child, distance)) {
                componentsToLoad.add(child);
            }
        }

        if (!componentsToLoad.isEmpty()) {

            final List<Integer> idsToLoad = new ArrayList<Integer>();
            for (final Widget widgetPlaceholder : componentsToLoad) {
                idsToLoad.add(panel.getWidgetIndex(widgetPlaceholder));
            }

            if (fetcher != null) {
                fetcher.fetchIndices(idsToLoad);
            } else {
                VConsole.error("LazyLayout has no fetcher!");
            }
        }

        return !componentsToLoad.isEmpty();
    }

    private static boolean isBeingShown(final Widget child, final int proximity) {

        final Element element = child.getElement();

        /*
         * track the original element's position as we iterate through the DOM
         * tree
         */
        int originalTopAdjusted = 0;
        final int originalHeight = element.getOffsetHeight();
        int originalLeftAdjusted = 0;
        final int originalWidth = element.getOffsetWidth();

        com.google.gwt.dom.client.Element childElement = element;
        com.google.gwt.dom.client.Element parentElement = element
                .getParentElement();

        while (parentElement != null) {

            // clientheight == the height as seen in browser
            // offsetheight == the DOM element's native height

            // What part of its canvas the parent shows, relative to its own
            // coordinates (0,0 is the top left corner)
            final int parentTop = parentElement.getScrollTop();
            final int parentBottom = parentTop
                    + parentElement.getClientHeight();
            final int parentLeft = parentElement.getScrollLeft();
            final int parentRight = parentLeft + parentElement.getClientWidth();

            /*
             * renderbox is the target box that is checked for visibility. If
             * the renderbox and parent's viewport don't overlap, it should not
             * be rendered. The renderbox is the child's position with an
             * adjusted margin.
             */
            final int renderBoxTop = childElement.getOffsetTop() - proximity;
            final int renderBoxBottom = childElement.getOffsetTop()
                    + childElement.getOffsetHeight() + proximity;
            final int renderBoxLeft = childElement.getOffsetLeft() - proximity;
            final int renderBoxRight = childElement.getOffsetLeft()
                    + childElement.getOffsetWidth() + proximity;

            if (!colliding2D(parentTop, parentRight, parentBottom, parentLeft,
                    renderBoxTop, renderBoxRight, renderBoxBottom,
                    renderBoxLeft)) {
                return false;
            }

            /*
             * see if the original component is visible from the parent. Move
             * the object around to correspond the relative changes in position.
             * The offset is always relative to the parent - not the canvas.
             */
            originalTopAdjusted += childElement.getOffsetTop()
                    - childElement.getScrollTop();
            originalLeftAdjusted += childElement.getOffsetLeft()
                    - childElement.getScrollLeft();
            if (!colliding2D(parentTop, parentRight, parentBottom, parentLeft,
                    originalTopAdjusted - proximity, originalLeftAdjusted
                            + originalWidth + proximity, originalTopAdjusted
                            + originalHeight + proximity, originalLeftAdjusted
                            - proximity)) {
                return false;
            }

            childElement = parentElement;
            parentElement = childElement.getOffsetParent();
        }

        // lastly, check the browser itself.
        final int parentTop = Window.getScrollTop();
        final int parentBottom = parentTop + Window.getClientHeight();
        final int parentLeft = Window.getScrollLeft();
        final int parentRight = parentLeft + Window.getClientWidth();

        final int renderBoxTop = childElement.getOffsetTop() - proximity;
        final int renderBoxBottom = childElement.getOffsetTop()
                + childElement.getClientHeight() + proximity;

        final int renderBoxLeft = childElement.getOffsetLeft() - proximity;
        final int renderBoxRight = childElement.getOffsetLeft()
                + childElement.getClientWidth() + proximity;

        if (!colliding2D(parentTop, parentRight, parentBottom, parentLeft,
                renderBoxTop, renderBoxRight, renderBoxBottom, renderBoxLeft)) {
            return false;
        }

        originalTopAdjusted += childElement.getOffsetTop();
        originalLeftAdjusted += childElement.getOffsetLeft();
        if (!colliding2D(parentTop, parentRight, parentBottom, parentLeft,
                originalTopAdjusted - proximity, originalLeftAdjusted
                        + originalWidth + proximity, originalTopAdjusted
                        + originalHeight + proximity, originalLeftAdjusted
                        - proximity)) {
            return false;
        }

        return true;
    }

    /**
     * Check whether a box overlaps (partially or completely) another.
     */
    private static boolean colliding2D(final int topA, final int rightA,
            final int bottomA, final int leftA, final int topB,
            final int rightB, final int bottomB, final int leftB) {

        final boolean verticalCollide = colliding1D(topA, bottomA, topB,
                bottomB);
        final boolean horizontalCollide = colliding1D(leftA, rightA, leftB,
                rightB);
        return verticalCollide && horizontalCollide;
    }

    /**
     * Check whether a line overlaps (partially or completely) another.
     */
    private static boolean colliding1D(final int startA, final int endA,
            final int startB, final int endB) {
        if (endA < startB) {
            return false;
        } else if (startA > endB) {
            return false;
        } else {
            return true;
        }
    }

    public void setFetcher(final ComponentFetcher componentFetcher) {
        fetcher = componentFetcher;
    }

    public void put(final Widget widget, final int i) {
        try {

            final Widget panelWidget = panel.getWidget(i);

            if (panelWidget.equals(widget)) {
                VConsole.error("Unnecessary placement command at index " + i);
                return;
            }

            if (panelWidget instanceof PlaceholderWidget) {
                panel.remove(i);
                panel.insert(widget, i);
            } else {
                VConsole.error("Trying to replace a component that isn't a placeholder. Index "
                        + i);
            }
        } catch (final IndexOutOfBoundsException e) {
            VConsole.error("Trying to replace a widget to a slot that doesn't exist. Index "
                    + i);
        }
    }
}
