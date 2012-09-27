package org.vaadin.tori.widgetset.client.ui.lazylayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
import com.vaadin.client.VConsole;

public class VLazyLayout extends SimplePanel {
    public static final String TAGNAME = "lazylayout";
    public static final String CLASSNAME = "v-" + TAGNAME;

    static final boolean DEBUG = false;

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
        private static final double SECONDARY_MULTIPLIER = 2d;
        private static final int AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER = 2;
        private static final int SECONDARY_TIMER = 2000;

        private int visitsLeftToTheServer = AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER;

        @Override
        public void run() {
            boolean foundSomething = false;
            while (visitsLeftToTheServer > 0 && !foundSomething) {
                final int totalExtraHeight = (int) (getFetchDistancePx() * SECONDARY_MULTIPLIER);
                final double progress = (double) AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER
                        / (double) visitsLeftToTheServer;
                final int fetchingDistance = (int) (getFetchDistancePx() + (totalExtraHeight * progress));

                foundSomething = findAllThingsToFetchAndFetchThem(fetchingDistance);

                visitsLeftToTheServer--;
                if (foundSomething) {
                    schedule(SECONDARY_TIMER);
                    return;
                }
            }

            resetCounter();
        }

        private void resetCounter() {
            visitsLeftToTheServer = AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER;
        }

        public void scheduleNew() {
            cancel();
            schedule(SECONDARY_TIMER);
            resetCounter();
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
            findAllThingsToFetchAndFetchThem(getFetchDistancePx());
            secondaryLoader.scheduleNew();
        }
    };
    private final SecondaryFetchTimer secondaryLoader = new SecondaryFetchTimer();
    private double distanceMultiplier;
    private int pageHeight;
    private int renderDelay;
    private boolean scrollingWasProgrammaticallyAdjusted = false;

    private ComponentFetcher fetcher = null;

    /**
     * @see VLazyLayout#updateScrollAdjustmentReference()
     */
    private int scrollOffsetToReferenceWidgetPx = -1;
    private Widget referenceWidget;

    public VLazyLayout() {
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

    private static void debug(final String msg) {
        if (DEBUG) {
            VConsole.error("[LazyLayout] " + msg);
        }
    }

    public void setAmountOfComponents(final int newAmountOfComponents) {
        debug("New amount of components: " + newAmountOfComponents);
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
        refreshPageHeight();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                VLazyLayout.this.findAllThingsToFetchAndFetchThem();
            }
        });
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
    }

    private void startScrollLoad() {
        if (!scrollingWasProgrammaticallyAdjusted) {
            secondaryLoader.cancel();
            scrollPoller.cancel();
            scrollPoller.schedule(renderDelay);
        }

        scrollingWasProgrammaticallyAdjusted = false;
    }

    public void setRenderDistanceMultiplier(
            final double renderDistanceMultiplier) {
        distanceMultiplier = renderDistanceMultiplier;
    }

    public void setRenderDelay(final int renderDelay) {
        this.renderDelay = renderDelay;
    }

    protected void findAllThingsToFetchAndFetchThem() {
        findAllThingsToFetchAndFetchThem(pageHeight);
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
                debug("Fetching " + idsToLoad.size() + " components.");
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

    private void _replaceComponent(final Widget widget, final int i) {

        if (widget == null) {
            VConsole.error("LazyLayout: Widget for index " + i
                    + " was null. Replacing with error indicator.");
            panel.remove(i);
            panel.insert(new HTML(
                    "<div style='background-color:red; color:white; font-weight: "
                            + "bold; border: 3px solid #f88; padding:5px'>"
                            + "broken lazy layout component at index " + i
                            + "</div>"), i);
            return;
        }

        try {

            final Widget panelWidget = panel.getWidget(i);

            if (panelWidget.equals(widget)) {
                VConsole.error("Unnecessary placement command at index " + i);
                return;
            }

            panel.remove(i);
            panel.insert(widget, i);
        } catch (final IndexOutOfBoundsException e) {
            VConsole.error("Trying to replace a widget to a slot that doesn't exist. Index "
                    + i);
        }
    }

    private void setScrollTop(final int topPx) {
        com.google.gwt.dom.client.Element parent = getElement();
        while (parent != null && parent.getScrollTop() <= 0) {
            parent = parent.getOffsetParent();
        }
        if (parent != null) {
            parent.setScrollTop(topPx);
            debug("setting scrolltop to " + topPx);
        } else {
            final int currentScrollLeft = Window.getScrollLeft();
            Window.scrollTo(currentScrollLeft, topPx);
            debug("setting scrolltop for window to " + topPx);
        }
        scrollingWasProgrammaticallyAdjusted = true;
    }

    /**
     * Gets the offset top of the closest component that's above the given
     * value.
     * 
     * @param topPixels
     *            top pixels into the layout
     * @return the position of the component above the given argument. 0 if it's
     *         above the first component. -1 if something went weirdly wrong (i
     *         guess no children available?)
     */
    private int getPreviousWidgetOffsetTop(final int topPixels) {
        int previousOffsetTop = 0;
        for (int i = 0; i < panel.getWidgetCount(); i++) {
            final Widget child = panel.getWidget(i);
            final int offsetTop = child.getElement().getOffsetTop();
            if (topPixels < previousOffsetTop) {
                return previousOffsetTop;
            } else {
                previousOffsetTop = offsetTop;
            }
        }
        return -1;
    }

    private int getCurrentScrollPos() {
        final com.google.gwt.dom.client.Element parent = getFirstScrolledElement();

        if (parent != null) {
            return parent.getScrollTop();
        } else {
            return Window.getScrollTop();
        }
    }

    private com.google.gwt.dom.client.Element getFirstScrolledElement() {
        com.google.gwt.dom.client.Element parent = getElement()
                .getOffsetParent();
        while (parent != null && parent.getScrollTop() <= 0) {
            parent = parent.getOffsetParent();
        }
        return parent;
    }

    /**
     * Make sure to call {@link #updateScrollAdjustmentReference()}
     * appropriately (i.e. before you start changing the DOM) before calling
     * this method.
     */
    private void fixScrollbar() {
        if (referenceWidget != null) {
            if (referenceWidget.getParent() != panel) {
                VConsole.error("Scroll adjustment reference widget is no longer a child of me");
            }

            final int referenceOffsetTop = referenceWidget.getElement()
                    .getOffsetTop();
            debug("offset: " + referenceOffsetTop);
            setScrollTop(referenceOffsetTop + scrollOffsetToReferenceWidgetPx);

            referenceWidget = null;
            scrollOffsetToReferenceWidgetPx = 0;
        }
    }

    /**
     * This method re-evaluates the component the scroll adjustment should base
     * upon.
     * 
     * @see #fixScrollbar()
     */
    private void updateScrollAdjustmentReference() {
        final int currentScrollPos = getCurrentScrollPos();
        final Widget referenceWidget = getFirstNonPlaceholderWidgetInOrAfter(currentScrollPos);

        if (referenceWidget != null) {
            this.referenceWidget = referenceWidget;
            debug("is parent: " + (referenceWidget.getParent() == panel));
            scrollOffsetToReferenceWidgetPx = currentScrollPos
                    - referenceWidget.getElement().getOffsetTop();
            debug("scroll offset: " + scrollOffsetToReferenceWidgetPx);
        } else {
            this.referenceWidget = null;
            scrollOffsetToReferenceWidgetPx = -1;
        }
    }

    /**
     * Gets the first non-placeholder widget in the current view.
     * 
     * @param topPixels
     * @return <code>null</code> if no suitable widget was found.
     */
    private Widget getFirstNonPlaceholderWidgetInOrAfter(final int topPixels) {
        // TODO: refactor. maek moar methods.

        for (int i = 0; i < panel.getWidgetCount(); i++) {
            final int childOffsetTop = panel.getWidget(i).getElement()
                    .getOffsetTop();

            if (topPixels < childOffsetTop) {
                /*
                 * we've reached the scrolled element's top. Let's search for
                 * the first non-placeholder widget
                 */

                if (i > 0) {
                    /*
                     * rewind one element, because that might be a
                     * non-placeholder widget, and we want to adjust to that.
                     */
                    i--;
                }

                for (; i < panel.getWidgetCount(); i++) {
                    final Widget candidateChild = panel.getWidget(i);
                    if (!(candidateChild instanceof PlaceholderWidget)) {
                        return candidateChild;
                    }

                    if (candidateChild.getElement().getOffsetTop() > (topPixels + Window
                            .getClientHeight())) {
                        /*
                         * we've already checked for a screenful of elements.
                         * There's no non-placeholder widgets to adjust by.
                         * Screw it.
                         */
                        debug("Screen shows no non-placeholder elements.");
                        return null;
                    }
                }
            }
        }

        // lazylayout is off the screen completely.
        return null;
    }

    private static native double getPreciseHeight(Element element)
    /*-{
       if (typeof (element.getBoundingClientRect) == 'function') {
          var rect = element.getBoundingClientRect();
          return rect.bottom - rect.top;
       } else {
          return element.offsetHeight;
       }
    }-*/;

    private static double getPreciseHeight(final Widget widget) {
        return getPreciseHeight(widget.getElement());
    }

    public void refreshPageHeight() {
        if (getParent() != null) {
            final int windowHeight = Window.getClientHeight();
            final int lazyLayoutHeight = getElement().getOffsetHeight();
            pageHeight = Math.min(windowHeight, lazyLayoutHeight);
        } else {
            pageHeight = -1;
        }
    }

    private int getFetchDistancePx() {
        return (int) (pageHeight * distanceMultiplier);
    }

    public void replaceComponents(final int[] indices, final Widget[] widgets) {
        try {
            updateScrollAdjustmentReference();
            for (int i = 0; i < indices.length; i++) {
                _replaceComponent(widgets[i], indices[i]);
            }
            // renderQueue.add(indices, widgets);
        } catch (final IllegalArgumentException e) {
            VConsole.error(e);
        }

        fixScrollbar();
    }
}
