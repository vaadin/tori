package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.ContextMenu.ContextAction;
import org.vaadin.tori.component.ContextMenu.ContextComponentSwapper;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Link;

@SuppressWarnings("serial")
public class TopicComponent extends CustomComponent {

    private class FollowAction implements ContextAction {
        private final DiscussionThread thread;

        public FollowAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            presenter.follow(thread);
            menu.swap(this, UNFOLLOW_ICON, UNFOLLOW_CAPTION,
                    new UnfollowAction(thread));
        }
    }

    private class UnfollowAction implements ContextAction {
        private final DiscussionThread thread;

        public UnfollowAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            presenter.unfollow(thread);
            menu.swap(this, FOLLOW_ICON, FOLLOW_CAPTION, new FollowAction(
                    thread));
        }
    }

    private class StickyAction implements ContextAction {
        private final DiscussionThread thread;

        public StickyAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            presenter.sticky(thread);
            menu.swap(this, UNSTICKY_ICON, UNSTICKY_CAPTION,
                    new UnstickyAction(thread));
        }
    }

    private class UnstickyAction implements ContextAction {
        private final DiscussionThread thread;

        public UnstickyAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            presenter.unsticky(thread);
            menu.swap(this, STICKY_ICON, STICKY_CAPTION, new StickyAction(
                    thread));
        }
    }

    private class LockAction implements ContextAction {
        private final DiscussionThread thread;

        public LockAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            presenter.lock(thread);
            menu.swap(this, UNLOCK_ICON, UNLOCK_CAPTION, new UnlockAction(
                    thread));
        }
    }

    private class UnlockAction implements ContextAction {
        private final DiscussionThread thread;

        public UnlockAction(final DiscussionThread thread) {
            this.thread = thread;
        }

        @Override
        public void contextClicked() {
            presenter.unlock(thread);
            menu.swap(this, LOCK_ICON, LOCK_CAPTION, new LockAction(thread));
        }
    }

    private static final String THREAD_URL = ToriNavigator.ApplicationView.THREADS
            .getUrl();

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
            "images/lock.png");

    private static final String UNLOCK_CAPTION = "Unlock thread";
    private static final Resource UNLOCK_ICON = new ThemeResource(
            "images/unlock.png");

    private final CustomLayout layout;
    private CategoryPresenter presenter;

    private final ContextMenu menu;

    public TopicComponent(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        ToriUtil.checkForNull(thread, "thread may not be null");
        ToriUtil.checkForNull(presenter, "presenter may not be null");

        this.presenter = presenter;

        setCompositionRoot(layout = new CustomLayout(
                ToriApplication.CUSTOM_LAYOUT_PATH + "topiclayout"));
        setWidth("100%");
        setStyleName("topic");

        final long id = thread.getId();
        final String topic = thread.getTopic();
        layout.addComponent(createCategoryLink(id, topic), "link");

        menu = createContextMenu(thread);
        layout.addComponent(menu, "contextmenu");
    }

    private Component createCategoryLink(final long id, final String name) {
        final Link categoryLink = new Link();
        categoryLink.setCaption(name);
        categoryLink.setResource(new ExternalResource("#" + THREAD_URL + "/"
                + id));
        return categoryLink;
    }

    private ContextMenu createContextMenu(final DiscussionThread thread) {
        final ContextMenu menu = new ContextMenu();
        menu.setData(thread);

        if (presenter.userCanFollow(thread)) {
            menu.add(FOLLOW_ICON, FOLLOW_CAPTION, new FollowAction(thread));
        } else if (presenter.userCanUnFollow(thread)) {
            menu.add(UNFOLLOW_ICON, UNFOLLOW_CAPTION,
                    new UnfollowAction(thread));
        }

        if (presenter.userMayMove(thread)) {
            menu.add(new ThemeResource("images/icon-move.png"), "Move thread",
                    new ContextComponentSwapper() {
                        @Override
                        public Component swapContextComponent() {
                            return new ThreadMoveComponent(thread, menu,
                                    presenter);
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

        return menu;
    }

    public void setPresenter(final CategoryPresenter presenter) {
        this.presenter = presenter;
    }

}
