package org.vaadin.tori.component.thread;

import java.util.Date;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.MenuPopup;
import org.vaadin.tori.component.MenuPopup.ContextAction;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;

@SuppressWarnings("serial")
public class ThreadListingRow extends PopupView {

    private static final String FOLLOW_CAPTION = "Follow thread";
    private static final Resource FOLLOW_ICON = new ThemeResource(
            "images/icon-follow.png");

    private static final String UNFOLLOW_CAPTION = "Unfollow thread";
    private static final Resource UNFOLLOW_ICON = new ThemeResource(
            "images/icon-unfollow.png");

    private static final String STICKY_CAPTION = "Make thread sticky";
    private static final Resource STICKY_ICON = new ThemeResource(

    "images/icon-sticky.png");
    private static final String UNSTICKY_CAPTION = "Remove thread stickiness";
    private static final Resource UNSTICKY_ICON = new ThemeResource(
            "images/icon-unsticky.png");

    private static final String LOCK_CAPTION = "Lock thread";
    private static final Resource LOCK_ICON = new ThemeResource(
            "images/icon-lock.png");

    private static final String UNLOCK_CAPTION = "Unlock thread";
    private static final Resource UNLOCK_ICON = new ThemeResource(
            "images/icon-unlock.png");

    private static final String STYLE_OPEN = "open";

    private class FollowAction implements ContextAction {
        @Override
        public void contextClicked() {
            try {
                presenter.follow(thread);
                menu.swap(this, UNFOLLOW_ICON, UNFOLLOW_CAPTION,
                        new UnfollowAction(thread));
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }
        }
    }

    private class UnfollowAction implements ContextAction {
        private final DiscussionThread thread;

        public UnfollowAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            try {
                presenter.unfollow(thread);
                menu.swap(this, FOLLOW_ICON, FOLLOW_CAPTION, new FollowAction());
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }
        }
    }

    private class StickyAction implements ContextAction {
        private final DiscussionThread thread;

        public StickyAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            try {
                presenter.sticky(thread);
                menu.swap(this, UNSTICKY_ICON, UNSTICKY_CAPTION,
                        new UnstickyAction(thread));
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }

        }
    }

    private class UnstickyAction implements ContextAction {
        private final DiscussionThread thread;

        public UnstickyAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            try {
                presenter.unsticky(thread);
                menu.swap(this, STICKY_ICON, STICKY_CAPTION, new StickyAction(
                        thread));
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }

        }
    }

    private class LockAction implements ContextAction {
        private final DiscussionThread thread;

        public LockAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            try {
                presenter.lock(thread);
                menu.swap(this, UNLOCK_ICON, UNLOCK_CAPTION, new UnlockAction(
                        thread));
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }

        }
    }

    private class UnlockAction implements ContextAction {
        private final DiscussionThread thread;

        public UnlockAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            try {
                presenter.unlock(thread);
                menu.swap(this, LOCK_ICON, LOCK_CAPTION, new LockAction(thread));
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }

        }
    }

    private class RowContent implements Content {

        private String startedBy;
        private int postCount;
        private String topic;
        private boolean sticky;
        private boolean locked;
        private Date time;
        private String latestAuthor;
        private long threadId;

        @Override
        public String getMinimizedValueAsHTML() {
            final String locked = this.locked ? "<div class='locked'></div>"
                    : "";
            final String sticky = this.sticky ? "<div class='sticky'></div>"
                    : "";
            final String topic = "<div class='topic'>"
                    + ToriUtil.escapeXhtml(this.topic) + "</div>";
            final String startedBy = "<div class='startedBy'>"
                    + ToriUtil.escapeXhtml(this.startedBy) + "</div>";
            final String postCount = "<div class='postcount'>" + this.postCount
                    + "</div>";
            final String latestAuthor = "<div class='latestauthor'>"
                    + ToriUtil.escapeXhtml(this.latestAuthor) + "</div>";
            final String time = String.format("<div class='latesttime'>"
                    + "<span class='stamp'>%1$td.%1$tm.%1$tY</span>"
                    + "<span class='pretty'>%2$s</span></div>", this.time,
                    new PrettyTime().format(this.time));

            final String url = "#"
                    + ToriNavigator.ApplicationView.THREADS.getUrl() + "/"
                    + threadId;

            final String contextMenu = menu.hasItems() ? "<div class='menutrigger'></div>"
                    : "";

            return String
                    .format("<a href='%s'>%s %s %s %s %s <div class='latestpost'>%s %s</div></a>%s",
                            url, locked, sticky, topic, startedBy, postCount,
                            time, latestAuthor, contextMenu);
        }

        @Override
        public Component getPopupComponent() {
            return menu;
        }

        public void setStartedBy(final String displayedName) {
            this.startedBy = displayedName;
        }

        public void setPostAmount(final int postCount) {
            this.postCount = postCount;
        }

        public void setTopic(final String topic) {
            this.topic = topic;
        }

        public void isSticky(final boolean sticky) {
            this.sticky = sticky;
        }

        public void isLocked(final boolean locked) {
            this.locked = locked;
        }

        public void setLatestPostDate(final Date time) {
            this.time = time;
        }

        public void setLatestPostName(final String latestAuthor) {
            this.latestAuthor = latestAuthor;
        }

        public void setThreadId(final long threadId) {
            this.threadId = threadId;
        }

    }

    private final DiscussionThread thread;
    private final CategoryPresenter presenter;

    private MenuPopup menu;

    public ThreadListingRow(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        super("", new Label());
        this.presenter = presenter;
        this.thread = thread;

        if (!presenter.userHasRead(thread)) {
            addStyleName("unread");
        }

        final RowContent content = new RowContent();

        initializePreview(thread, content);
        menu = getNewMenuPopup(thread, presenter);

        setContent(content);

        super.setHideOnMouseOut(false);
        super.addListener(new PopupVisibilityListener() {
            private boolean isBeingReloaded;

            @Override
            public void popupVisibilityChange(final PopupVisibilityEvent event) {
                if (event.isPopupVisible()) {
                    /*
                     * This is a big nasty workaround for Vaadin's PopupView bug
                     * (affects ThreadListingRow, since it's a fork of
                     * PopupView) where components' connectors are lost when the
                     * PopupView is opened the second time, and the fact that it
                     * doesn't support .replaceComponent().
                     * 
                     * Instead, we set a new content to the PopupButton whenever
                     * it's opened. But, since this event is called _after_ the
                     * menu is being displayed, we need to do this so that the
                     * right component is shown in the browser.
                     */
                    if (!isBeingReloaded) {
                        menu = getNewMenuPopup(thread, presenter);
                        isBeingReloaded = true;

                        // this is the hack above talks about.
                        setPopupVisible(false);
                        setPopupVisible(true);
                    } else {
                        isBeingReloaded = false;
                    }
                    addStyleName(STYLE_OPEN);
                } else {
                    removeStyleName(STYLE_OPEN);
                }
            }
        });
    }

    @Override
    public void addStyleName(final String style) {
        super.addStyleName(style);
    }

    @Override
    public void removeStyleName(final String style) {
        super.removeStyleName(style);
    }

    /**
     * Fills the popup menu with all the appropriate menu items. If there's an
     * error, an error message will be displayed within the popup menu.
     */
    private MenuPopup getNewMenuPopup(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        MenuPopup menu;
        try {
            menu = createContextMenu(thread, presenter);
        } catch (final DataSourceException e) {
            menu = new MenuPopup();
            menu.add(null, DataSourceException.BORING_GENERIC_ERROR_MESSAGE,
                    ContextAction.NULL);
        }
        requestRepaint();
        return menu;
    }

    private MenuPopup createContextMenu(final DiscussionThread thread,
            final CategoryPresenter presenter) throws DataSourceException {
        final MenuPopup menu = new MenuPopup();

        if (presenter.userCanFollow(thread)) {
            menu.add(FOLLOW_ICON, FOLLOW_CAPTION, new FollowAction());
        } else if (presenter.userCanUnFollow(thread)) {
            menu.add(UNFOLLOW_ICON, UNFOLLOW_CAPTION,
                    new UnfollowAction(thread));
        }

        if (presenter.userMayMove(thread)) {
            menu.add(new ThemeResource("images/icon-move.png"), "Move thread",
                    new ContextComponentSwapper() {
                        @Override
                        public Component swapContextComponent() {
                            return new ThreadMoveComponent(thread,
                                    ThreadListingRow.this, presenter);
                        }
                    });
        }

        if (presenter.userCanSticky(thread)) {
            menu.add(STICKY_ICON, STICKY_CAPTION, new StickyAction(thread));
        } else if (presenter.userCanUnSticky(thread)) {
            menu.add(UNSTICKY_ICON, UNSTICKY_CAPTION,
                    new UnstickyAction(thread));
        }

        if (presenter.userCanLock(thread)) {
            menu.add(LOCK_ICON, LOCK_CAPTION, new LockAction(thread));
        } else if (presenter.userCanUnLock(thread)) {
            menu.add(UNLOCK_ICON, UNLOCK_CAPTION, new UnlockAction(thread));
        }

        if (presenter.userMayDelete(thread)) {
            menu.add(new ThemeResource("images/icon-delete.png"),
                    "Delete thread", new ContextAction() {
                        @Override
                        public void contextClicked() {
                            try {
                                presenter.delete(thread);
                            } catch (final DataSourceException e) {
                                getRoot()
                                        .showNotification(
                                                DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                            }
                        }
                    });
        }

        return menu;
    }

    private static void initializePreview(final DiscussionThread thread,
            final RowContent content) {
        content.setStartedBy(thread.getOriginalPoster().getDisplayedName());
        content.setLatestPostDate(thread.getLatestPost().getTime());
        content.setLatestPostName(thread.getLatestPost().getAuthor()
                .getDisplayedName());
        content.setPostAmount(thread.getPostCount());
        content.setTopic(thread.getTopic());
        content.isSticky(thread.isSticky());
        content.isLocked(thread.isLocked());
        content.setThreadId(thread.getId());
    }

    public DiscussionThread getThread() {
        return thread;
    }

    @Override
    public void setHideOnMouseOut(final boolean hideOnMouseOut) {
        // ignore calls.
    }

    public void refresh() {
        final RowContent rowContent = new RowContent();
        initializePreview(thread, rowContent);
        setContent(rowContent);
    }
}
