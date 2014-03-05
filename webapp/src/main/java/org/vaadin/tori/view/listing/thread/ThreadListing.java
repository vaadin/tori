package org.vaadin.tori.view.listing.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ocpsoft.prettytime.PrettyTime;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadData;
import org.vaadin.tori.view.listing.thread.ThreadListingView.ThreadProvider;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadAdditionalData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadData.ThreadPrimaryData;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingClientRpc;
import org.vaadin.tori.widgetset.client.ui.threadlisting.ThreadListingServerRpc;

import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

@SuppressWarnings("serial")
public class ThreadListing extends AbstractComponentContainer implements
        ThreadListingServerRpc {

    private static final String FOLLOW_CAPTION = "Follow Topic";
    private static final String UNFOLLOW_CAPTION = "Unfollow Topic";
    private static final String STICKY_CAPTION = "Pin Topic";
    private static final String UNSTICKY_CAPTION = "Unpin Topic";
    private static final String LOCK_CAPTION = "Lock Topic";
    private static final String UNLOCK_CAPTION = "Unlock Topic";
    private static final String MOVE_CAPTION = "Move Topic...";
    private static final String DELETE_CAPTION = "Delete Topic...";

    private static final int PRELOAD_AMOUNT = 50;
    private int fetchedRows = 0;
    private int totalRows = 0;

    private final Set<Component> components = new HashSet<Component>();

    public ThreadListing(final ThreadListingPresenter presenter) {
        this.presenter = presenter;
        setWidth(100.0f, Unit.PERCENTAGE);
        registerRpc(this);
    }

    private ThreadProvider threadProvider;
    private final ThreadListingPresenter presenter;

    private ThreadPrimaryData getThreadPrimaryData(final ThreadData thread) {
        final ThreadPrimaryData data = new ThreadPrimaryData();
        data.author = thread.getAuthor();
        data.latestPostPretty = new PrettyTime().format(thread
                .getLatestPostTime());
        data.postCount = thread.getPostCount();
        data.threadId = thread.getId();
        data.topic = thread.getTopic();
        return data;
    }

    private ThreadAdditionalData getThreadAdditionalData(final ThreadData thread) {
        final ThreadAdditionalData data = new ThreadAdditionalData();
        data.threadId = thread.getId();
        data.isLocked = thread.isLocked();
        data.isSticky = thread.isSticky();
        data.isFollowed = thread.isFollowing();
        data.mayFollow = thread.mayFollow();
        data.url = "#" + ToriNavigator.ApplicationView.THREADS.getUrl() + "/"
                + thread.getId();
        data.latestPostUrl = data.url + "/" + thread.getLatestPostId();
        data.latestAuthor = thread.getLatestPostAuthor();
        data.isRead = thread.userHasRead();
        data.settings = buildSettings(thread);
        return data;
    }

    private Command getSettingsCommand(final long threadId) {
        return new Command() {
            @Override
            public void menuSelected(final MenuItem selectedItem) {
                if (FOLLOW_CAPTION.equals(selectedItem.getText())) {
                    presenter.follow(threadId);
                } else if (UNFOLLOW_CAPTION.equals(selectedItem.getText())) {
                    presenter.unfollow(threadId);
                } else if (STICKY_CAPTION.equals(selectedItem.getText())) {
                    presenter.sticky(threadId);
                } else if (UNSTICKY_CAPTION.equals(selectedItem.getText())) {
                    presenter.unsticky(threadId);
                } else if (LOCK_CAPTION.equals(selectedItem.getText())) {
                    presenter.lock(threadId);
                } else if (UNLOCK_CAPTION.equals(selectedItem.getText())) {
                    presenter.unlock(threadId);
                } else if (MOVE_CAPTION.equals(selectedItem.getText())) {
                    presenter.moveRequested(threadId);
                } else if (DELETE_CAPTION.equals(selectedItem.getText())) {
                    ConfirmDialog dialog = ConfirmDialog.show(getUI(),
                            "Are you sure you want to delete the topic?",
                            new ConfirmDialog.Listener() {
                                @Override
                                public void onClose(final ConfirmDialog arg0) {
                                    if (arg0.isConfirmed()) {
                                        removeThreadRow(threadId);
                                        presenter.delete(threadId);
                                    }
                                }
                            });
                    dialog.getOkButton().setCaption("Delete Topic");
                }
            }
        };
    }

    private Component buildSettings(final ThreadData thread) {
        Command settingsCommand = getSettingsCommand(thread.getId());
        MenuBar dropdownMenu = ComponentUtil.getDropdownMenu();
        MenuItem rootItem = dropdownMenu.getMoreMenuItem();
        if (thread.mayFollow()) {
            rootItem.addItem(thread.isFollowing() ? UNFOLLOW_CAPTION
                    : FOLLOW_CAPTION, settingsCommand);
        }
        MenuItem separator = null;
        if (rootItem.hasChildren()) {
            separator = rootItem.addSeparator();
        }
        if (thread.mayLock()) {
            rootItem.addItem(thread.isLocked() ? UNLOCK_CAPTION : LOCK_CAPTION,
                    settingsCommand);
        }
        if (thread.maySticky()) {
            rootItem.addItem(thread.isSticky() ? UNSTICKY_CAPTION
                    : STICKY_CAPTION, settingsCommand);
        }
        if (thread.mayDelete()) {
            rootItem.addItem(DELETE_CAPTION, settingsCommand);
        }
        if (thread.mayMove()) {
            rootItem.addItem(MOVE_CAPTION, settingsCommand);
        }
        if (separator != null && rootItem.getChildren().size() == 2) {
            rootItem.removeChild(separator);
        }

        Component result = null;
        if (rootItem.hasChildren()) {
            components.add(dropdownMenu);
            addComponent(dropdownMenu);
            result = dropdownMenu;
        }
        return result;
    }

    public void setThreadProvider(final ThreadProvider threadProvider) {
        this.threadProvider = threadProvider;
        totalRows = threadProvider.getThreadCount();
        getRpcProxy(ThreadListingClientRpc.class).sendRows(null,
                Math.min(totalRows, PRELOAD_AMOUNT));
    }

    public void updateThreadRow(final ThreadData thread) {
        getRpcProxy(ThreadListingClientRpc.class).refreshThreadRows(
                Arrays.asList(getThreadAdditionalData(thread)));
    }

    public void removeThreadRow(final long threadId) {
        getRpcProxy(ThreadListingClientRpc.class).removeThreadRow(threadId);
    }

    @Override
    public void replaceComponent(final Component oldComponent,
            final Component newComponent) {
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

        final ArrayList<ThreadPrimaryData> rows = new ArrayList<ThreadPrimaryData>();

        for (final ThreadData thread : threads) {
            rows.add(getThreadPrimaryData(thread));
        }

        fetchedRows += rows.size();
        int remaining = totalRows - fetchedRows;
        int placeholders = Math.min(remaining, PRELOAD_AMOUNT);
        getRpcProxy(ThreadListingClientRpc.class).sendRows(rows, placeholders);

        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                final ArrayList<ThreadAdditionalData> rows = new ArrayList<ThreadAdditionalData>();
                for (final ThreadData thread : threads) {
                    rows.add(getThreadAdditionalData(thread));
                }
                getRpcProxy(ThreadListingClientRpc.class).refreshThreadRows(
                        rows);
            }
        });
    }

    @Override
    public void follow(final long threadId, final boolean follow) {
        if (follow) {
            presenter.follow(threadId);
        } else {
            presenter.unfollow(threadId);
        }
    }
}
