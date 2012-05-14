package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.LazyLayout;
import org.vaadin.tori.data.entity.DiscussionThread;

@SuppressWarnings("serial")
public class ThreadListingHacked extends ThreadListing {

    public ThreadListingHacked(final CategoryPresenter presenter) {
        super(presenter);
        root.replaceComponent(layout, layout = new LazyLayout());
        layout.setPlaceholderSize(PLACEHOLDER_HEIGHT, PLACEHOLDER_WIDTH);
        layout.setRenderDistance(RENDER_DISTANCE_PX);
        layout.setRenderDelay(RENDER_DELAY_MILLIS);
        layout.setStyleName("wrapper-layout");
    }

    public void setThreads(final List<DiscussionThread> threadsInCategory) {
        for (int i = 0; i < threadsInCategory.size(); i++) {
            final DiscussionThread thread = threadsInCategory.get(i);
            final ThreadListingRow row = new ThreadListingRow(thread, presenter);
            if (i < 10) {
                ((LazyLayout) layout).addComponentEagerly(row);
            } else {
                layout.addComponent(row);
            }
        }
    }
}
