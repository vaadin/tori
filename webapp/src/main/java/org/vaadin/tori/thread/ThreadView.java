package org.vaadin.tori.thread;

import java.util.List;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.View;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface ThreadView extends View {
    /** May return <code>null</code> if the user visited an invalid URL */
    @CheckForNull
    DiscussionThread getCurrentThread();

    void displayPosts(List<Post> posts);

    void displayThreadNotFoundError(String threadIdString);

    void confirmPostReported();

    void confirmBanned();

    void confirmFollowingThread();

    void confirmUnFollowingThread();

    void confirmPostDeleted();

    void refreshScores(Post post, long newScore);

    void confirmReplyPosted();

    /**
     * This method is called when a reply is tried to be sent, but the current
     * {@link User} doens't have the rights to.
     * <p/>
     * Most probably happens when the <code>User</code> was revoked replying
     * rights while the post was being authored.
     */
    void displayUserCanNotReply();
}
