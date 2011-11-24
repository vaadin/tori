package org.vaadin.tori.thread;

import java.util.List;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.View;

public interface ThreadView extends View {
    DiscussionThread getCurrentThread();

    void displayPosts(List<Post> posts);

    void displayThreadNotFoundError(String threadIdString);

    void confirmPostReported();

    void confirmBanned();

    void confirmFollowingThread();

    void confirmUnFollowingThread();

    void confirmPostDeleted();

    void refreshScores(Post post, long newScore);

    /**
     * @param reply
     *            The newly created {@link Post} component, representing the
     *            reply.
     */
    void confirmReplyPosted(Post reply);

    /**
     * This method is called when a reply is tried to be sent, but the current
     * {@link User} doens't have the rights to.
     * <p/>
     * Most probably happens when the <code>User</code> was revoked replying
     * rights while the post was being authored.
     */
    void displayUserCanNotReply();
}
