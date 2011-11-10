package org.vaadin.tori.thread;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.Presenter;

public class ThreadPresenter extends Presenter<ThreadView> {

    private final DataSource dataSource;
    private DiscussionThread currentThread;

    public ThreadPresenter(final DataSource dataSource) {
        this.dataSource = dataSource;
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

}
