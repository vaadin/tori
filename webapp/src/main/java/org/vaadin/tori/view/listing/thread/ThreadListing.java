package org.vaadin.tori.view.listing.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadData;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadProvider;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingClientRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingServerRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.ControlInfo.Action;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;

public class ThreadListing extends AbstractComponent {

    private static final int PRELOAD_AMOUNT = 50;

    ThreadListingServerRpc rpc = new ThreadListingServerRpc() {
        @Override
        public void fetchComponentsForIndices(final List<Integer> indicesToFetch) {
            final Map<Integer, RowInfo> map = new HashMap<Integer, RowInfo>();

            Collections.sort(indicesToFetch);
            for (final int[] range : groupToRanges(indicesToFetch)) {
                final int from = range[0];
                final int to = range[1];

                int i = from;
                for (final ThreadData thread : threadProvider
                        .getThreadsBetween(from, to)) {

                    final ControlInfo controlInfo = getControlInfo(thread);
                    if (controlInfo != null) {
                        controlInfoMap.put(i, controlInfo);
                    }
                    map.put(i,
                            getRowInfo(thread, !controlInfo.actions.isEmpty()));

                    i++;
                }
            }

            getLogger().debug("Sending " + map.size() + " rows");
            getRpcProxy(ThreadListingClientRpc.class).sendComponents(map);
        }

        @Override
        public void fetchControlsForIndex(final int rowIndex) {
            getRpcProxy(ThreadListingClientRpc.class).sendControls(
                    controlInfoMap.get(rowIndex));
        }

        @Override
        public void handle(final Action action, final long threadId) {
            switch (action) {
            case FOLLOW:
                presenter.follow(threadId);
                break;
            case UNFOLLOW:
                presenter.unfollow(threadId);
                break;
            case STICKY:
                presenter.sticky(threadId);
                break;
            case UNSTICKY:
                presenter.unsticky(threadId);
                break;
            case DELETE:
                ConfirmDialog.show(getUI(),
                        "Are you sure you want to delete the thread?",
                        new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(ConfirmDialog arg0) {
                                if (arg0.isConfirmed()) {
                                    removeThreadRow(threadId);
                                    presenter.delete(threadId);
                                }
                            }
                        });
                break;
            case LOCK:
                presenter.lock(threadId);
                break;
            case UNLOCK:
                presenter.unlock(threadId);
                break;
            case MOVE:
                presenter.moveRequested(threadId);
                return;
            default:
                throw new IllegalArgumentException(
                        "Unrecognized/unsupported action " + action
                                + " for thread.");
            }
        }
    };

    private ThreadProvider threadProvider;

    private final ThreadListingPresenter presenter;

    private final Map<Long, Integer> threadIdToRowIndex = new HashMap<Long, Integer>();

    /** Row placement index number &rarr; control info */
    private final Map<Integer, ControlInfo> controlInfoMap = new HashMap<Integer, ControlInfo>();

    private ControlInfo getControlInfo(final ThreadData thread) {
        final ControlInfo i = new ControlInfo();
        long threadId = thread.getId();
        i.threadId = threadId;
        List<Action> actions = i.actions;

        if (thread.mayFollow()) {
            actions.add(thread.isFollowing() ? Action.UNFOLLOW : Action.FOLLOW);
        }
        if (thread.mayLock()) {
            actions.add(thread.isLocked() ? Action.UNLOCK : Action.LOCK);
        }
        if (thread.maySticky()) {
            actions.add(thread.isSticky() ? Action.UNSTICKY : Action.STICKY);
        }
        if (thread.mayDelete()) {
            actions.add(Action.DELETE);
        }
        if (thread.mayMove()) {
            actions.add(Action.MOVE);
        }
        return i;
    }

    private RowInfo getRowInfo(final ThreadData thread, boolean hasControls) {
        final RowInfo row = new RowInfo();
        row.author = thread.getAuthor();
        row.isLocked = thread.isLocked();
        row.isSticky = thread.isSticky();
        row.isFollowed = thread.isFollowing();
        row.topic = thread.getTopic();
        row.postCount = thread.getPostCount();
        row.url = "#" + ToriNavigator.ApplicationView.THREADS.getUrl() + "/"
                + thread.getId();
        row.latestPostAuthor = thread.getLatestPostAuthor();
        row.latestPostDate = String.format("%1$td.%1$tm.%1$tY",
                thread.getLatestPostTime());
        row.latestPostPretty = new PrettyTime().format(thread
                .getLatestPostTime());

        row.showTools = hasControls;
        row.isRead = thread.userHasRead();

        return row;
    }

    public ThreadListing(final ThreadListingPresenter presenter) {
        this.presenter = presenter;
        registerRpc(rpc);

    }

    public void setThreadProvider(final ThreadProvider threadProvider) {
        this.threadProvider = threadProvider;

        preloadRows();
        getState().rows = threadProvider.getThreadCount();
    }

    private void preloadRows() {
        final List<ThreadData> threads = threadProvider.getThreadsBetween(0,
                PRELOAD_AMOUNT);
        final ArrayList<RowInfo> rows = new ArrayList<RowInfo>();

        int i = 0;
        for (final ThreadData thread : threads) {
            ControlInfo controlInfo = getControlInfo(thread);
            rows.add(getRowInfo(thread, !controlInfo.actions.isEmpty()));
            controlInfoMap.put(i, controlInfo);
            threadIdToRowIndex.put(thread.getId(), i);
            i++;
        }
        getState().preloadedRows = rows;
    }

    private void showError(final DataSourceException e) {
        Notification.show(DataSourceException.GENERIC_ERROR_MESSAGE,
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

    public void updateThreadRow(ThreadData thread) {
        // replace the row with new metadata
        final Integer rowIndex = threadIdToRowIndex.get(thread.getId());
        ControlInfo controlInfo = getControlInfo(thread);
        controlInfoMap.put(rowIndex, controlInfo);
        getRpcProxy(ThreadListingClientRpc.class).refreshSelectedRowAs(
                getRowInfo(thread, !controlInfo.actions.isEmpty()));
    }

    public void removeThreadRow(long threadId) {
        // remove all information about the row
        final Integer rowIndex = threadIdToRowIndex.remove(threadId);
        controlInfoMap.remove(rowIndex);
        getRpcProxy(ThreadListingClientRpc.class).removeSelectedRow();
    }
}
