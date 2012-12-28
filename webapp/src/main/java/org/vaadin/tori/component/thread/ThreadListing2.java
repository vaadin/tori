package org.vaadin.tori.component.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.category.CategoryView.ThreadProvider;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingClientRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingServerRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;

public class ThreadListing2 extends AbstractComponent {

    private static final int PRELOAD_AMOUNT = 50;

    ThreadListingServerRpc rpc = new ThreadListingServerRpc() {
        @Override
        public void fetchComponentsForIndices(final List<Integer> indicesToFetch) {
            try {
                final Map<Integer, RowInfo> map = new HashMap<Integer, RowInfo>();

                Collections.sort(indicesToFetch);
                for (final int[] range : groupToRanges(indicesToFetch)) {
                    final int from = range[0];
                    final int to = range[1];

                    int i = from;
                    for (final DiscussionThread thread : threadProvider
                            .getThreadsBetween(from, to)) {
                        map.put(i, getRowInfo(thread));

                        final ControlInfo controlInfo = getControlInfo(thread);
                        if (controlInfo != null) {
                            controlInfoMap.put(i, controlInfo);
                        }

                        i++;
                    }
                }

                getLogger().debug("Sending " + map.size() + " rows");
                getRpcProxy(ThreadListingClientRpc.class).sendComponents(map);

            } catch (final DataSourceException e) {
                showError(e);
            }
        }

        @Override
        public void fetchControlsForIndex(final int rowIndex) {
            getRpcProxy(ThreadListingClientRpc.class).sendControls(
                    controlInfoMap.get(rowIndex));
        }
    };

    private final ThreadProvider threadProvider;

    private final CategoryPresenter presenter;

    private final Map<Integer, ControlInfo> controlInfoMap = new HashMap<Integer, ControlInfo>();

    private ControlInfo getControlInfo(final DiscussionThread thread)
            throws DataSourceException {
        final ControlInfo i = new ControlInfo();
        i.follow = presenter.userCanFollow(thread);
        i.unfollow = presenter.userCanUnFollow(thread);
        i.lock = presenter.userCanLock(thread);
        i.unlock = presenter.userCanUnLock(thread);
        i.sticky = presenter.userCanSticky(thread);
        i.unsticky = presenter.userCanUnSticky(thread);
        i.delete = presenter.userMayDelete(thread);
        i.move = presenter.userMayMove(thread);
        return i;
    }

    private RowInfo getRowInfo(final DiscussionThread thread) {
        final RowInfo row = new RowInfo();
        row.author = thread.getOriginalPoster().getDisplayedName();
        row.isLocked = thread.isLocked();
        row.isSticky = thread.isSticky();
        row.isFollowed = presenter.userIsFollowing(thread);
        row.topic = thread.getTopic();
        row.postCount = thread.getPostCount();
        row.url = "#" + ToriNavigator.ApplicationView.THREADS.getUrl() + "/"
                + thread.getId();
        row.latestPostAuthor = thread.getLatestPost().getAuthor()
                .getDisplayedName();
        row.latestPostDate = String.format("%1$td.%1$tm.%1$tY", thread
                .getLatestPost().getTime());
        row.latestPostPretty = new PrettyTime().format(thread.getLatestPost()
                .getTime());

        row.showTools = presenter.mayShowToolsFor(thread);
        row.isRead = presenter.userHasRead(thread);

        return row;
    }

    public ThreadListing2(final CategoryPresenter presenter,
            final ThreadProvider threadProvider) {
        this.presenter = presenter;
        this.threadProvider = threadProvider;

        registerRpc(rpc);

        preloadRows();
        try {
            getState().rows = threadProvider.getThreadAmount();
        } catch (final DataSourceException e) {
            showError(e);
        }
    }

    private void preloadRows() {
        try {
            final List<DiscussionThread> threads = threadProvider
                    .getThreadsBetween(0, PRELOAD_AMOUNT);
            final ArrayList<RowInfo> rows = new ArrayList<RowInfo>();

            int i = 0;
            for (final DiscussionThread thread : threads) {
                rows.add(getRowInfo(thread));
                controlInfoMap.put(i++, getControlInfo(thread));
            }
            getState().preloadedRows = rows;
        } catch (final DataSourceException e) {
            showError(e);
        }
    }

    private void showError(final DataSourceException e) {
        Notification.show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE,
                Notification.Type.ERROR_MESSAGE);
        e.printStackTrace();
        getLogger().error(e);
    }

    private Logger getLogger() {
        return Logger.getLogger(getClass());
    }

    private static List<int[]> groupToRanges(final List<Integer> orderedNumbers) {
        final List<int[]> ranges = new ArrayList<int[]>();

        if (!orderedNumbers.isEmpty()) {
            int start = orderedNumbers.get(0);
            int previous = start;

            for (final int num : orderedNumbers) {
                if (num > previous + 1) {
                    ranges.add(new int[] { start, previous });
                    start = num;
                }
                previous = num;
            }
            ranges.add(new int[] { start, previous });
        }

        return ranges;
    }

    @Override
    protected ThreadListingState getState() {
        return (ThreadListingState) super.getState();
    }
}
