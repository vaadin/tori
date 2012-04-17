package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.data.entity.DiscussionThread;

@SuppressWarnings("serial")
public class ThreadListingHacked extends ThreadListing {

    public ThreadListingHacked(final CategoryPresenter presenter) {
        super(presenter);
        /*-
         * FIXME LazyLayout
        layout.setComponentGenerator(null);
         */
    }

    public void setThreads(final List<DiscussionThread> threadsInCategory) {
        for (int i = 0; i < threadsInCategory.size(); i++) {
            final DiscussionThread thread = threadsInCategory.get(i);
            final ThreadListingRow row = new ThreadListingRow(thread, presenter);
            /*-
            FIXME lazylaout
            if (i < 10) {
                layout.addComponentEagerly(row);
            } else {
                layout.addComponent(row);
            }
             */
            layout.addComponent(row);
        }
    }
}
