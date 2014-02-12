package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;

public class ThreadListingWidget extends FlowPanel {

    private static final String CLASS_NAME = "threadlisting";
    private static final String ROW_CLASS_NAME = "threadlistingrow";

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration scrollHandlerRegistrationWin;

    private final FlowPanel placeHolders = new FlowPanel();
    private final Map<Long, FlowPanel> threadRows = new HashMap<Long, FlowPanel>();

    public interface Fetcher {
        void fetchRows();
    }

    private final int renderDelay = 500;
    private int pageHeight;
    private final double distanceMultiplier = 1.0;

    private Fetcher fetcher;
    private boolean fetching;

    public ThreadListingWidget() {
        setStyleName("threadlisting");
        setWidth("100%");
    }

    public void init(final Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    public void addRows(List<RowInfo> rows, int placeholders) {

        remove(placeHolders);
        for (RowInfo rowInfo : rows) {
            FlowPanel newRow = createRow(rowInfo);
            add(newRow);
            threadRows.put(rowInfo.threadId, newRow);
        }
        while (placeHolders.getWidgetCount() > 0
                && placeHolders.getWidgetCount() > placeholders) {
            placeHolders.remove(0);
        }
        while (placeHolders.getWidgetCount() < placeholders) {
            Label placeHolder = new Label();
            placeHolder.addStyleName(CLASS_NAME + "-placeholder");
            placeHolders.add(placeHolder);
        }
        add(placeHolders);
        fetching = false;
    }

    private FlowPanel createRow(final RowInfo rowInfo) {
        FlowPanel panel = new FlowPanel();
        populateRow(panel, rowInfo);
        return panel;
    }

    @Override
    protected void onUnload() {
        if (scrollHandlerRegistration != null) {
            scrollHandlerRegistration.removeHandler();
        }
        if (scrollHandlerRegistrationWin != null) {
            scrollHandlerRegistrationWin.removeHandler();
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
                    checkNewRowsNeeded();
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
                    checkNewRowsNeeded();
                }

            };
            scrollHandlerRegistrationWin = Window
                    .addWindowScrollHandler(handler);
        }
    }

    private void checkNewRowsNeeded() {
        if (!fetching && placeHolders.getWidgetCount() > 0
                && isElementInViewport(placeHolders.getElement())) {
            fetching = true;
            fetcher.fetchRows();
        }
    }

    private int getFetchDistancePx() {
        return (int) (pageHeight * distanceMultiplier);
    }

    public void refreshRow(final RowInfo rowInfo) {
        FlowPanel threadRow = threadRows.get(rowInfo.threadId);
        threadRow.setStyleName("");
        threadRow.clear();
        populateRow(threadRow, rowInfo);
    }

    private void populateRow(FlowPanel panel, RowInfo rowInfo) {
        panel.setWidth("100%");
        panel.addStyleName(ROW_CLASS_NAME);

        if (rowInfo.isLocked) {
            panel.addStyleName("locked");
        }
        if (rowInfo.isSticky) {
            panel.addStyleName("sticky");
        }
        if (rowInfo.isFollowed) {
            panel.addStyleName("following");
        }
        if (!rowInfo.isRead) {
            panel.addStyleName("unread");
        }

        Anchor anchor = new Anchor(rowInfo.topic, rowInfo.url);
        anchor.addStyleName("topic");
        panel.add(anchor);

        Label startedBy = new Label(rowInfo.author);
        startedBy.addStyleName("startedby");
        panel.add(startedBy);

        String postCount = rowInfo.postCount != 0 ? String
                .valueOf(rowInfo.postCount) : "";
        Label postCountWidget = new Label(postCount);
        postCountWidget.addStyleName("postcount");
        panel.add(postCountWidget);

        Label latest = new Label(rowInfo.latestPostPretty);
        latest.addStyleName("latesttime");
        panel.add(latest);

        if (rowInfo.settings != null) {
            Widget settings = ((AbstractComponentConnector) rowInfo.settings)
                    .getWidget();
            panel.add(settings);
        }
    }

    public void removeThreadRow(long threadId) {
        remove(threadRows.get(threadId));
    }

    private static native boolean isElementInViewport(Element el)
    /*-{
        return (
            el.getBoundingClientRect().top <= ($wnd.innerHeight || $doc.documentElement.clientHeight)
        );
    }-*/;

}
