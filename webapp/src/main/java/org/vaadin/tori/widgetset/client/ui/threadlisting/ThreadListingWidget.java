package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.VConsole;

public class ThreadListingWidget extends Widget {

    private static final boolean DEBUG = true;

    private static final String CLASS_NAME = "threadlisting";
    private static final String ROW_CLASS_NAME = "threadlistingrow";

    private static final String PLACEHOLDER_ELEMENT = "DIV";

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

    public interface ComponentFetcher {
        void fetchIndices(List<Integer> indicesToFetch);
    }

    private final SecondaryFetchTimer secondaryLoader = new SecondaryFetchTimer();
    private final int renderDelay = 500;
    private int pageHeight;
    private final double distanceMultiplier = 1.0;

    private ComponentFetcher fetcher;

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
            final ComponentFetcher fetcher) {
        if (rows < 0) {
            throw new IllegalArgumentException("Row amount can't be negative");
        }

        preloadRows(preloadedRows);

        for (int i = preloadedRows.size() + 1; i <= rows; i++) {
            addPlaceholder();
        }

        refreshPageHeight();
        this.fetcher = fetcher;
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

        // TODO: Locked
        // TODO: Sticky

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

        return anchor;
    }

    private void addPlaceholder() {
        final Element placeholderDiv = DOM.createDiv();
        placeholderDiv.setClassName(CLASS_NAME + "-placeholder");
        getElement().appendChild(placeholderDiv);
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

        /*
         * TODO: optimize (give up something is first being shown, and then
         * suddenly stops being shown
         */

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
                if (isAPlaceholderWidget && isBeingShown(childElem, distance)) {
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

        return !componentsToLoad.isEmpty();
    }

    static void debug(final String msg) {
        if (DEBUG) {
            VConsole.error("[ThreadListingWidget] " + msg);
        }
    }

    private static boolean isBeingShown(final Element element,
            final int proximity) {

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
}
