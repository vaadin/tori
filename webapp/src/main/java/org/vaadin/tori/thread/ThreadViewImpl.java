package org.vaadin.tori.thread;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private VerticalLayout layout;

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void initView() {
        layout.setWidth("100%");
    }

    @Override
    protected ThreadPresenter createPresenter() {
        return new ThreadPresenter(ToriApplication.getCurrent().getDataSource());
    }

    @Override
    public DiscussionThread getCurrentThread() {
        return getPresenter().getCurrentThread();
    }

    @Override
    public void displayPosts(final List<Post> posts) {
        layout.removeAllComponents();

        layout.addComponent(new HeadingLabel(getCurrentThread().getTopic(),
                HeadingLevel.H2));

        for (final Post post : posts) {
            layout.addComponent(new HeadingLabel("Post by "
                    + post.getAuthor().getDisplayedName(), HeadingLevel.H3));
        }
    }

    @Override
    public void displayThreadNotFoundError(final String threadIdString) {
        getWindow().showNotification("No thread found for " + threadIdString,
                Notification.TYPE_ERROR_MESSAGE);
    }

    @Override
    protected void navigationTo(final String requestedDataId) {
        super.getPresenter().setCurrentThreadById(requestedDataId);
    }
}
