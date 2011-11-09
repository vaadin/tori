package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ThreadListing extends CustomComponent {

    private final VerticalLayout layout = new VerticalLayout();

    public ThreadListing() {
        setCompositionRoot(layout);
    }

    public void setThreads(final List<DiscussionThread> threadsInCategory) {
        layout.removeAllComponents();

        for (final DiscussionThread thread : threadsInCategory) {
            layout.addComponent(new ThreadListingItem(thread));
        }
    }
}
