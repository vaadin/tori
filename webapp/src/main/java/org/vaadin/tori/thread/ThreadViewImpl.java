package org.vaadin.tori.thread;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.component.FloatingBar;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.post.PostComponent;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;

    @Override
    protected Component createCompositionRoot() {
        return layout = new CssLayout();
    }

    @Override
    public void initView() {
        layout.setWidth("100%");
    }

    @Override
    protected ThreadPresenter createPresenter() {
        final ToriApplication app = ToriApplication.getCurrent();
        return new ThreadPresenter(app.getDataSource(),
                app.getAuthorizationService());
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

        boolean first = true;
        for (final Post post : posts) {
            final PostComponent c = new PostComponent(post, getPresenter());
            if (getPresenter().userMayReportPosts()) {
                c.enableReporting();
            }
            if (getPresenter().userMayEdit(post)) {
                c.enableEditing();
            }
            if (getPresenter().userMayQuote(post)) {
                c.enableQuoting();
            }
            layout.addComponent(c);

            if (first) {
                // create the floating summary bar for the first post
                final FloatingBar summaryBar = getPostSummaryBar(post);
                summaryBar.setScrollComponent(c);
                layout.addComponent(summaryBar);
                first = false;
            }
        }
    }

    private FloatingBar getPostSummaryBar(final Post post) {
        final FloatingBar bar = new FloatingBar();
        final VerticalLayout barLayout = new VerticalLayout();
        barLayout.setWidth("100%");
        barLayout.addComponent(new Label(post.getBodyRaw().substring(0, 100)));
        bar.setContent(barLayout);
        return bar;
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

    @Override
    public void confirmPostReported() {
        getWindow().showNotification("Post is reported!");
    }
}
