package org.vaadin.tori.data.entity;

import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBThread;

public class DiscussionThreadWrapper extends DiscussionThread {

    public MBThread liferayThread;
    public MBMessage liferayRootMessage;
    public User threadAuthor;

    private DiscussionThreadWrapper(final MBThread thread,
            final MBMessage rootMessage, final User threadAuthor) {
        liferayThread = thread;
        liferayRootMessage = rootMessage;
        this.threadAuthor = threadAuthor;
    }

    @Override
    public long getId() {
        return liferayThread.getThreadId();
    }

    @Override
    public String getTopic() {
        return liferayRootMessage.getSubject();
    }

    @Override
    public int getPostCount() {
        return liferayThread.getMessageCount();
    }

    @Override
    public User getOriginalPoster() {
        return threadAuthor;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof DiscussionThreadWrapper) {
            return liferayThread
                    .equals(((DiscussionThreadWrapper) obj).liferayThread);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return liferayThread.hashCode();
    }

    public static DiscussionThread wrap(final MBThread threadToWrap,
            final MBMessage threadRootMessage, final User threadAuthor) {
        if (threadToWrap != null) {
            return new DiscussionThreadWrapper(threadToWrap, threadRootMessage,
                    threadAuthor);
        } else {
            return null;
        }
    }

}
