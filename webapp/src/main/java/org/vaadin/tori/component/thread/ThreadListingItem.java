package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.data.entity.Thread;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class ThreadListingItem extends CustomComponent {
    private final Thread thread;

    public ThreadListingItem(final Thread thread) {
        this.thread = thread;

        final String threadUrl = ToriNavigator.ApplicationView.THREADS.getUrl();
        final long id = thread.getId();
        final String topic = thread.getTopic();
        setCompositionRoot(new Label(String.format("<a href=\"#%s/%s\">%s</a>",
                threadUrl, id, topic), Label.CONTENT_XHTML));
    }
}
