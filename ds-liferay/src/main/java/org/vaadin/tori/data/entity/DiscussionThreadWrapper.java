package org.vaadin.tori.data.entity;

import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBThread;

public class DiscussionThreadWrapper extends DiscussionThread {

    public MBThread liferayThread;
    public MBMessage liferayRootMessage;
    public User threadAuthor;
    public User lastPostAuthor;

    private DiscussionThreadWrapper(final MBThread thread,
            final MBMessage rootMessage, final User threadAuthor,
            final User lastPostAuthor) {
        liferayThread = thread;
        liferayRootMessage = rootMessage;
        this.threadAuthor = threadAuthor;
        this.lastPostAuthor = lastPostAuthor;
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
    public Post getLatestPost() {
        final Post fakedLastPost = new Post();
        fakedLastPost.setTime(liferayThread.getLastPostDate());
        fakedLastPost.setAuthor(lastPostAuthor);
        return fakedLastPost;
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
            final MBMessage threadRootMessage, final User threadAuthor,
            final User lastPostAuthor) {
        if (threadToWrap != null) {
            return new DiscussionThreadWrapper(threadToWrap, threadRootMessage,
                    threadAuthor, lastPostAuthor);
        } else {
            return null;
        }
    }

}
