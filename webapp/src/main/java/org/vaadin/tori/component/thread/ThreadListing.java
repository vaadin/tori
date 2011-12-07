package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

/**
 * UI component for displaying a vertical hierarchical list of threads.
 */
@SuppressWarnings("serial")
public class ThreadListing extends Table {

    private static final String PROPERTY_ID_TOPIC = "Topic";
    private static final String PROPERTY_ID_STARTEDBY = "Started by";
    private static final String PROPERTY_ID_POSTS = "Posts";
    private static final String PROPERTY_ID_LATESTPOST = "Latest post";
    private final CategoryPresenter presenter;

    public ThreadListing(final CategoryPresenter presenter) {
        this.presenter = presenter;
        setStyleName("threadTable");

        // set container properties
        addContainerProperty(PROPERTY_ID_TOPIC, Component.class, null);
        addContainerProperty(PROPERTY_ID_STARTEDBY, String.class, "");
        addContainerProperty(PROPERTY_ID_POSTS, Integer.class, 0);
        addContainerProperty(PROPERTY_ID_LATESTPOST, Component.class, null);

        // setColumnWidth(PROPERTY_ID_TOPIC, *);
        setColumnWidth(PROPERTY_ID_STARTEDBY, 150);
        setColumnWidth(PROPERTY_ID_POSTS, 50);
        setColumnWidth(PROPERTY_ID_LATESTPOST, 150);

        // set visual properties
        setWidth("100%");
    }

    public void setThreads(final List<DiscussionThread> threads) {
        removeAllItems();

        for (final DiscussionThread thread : threads) {
            final Item item = addItem(thread);
            item.getItemProperty(PROPERTY_ID_TOPIC).setValue(
                    new TopicComponent(thread, presenter));
            item.getItemProperty(PROPERTY_ID_STARTEDBY).setValue(
                    thread.getOriginalPoster().getDisplayedName());
            item.getItemProperty(PROPERTY_ID_POSTS).setValue(
                    thread.getPostCount());
            item.getItemProperty(PROPERTY_ID_LATESTPOST).setValue(
                    new LatestPostComponent(thread));

            setPageLength(this.size());
        }
    }

    public void refresh(final DiscussionThread thread) {
        // TODO
        System.out.println("ThreadListing.refresh()");
        System.err.println("NOT DONE!");
    }
}
