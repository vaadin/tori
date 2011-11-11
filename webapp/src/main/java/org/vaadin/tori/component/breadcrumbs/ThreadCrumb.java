package org.vaadin.tori.component.breadcrumbs;

import org.vaadin.hene.splitbutton.SplitButton;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonPopupVisibilityEvent;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
class ThreadCrumb extends CustomComponent {
    public interface ThreadSelectionListener {
        void selectThread(DiscussionThread selectedThread);
    }

    private final ThreadSelectionListener listener;
    private final SplitButton crumb;

    public ThreadCrumb(final DiscussionThread thread,
            final ThreadSelectionListener listener) {
        this.listener = listener;

        if (thread == null) {
            throw new RuntimeException("Trying to render the thread part of "
                    + "the breadcrumbs, but the given thread was null");
        }

        crumb = new SplitButton(thread.getTopic());
        crumb.addPopupVisibilityListener(new SplitButton.SplitButtonPopupVisibilityListener() {
            @Override
            public void splitButtonPopupVisibilityChange(
                    final SplitButtonPopupVisibilityEvent event) {
                event.getSplitButton().setComponent(new Label("bar"));
            }
        });

        setCompositionRoot(crumb);
    }
}
