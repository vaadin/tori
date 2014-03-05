package org.vaadin.tori.widgetset.client.ui.threadlisting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadAdditionalData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadPrimaryData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingRow.ThreadListingRowListener;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ThreadListingWidget extends FlowPanel {

    private static final String CLASS_NAME = "threadlisting";
    private static final String ROW_CLASS_NAME = "threadlistingrow";

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration scrollHandlerRegistrationWin;

    private final FlowPanel placeHolders = new FlowPanel();
    private final Map<Long, ThreadListingRow> threadRows = new HashMap<Long, ThreadListingRow>();

    public interface Fetcher {
        void fetchRows();
    }

    private ThreadListingRowListener threadListingRowListener;
    private Fetcher fetcher;
    private boolean fetching;
    private Timer timer;

    private static final int DELAY = 200;

    public ThreadListingWidget() {
        setStyleName("threadlisting");
        setWidth("100%");
    }

    public void init(final Fetcher fetcher,
            final ThreadListingRowListener threadListingRowListener) {
        this.fetcher = fetcher;
        this.threadListingRowListener = threadListingRowListener;
    }

    public void addRows(final List<ThreadPrimaryData> rows,
            final int placeholders) {

        remove(placeHolders);
        if (rows != null) {
            for (ThreadPrimaryData rowInfo : rows) {
                ThreadListingRow newRow = new ThreadListingRow(rowInfo,
                        threadListingRowListener);
                add(newRow);
                threadRows.put(rowInfo.threadId, newRow);
            }
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
        checkNewRowsNeeded();
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
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    if (!fetching && placeHolders.getWidgetCount() > 0
                            && isElementInViewport(placeHolders.getElement())) {
                        fetching = true;
                        fetcher.fetchRows();
                    }
                    timer.cancel();
                    timer = null;
                }
            };
            timer.schedule(DELAY);
        }
    }

    public void removeThreadRow(final long threadId) {
        remove(threadRows.get(threadId));
    }

    private static native boolean isElementInViewport(final Element el)
    /*-{
        return (
            el.getBoundingClientRect().top <= ($wnd.innerHeight || $doc.documentElement.clientHeight)
        );
    }-*/;

    public void refreshRows(final List<ThreadAdditionalData> rows) {
        for (ThreadAdditionalData data : rows) {
            threadRows.get(data.threadId).updateRowInfo(data);
        }
    }
}
