package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Link;

@SuppressWarnings("serial")
public class TopicComponent extends CustomComponent {

    private static final String THREAD_URL = ToriNavigator.ApplicationView.THREADS
            .getUrl();

    private final CssLayout layout;

    public TopicComponent(final DiscussionThread thread) {
        setCompositionRoot(layout = new CssLayout());
        layout.setSizeFull();
        setSizeFull();
        setStyleName("topic");

        final long id = thread.getId();
        final String topic = thread.getTopic();
        layout.addComponent(createCategoryLink(id, topic));
    }

    private Component createCategoryLink(final long id, final String name) {
        final Link categoryLink = new Link();
        categoryLink.setCaption(name);
        categoryLink.setResource(new ExternalResource("#" + THREAD_URL + "/"
                + id));
        return categoryLink;
    }
}
