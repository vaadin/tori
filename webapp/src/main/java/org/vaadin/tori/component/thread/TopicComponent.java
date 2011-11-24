package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.ContextMenu.ContextAction;
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

    private static final String THREAD_URL = ToriNavigator.ApplicationView.THREADS
            .getUrl();

    private static final String FOLLOW_CAPTION = "Follow thread";
    private static final Resource FOLLOW_ICON = new ThemeResource(
            "images/icon-follow.png");

    private static final String UNFOLLOW_CAPTION = "Unfollow thread";
    private static final Resource UNFOLLOW_ICON = new ThemeResource(
            "images/icon-unfollow.png");

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

        return menu;
    }

    public void setPresenter(final CategoryPresenter presenter) {
        this.presenter = presenter;
    }

}
