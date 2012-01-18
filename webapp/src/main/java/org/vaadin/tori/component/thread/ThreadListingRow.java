package org.vaadin.tori.component.thread;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class ThreadListingRow extends CssLayout {

    private final DiscussionThread thread;

    public ThreadListingRow(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        this.thread = thread;

        final CssLayout details = new CssLayout();
        details.setStyleName("details");

        setStyleName("thread-listing-row");
        addComponent(new TopicComponent(thread, presenter));
        details.addComponent(getStartedBy(thread));
        details.addComponent(getPosts(thread));
        details.addComponent(new LatestPostComponent(thread));
        addComponent(details);
    }

    private Component getStartedBy(final DiscussionThread thread) {
        final Label startedBy = new Label(thread.getOriginalPoster()
                .getDisplayedName());
        startedBy.setSizeUndefined();
        startedBy.setStyleName("started-by");
        return startedBy;
    }

    private Component getPosts(final DiscussionThread thread) {
        final Label posts = new Label(Integer.toString(thread.getPostCount()));
        posts.setSizeUndefined();
        posts.setStyleName("posts");
        return posts;
    }

    public DiscussionThread getThread() {
        return thread;
    }
}
