package org.vaadin.tori.view.listing.thread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.ComponentUtil;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadData;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadProvider;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingClientRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingServerRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingState.RowInfo;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class ThreadListing extends AbstractComponentContainer implements
        ThreadListingServerRpc {

    private static final int PRELOAD_AMOUNT = 50;
    private int fetchedRows = 0;
    private int totalRows = 0;

    private final Set<Component> components = new HashSet<Component>();

    public ThreadListing(final ThreadListingPresenter presenter) {
        this.presenter = presenter;
        setWidth(100.0f, Unit.PERCENTAGE);
        registerRpc(this);
    }

    // @Override
    // public void handle(final Action action, final long threadId) {
    // switch (action) {
    // case FOLLOW:
    // presenter.follow(threadId);
    // break;
    // case UNFOLLOW:
    // presenter.unfollow(threadId);
    // break;
    // case STICKY:
    // presenter.sticky(threadId);
    // break;
    // case UNSTICKY:
    // presenter.unsticky(threadId);
    // break;
    // case DELETE:
    // ConfirmDialog.show(getUI(),
    // "Are you sure you want to delete the thread?",
    // new ConfirmDialog.Listener() {
    // @Override
    // public void onClose(ConfirmDialog arg0) {
    // if (arg0.isConfirmed()) {
    // removeThreadRow(threadId);
    // presenter.delete(threadId);
    // }
    // }
    // });
    // break;
    // case LOCK:
    // presenter.lock(threadId);
    // break;
    // case UNLOCK:
    // presenter.unlock(threadId);
    // break;
    // case MOVE:
    // presenter.moveRequested(threadId);
    // return;
    // default:
    // throw new IllegalArgumentException(
    // "Unrecognized/unsupported action " + action
    // + " for thread.");
    // }
    // }
    // };

    private ThreadProvider threadProvider;

    private final ThreadListingPresenter presenter;

    // private ControlInfo getControlInfo(final ThreadData thread) {
    // final ControlInfo i = new ControlInfo();
    // long threadId = thread.getId();
    // i.threadId = threadId;
    // List<Action> actions = i.actions;
    //
    // if (thread.mayFollow()) {
    // actions.add(thread.isFollowing() ? Action.UNFOLLOW : Action.FOLLOW);
    // }
    // if (thread.mayLock()) {
    // actions.add(thread.isLocked() ? Action.UNLOCK : Action.LOCK);
    // }
    // if (thread.maySticky()) {
    // actions.add(thread.isSticky() ? Action.UNSTICKY : Action.STICKY);
    // }
    // if (thread.mayDelete()) {
    // actions.add(Action.DELETE);
    // }
    // if (thread.mayMove()) {
    // actions.add(Action.MOVE);
    // }
    // return i;
    // }

    private RowInfo getRowInfo(final ThreadData thread) {
        final RowInfo row = new RowInfo();
        row.author = thread.getAuthor();
        row.isLocked = thread.isLocked();
        row.isSticky = thread.isSticky();
        row.isFollowed = thread.isFollowing();
        row.topic = thread.getTopic();
        row.postCount = thread.getPostCount();
        row.url = "#" + ToriNavigator.ApplicationView.THREADS.getUrl() + "/"
                + thread.getId();
        row.latestPostPretty = new PrettyTime().format(thread
                .getLatestPostTime());

        row.isRead = thread.userHasRead();
        row.settings = buildSettings(thread);
        return row;
    }

    private Component buildSettings(ThreadData thread) {
        MenuBar dropdownMenu = ComponentUtil.getDropdownMenu();
        dropdownMenu.getMoreMenuItem().addItem("test", new Command() {
            @Override
            public void menuSelected(MenuItem selectedItem) {
                System.out.println("fdfw");
            }
        });
        components.add(dropdownMenu);
        addComponent(dropdownMenu);
        return dropdownMenu;
    }

    public void setThreadProvider(final ThreadProvider threadProvider) {
        this.threadProvider = threadProvider;
        totalRows = threadProvider.getThreadCount();
        fetchRows();
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
        getRpcProxy(ThreadListingClientRpc.class)
                .refreshRow(getRowInfo(thread));
    }

    public void removeThreadRow(long threadId) {
        // remove all information about the row
        // final Integer rowIndex = threadIdToRowIndex.remove(threadId);
        // getRpcProxy(ThreadListingClientRpc.class).removeRow(rowIndex);
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        // Ignore
    }

    @Override
    public int getComponentCount() {
        return components.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return components.iterator();
    }

    @Override
    public void fetchRows() {
        final List<ThreadData> threads = threadProvider.getThreadsBetween(
                fetchedRows, fetchedRows + PRELOAD_AMOUNT);

        final ArrayList<RowInfo> rows = new ArrayList<RowInfo>();

        for (final ThreadData thread : threads) {
            rows.add(getRowInfo(thread));
        }
        fetchedRows += rows.size();
        int remaining = totalRows - fetchedRows;
        int placeholders = Math.min(remaining, PRELOAD_AMOUNT);
        getRpcProxy(ThreadListingClientRpc.class).sendRows(rows, placeholders);
    }
}
