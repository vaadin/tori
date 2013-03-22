package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo.Action;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;

public class ThreadListingWidget extends Widget {

    private static final String CONTEXTMENU_CLASS = "contextmenu";

    private static class Point {
        public final int top;
        public final int left;

        public Point(final int top, final int left) {
            this.top = top;
            this.left = left;
        }
    }

    private static class VisibilityHelper {

        private static boolean isBeingShown(final Element element,
                final int proximity) {

            /*
             * track the original element's position as we iterate through the
             * DOM tree
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
                final int parentRight = parentLeft
                        + parentElement.getClientWidth();

                /*
                 * renderbox is the target box that is checked for visibility.
                 * If the renderbox and parent's viewport don't overlap, it
                 * should not be rendered. The renderbox is the child's position
                 * with an adjusted margin.
                 */
                final int renderBoxTop = childElement.getOffsetTop()
                        - proximity;
                final int renderBoxBottom = childElement.getOffsetTop()
                        + childElement.getOffsetHeight() + proximity;
                final int renderBoxLeft = childElement.getOffsetLeft()
                        - proximity;
                final int renderBoxRight = childElement.getOffsetLeft()
                        + childElement.getOffsetWidth() + proximity;

                if (!colliding2D(parentTop, parentRight, parentBottom,
                        parentLeft, renderBoxTop, renderBoxRight,
                        renderBoxBottom, renderBoxLeft)) {
                    return false;
                }

                /*
                 * see if the original component is visible from the parent.
                 * Move the object around to correspond the relative changes in
                 * position. The offset is always relative to the parent - not
                 * the canvas.
                 */
                originalTopAdjusted += childElement.getOffsetTop()
                        - childElement.getScrollTop();
                originalLeftAdjusted += childElement.getOffsetLeft()
                        - childElement.getScrollLeft();
                if (!colliding2D(parentTop, parentRight, parentBottom,
                        parentLeft, originalTopAdjusted - proximity,
                        originalLeftAdjusted + originalWidth + proximity,
                        originalTopAdjusted + originalHeight + proximity,
                        originalLeftAdjusted - proximity)) {
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
                    renderBoxTop, renderBoxRight, renderBoxBottom,
                    renderBoxLeft)) {
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

    }

    private static final boolean DEBUG = true;

    private static final String CLASS_NAME = "threadlisting";
    private static final String ROW_CLASS_NAME = "threadlistingrow";
    private static final String POPUP_CLASS_NAME = ROW_CLASS_NAME + "-popup";

    private static final String PLACEHOLDER_ELEMENT = "DIV";

    private static final long POPUPTHREADID_CLEARED_VALUE = -1;

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration scrollHandlerRegistrationWin;
    private boolean scrollingWasProgrammaticallyAdjusted = false;

    private final Timer scrollPoller = new Timer() {
        @Override
        public void run() {
            findAllThingsToFetchAndFetchThem(getFetchDistancePx());
            secondaryLoader.scheduleNew();
        }
    };

    private class SecondaryFetchTimer extends Timer {
        private static final double SECONDARY_MULTIPLIER = 2d;
        private static final int AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER = 2;
        private static final int SECONDARY_TIMER = 2000;

        private int visitsLeftToTheServer = AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER;

        @Override
        public void run() {
            debug("running secondary timer");
            boolean foundSomething = false;
            while (visitsLeftToTheServer > 0 && !foundSomething) {
                final int totalExtraHeight = (int) (getFetchDistancePx() * SECONDARY_MULTIPLIER);
                final double progress = (double) AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER
                        / (double) visitsLeftToTheServer;
                final int fetchingDistance = (int) (getFetchDistancePx() + (totalExtraHeight * progress));

                foundSomething = findAllThingsToFetchAndFetchThem(fetchingDistance);

                visitsLeftToTheServer--;
                if (foundSomething) {
                    debug("found something, fetching...");
                    schedule(SECONDARY_TIMER);
                    return;
                } else {
                    debug("found nothing.");
                }
            }
            debug("done running.");

            resetCounter();
        }

        @Override
        public void cancel() {
            debug("cancelling secondary timer");
            super.cancel();
        }

        private void resetCounter() {
            visitsLeftToTheServer = AMOUNT_OF_TIME_TO_GO_BACK_TO_THE_SERVER;
        }

        public void scheduleNew() {
            debug("scheduling secondary timer");
            schedule(SECONDARY_TIMER);
            resetCounter();
        }
    }

    public interface Fetcher {
        void fetchIndices(List<Integer> indicesToFetch);

        void fetchControlsFor(int rowIndexOf);
    }

    public interface RowActionHandler {
        void handle(Action action, long threadId);
    }

    private final SecondaryFetchTimer secondaryLoader = new SecondaryFetchTimer();
    private final int renderDelay = 500;
    private int pageHeight;
    private final double distanceMultiplier = 1.0;

    private Fetcher fetcher;
    private RowActionHandler rowActionHandler;

    private Element popup;

    private HandlerRegistration popupCloseHandlerRegistration;

    private final Map<Element, Action> popupControls = new HashMap<Element, Action>();

    private long popupThreadId = POPUPTHREADID_CLEARED_VALUE;

    private Element openedThreadListingRow = null;

    public ThreadListingWidget() {
        setElement(DOM.createDiv());
        getElement().appendChild(createHeaders());
        setStyleName("threadlisting");
    }

    private Element createHeaders() {
        final Element titleDiv = DOM.createDiv();
        titleDiv.setClassName("header");

        {
            final Element topic = DOM.createDiv();
            topic.setInnerText("Topic");
            topic.setClassName("header-topic");
            titleDiv.appendChild(topic);
        }

        {
            final Element startedby = DOM.createDiv();
            startedby.setInnerText("Started by");
            startedby.setClassName("header-startedby");
            titleDiv.appendChild(startedby);
        }

        {
            final Element posts = DOM.createDiv();
            posts.setInnerText("Posts");
            posts.setClassName("header-posts");
            titleDiv.appendChild(posts);
        }

        {
            final Element latestPost = DOM.createDiv();
            latestPost.setInnerText("Latest post");
            latestPost.setClassName("header-latestpost");
            titleDiv.appendChild(latestPost);
        }

        return titleDiv;
    }

    public void init(final int rows, final List<RowInfo> preloadedRows,
            final Fetcher fetcher, final RowActionHandler handler) {
        if (rows < 0) {
            throw new IllegalArgumentException("Row amount can't be negative");
        }

        preloadRows(preloadedRows);

        for (int i = preloadedRows.size() + 1; i <= rows; i++) {
            addPlaceholder();
        }

        refreshPageHeight();
        this.fetcher = fetcher;
        this.rowActionHandler = handler;
        startScrollLoad();
    }

    private void preloadRows(final List<RowInfo> preloadedRows) {
        for (final RowInfo rowInfo : preloadedRows) {
            getElement().appendChild(createRow(rowInfo));
        }
    }

    private Element createRow(final RowInfo rowInfo) {
        final Element anchor = DOM.createAnchor();
        anchor.setPropertyString("href", rowInfo.url);
        anchor.setClassName(ROW_CLASS_NAME);

        if (rowInfo.isLocked) {
            final Element locked = DOM.createDiv();
            locked.setClassName("locked");
            anchor.appendChild(locked);
        }
        if (rowInfo.isSticky) {
            final Element sticky = DOM.createDiv();
            sticky.setClassName("sticky");
            anchor.appendChild(sticky);
        }

        if (rowInfo.isFollowed) {
            anchor.addClassName("following");
        }

        if (!rowInfo.isRead) {
            anchor.addClassName("unread");
        }

        final Element topic = DOM.createDiv();
        topic.setInnerText(rowInfo.topic);
        topic.setClassName("topic");
        anchor.appendChild(topic);

        final Element startedby = DOM.createDiv();
        startedby.setInnerText(rowInfo.author);
        startedby.setClassName("startedby");
        anchor.appendChild(startedby);

        final Element postcount = DOM.createDiv();
        postcount.setInnerText(String.valueOf(rowInfo.postCount));
        postcount.setClassName("postcount");
        anchor.appendChild(postcount);

        final Element latestpost = DOM.createDiv();
        latestpost.setClassName("latestpost");
        anchor.appendChild(latestpost);

        {
            final Element latesttime = DOM.createDiv();
            latesttime.setClassName("latesttime");
            latestpost.appendChild(latesttime);

            final Element stamp = DOM.createDiv();
            stamp.setInnerText(rowInfo.latestPostDate);
            stamp.setClassName("stamp");
            latesttime.appendChild(stamp);

            final Element pretty = DOM.createDiv();
            pretty.setInnerText(rowInfo.latestPostPretty);
            pretty.setClassName("pretty");
            latesttime.appendChild(pretty);

            final Element latestauthor = DOM.createDiv();
            latestauthor.setInnerText(rowInfo.latestPostAuthor);
            latestauthor.setClassName("latestauthor");
            latestpost.appendChild(latestauthor);
        }

        final Element fadeA = DOM.createDiv();
        fadeA.setClassName("fd a");
        anchor.appendChild(fadeA);

        final Element fadeB = DOM.createDiv();
        fadeB.setClassName("fd b");
        anchor.appendChild(fadeB);

        final Element fadeC = DOM.createDiv();
        fadeC.setClassName("fd c");
        anchor.appendChild(fadeC);

        if (rowInfo.showTools) {
            final Element tools = DOM.createDiv();
            tools.setClassName(CONTEXTMENU_CLASS);
            anchor.appendChild(tools);
            DOM.sinkEvents(tools, Event.ONCLICK);
        }

        return anchor;
    }

    @Override
    public void onBrowserEvent(final Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            final Element target = (Element) Element.as(event.getEventTarget());
            final boolean targetIsContextMenu = target.getClassName().equals(
                    CONTEXTMENU_CLASS);
            if (targetIsContextMenu) {
                final Element threadListingRow = (Element) target
                        .getParentElement();
                openedThreadListingRow = threadListingRow;
                openPopupAt(getBottomLeftCornerOf(target), threadListingRow);
                event.stopPropagation();
                event.preventDefault();
            }

            // closing will be handled by the native preview handler
        }
        super.onBrowserEvent(event);
    }

    private Point getBottomLeftCornerOf(final Element e) {
        if (e.getParentNode() == null) {
            return null;
        }

        final int bottom = e.getOffsetTop() + e.getOffsetHeight();
        return new Point(bottom, e.getOffsetLeft());
    }

    public void openPopupAt(final Point point, final Element popupRow) {
        if (popup == null) {
            popup = DOM.createDiv();
            popup.addClassName(POPUP_CLASS_NAME);

            popup.getStyle().setPosition(Position.ABSOLUTE);
            popup.setInnerText("Loading controls...");
        }

        popup.getStyle().setTop(point.top, Unit.PX);
        popup.getStyle().setLeft(point.left, Unit.PX);
        popupRow.appendChild(popup);

        if (popupCloseHandlerRegistration == null) {
            popupCloseHandlerRegistration = Event
                    .addNativePreviewHandler(new Event.NativePreviewHandler() {
                        @Override
                        public void onPreviewNativeEvent(
                                final NativePreviewEvent event) {
                            if (event.getTypeInt() != Event.ONCLICK) {
                                return;
                            }

                            try {
                                Element controlElement = null;
                                Element e = event.getNativeEvent()
                                        .getEventTarget().cast();
                                while (e != null) {
                                    if (e.getClassName().contains("control")) {
                                        controlElement = e;
                                    }

                                    else if (e.getClassName().equals(
                                            POPUP_CLASS_NAME)) {
                                        handlePopupClick(controlElement);
                                        break;
                                    }
                                    e = (Element) e.getParentElement();
                                }

                                closePopup();
                            } finally {
                                event.cancel();
                            }
                        }
                    });
        }

        fetcher.fetchControlsFor(getRowIndexOf(popupRow));
    }

    private void handlePopupClick(final Element e) {
        final Action action = popupControls.get(e);
        if (action != null) {
            if (popupThreadId != POPUPTHREADID_CLEARED_VALUE) {
                if (action == Action.DELETE
                        && !Window
                                .confirm("Are you sure you want to delete the thread?")) {
                    return;
                }
                rowActionHandler.handle(action, popupThreadId);
            } else {
                VConsole.error("ThreadListingWidget tries to make an action, but popupThreadId is uninitialized");
            }
        }
    }

    private int getRowIndexOf(final Element popupRow) {
        final NodeList<Node> childNodes = getElement().getChildNodes();

        // we skip the first, due to header element.
        for (int i = 1; i < childNodes.getLength(); i++) {
            final Node node = childNodes.getItem(i);
            if (node.equals(popupRow)) {
                return i - 1;
            }
        }
        return -1;
    }

    public void closePopup() {
        if (popup != null && popupIsOpen()) {
            popup.removeFromParent();
            popup.setInnerText("Loading controls...");
            popupCloseHandlerRegistration.removeHandler();
            popupCloseHandlerRegistration = null;
            popupThreadId = POPUPTHREADID_CLEARED_VALUE;
        }
    }

    private boolean popupIsOpen() {
        return popup.getParentElement() != null;
    }

    private void addPlaceholder() {
        final Element placeholderDiv = DOM.createDiv();
        placeholderDiv.setClassName(CLASS_NAME + "-placeholder");
        getElement().appendChild(placeholderDiv);
    }

    @Override
    protected void onUnload() {
        if (scrollHandlerRegistration != null) {
            scrollHandlerRegistration.removeHandler();
        }

        if (scrollHandlerRegistrationWin != null) {
            scrollHandlerRegistrationWin.removeHandler();
        }

        if (popupCloseHandlerRegistration != null) {
            popupCloseHandlerRegistration.removeHandler();
        }

        if (scrollPoller != null) {
            scrollPoller.cancel();
        }

        if (secondaryLoader != null) {
            secondaryLoader.cancel();
        }

        super.onUnload();
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

    private void startScrollLoad() {
        if (!scrollingWasProgrammaticallyAdjusted) {
            secondaryLoader.cancel();
            scrollPoller.cancel();
            scrollPoller.schedule(renderDelay);
        }

        scrollingWasProgrammaticallyAdjusted = false;
    }

    private int getFetchDistancePx() {
        return (int) (pageHeight * distanceMultiplier);
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

    protected void findAllThingsToFetchAndFetchThem() {
        findAllThingsToFetchAndFetchThem(pageHeight);
    }

    private boolean findAllThingsToFetchAndFetchThem(final int distance) {
        final List<Integer> componentsToLoad = new ArrayList<Integer>();

        final long startTime = System.currentTimeMillis();
        boolean shownItemsHaveBeenFound = false;

        /*
         * starting index is 1, since the first element is the header - so we
         * ignore it.
         */
        for (int i = 1; i < getElement().getChildCount(); i++) {
            final Node child = getElement().getChild(i);

            if (child instanceof Element) {
                final Element childElem = (Element) child;
                final boolean isAPlaceholderWidget = childElem.getTagName()
                        .equals(PLACEHOLDER_ELEMENT);

                final boolean beingShown = VisibilityHelper.isBeingShown(
                        childElem, distance);
                if (shownItemsHaveBeenFound && !beingShown) {
                    /*
                     * we previously have seen items, but now they're
                     * disappearing. This needs to mean that we're checking for
                     * elements below the screen.
                     */
                    debug("started for element looking past screen: hopping out of loop");
                    break;
                }

                shownItemsHaveBeenFound = beingShown;

                if (isAPlaceholderWidget && beingShown) {
                    // again, substracting the position of the header element.
                    componentsToLoad.add(i - 1);
                }
            }
        }

        if (!componentsToLoad.isEmpty()) {
            if (fetcher != null) {
                debug("Fetching " + componentsToLoad.size() + " components.");
                fetcher.fetchIndices(componentsToLoad);
            } else {
                VConsole.error("LazyLayout has no fetcher!");
            }
        }

        if (DEBUG) {
            debug("seeked stuff for "
                    + (System.currentTimeMillis() - startTime) + "ms");
        }

        return !componentsToLoad.isEmpty();
    }

    static void debug(final String msg) {
        if (DEBUG) {
            VConsole.log("[ThreadListingWidget] " + msg);
        }
    }

    public void replaceRows(final Map<Integer, RowInfo> rows) {
        for (final Entry<Integer, RowInfo> rowEntry : rows.entrySet()) {
            final Integer canonicalIndex = rowEntry.getKey();
            final RowInfo rowInfo = rowEntry.getValue();

            /*
             * we add one position to the index, since the topmost row is the
             * title row, and we don't want to replace that.
             */

            final int adjustedIndex = canonicalIndex + 1;

            final Node child = getElement().getChild(adjustedIndex);
            if (child instanceof Element) {
                final Element element = (Element) child;
                if (element.getTagName().equals("a")) {
                    debug("Was about to replace index " + canonicalIndex
                            + ", but it was already loaded. Skipping.");
                    continue;
                }

                getElement().removeChild(element);
                final Element rowElement = createRow(rowInfo);

                final Node referenceNode;
                if (adjustedIndex < getElement().getChildCount()) {
                    referenceNode = getElement().getChild(adjustedIndex);
                } else {
                    referenceNode = null;
                }
                getElement().insertBefore(rowElement, referenceNode);
            }
        }
    }

    public void setPopupControls(final ControlInfo controlInfo) {
        popup.setInnerHTML("");
        popupControls.clear();
        for (final Action action : controlInfo.actions) {
            final Element e = getActionElement(action);
            popup.appendChild(e);
            popupThreadId = controlInfo.threadId;
            popupControls.put(e, action);
        }
    }

    private Element getActionElement(final Action action) {
        final Element controlButton = DOM.createDiv();
        controlButton.setClassName("control control-" + action.toCssClass());
        controlButton.setInnerHTML("<div class='" + action.toCssClass()
                + " icon'></div><div>" + action.toCaption() + "</div>");
        return controlButton;
    }

    public void replaceOpenedThreadListingRowWith(final RowInfo rowInfo) {
        if (openedThreadListingRow != null) {
            final Element newRow = createRow(rowInfo);
            openedThreadListingRow.getParentNode().replaceChild(newRow,
                    openedThreadListingRow);
            openedThreadListingRow = null;
        } else {
            VConsole.error("Illegally trying to replace a thread listing row.");
        }
    }

    public void removeSelectedRow() {
        if (openedThreadListingRow != null) {
            openedThreadListingRow.removeFromParent();
            openedThreadListingRow = null;
        } else {
            VConsole.error("Illegally trying to remove a thread listing row.");
        }
    }
}
