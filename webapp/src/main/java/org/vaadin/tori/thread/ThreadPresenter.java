package org.vaadin.tori.thread;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.Presenter;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.service.post.PostReport;
import org.vaadin.tori.service.post.PostReportReceiver;

public class ThreadPresenter extends Presenter<ThreadView> implements
        PostReportReceiver {

    private DiscussionThread currentThread;

    public ThreadPresenter(final DataSource dataSource,
            final AuthorizationService authorizationService) {
        super(dataSource, authorizationService);
    }

    public void setCurrentThreadById(final String threadIdString) {
        DiscussionThread requestedThread = null;
        try {
            final long threadId = Long.valueOf(threadIdString);
            requestedThread = dataSource.getThread(threadId);
        } catch (final NumberFormatException e) {
            log.error("Invalid thread id format: " + threadIdString);
        }

        if (requestedThread != null) {
            currentThread = requestedThread;
            final ThreadView view = getView();
            view.displayPosts(currentThread.getPosts());
        } else {
            getView().displayThreadNotFoundError(threadIdString);
        }
    }

    public DiscussionThread getCurrentThread() {
        return currentThread;
    }

    @Override
    public void handlePostReport(final PostReport report) {
        dataSource.reportPost(report);
        getView().confirmPostReported();
    }

    public boolean userMayReportPosts() {
        return authorizationService.mayReportPosts();
    }
}
