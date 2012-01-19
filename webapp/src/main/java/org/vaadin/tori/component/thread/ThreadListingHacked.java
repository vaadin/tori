package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.data.entity.DiscussionThread;

public class ThreadListingHacked extends ThreadListing {

    public ThreadListingHacked(final CategoryPresenter presenter) {
        super(presenter);
        super.layout.setComponentGenerator(null);
    }

    public void setThreads(final List<DiscussionThread> threadsInCategory) {
        for (int i = 0; i < threadsInCategory.size(); i++) {
            final DiscussionThread thread = threadsInCategory.get(i);
            final ThreadListingRow row = new ThreadListingRow(thread, presenter);
            if (i < 10) {
                layout.addComponentEagerly(row);
            } else {
                layout.addComponent(row);
            }
        }
    }
}
