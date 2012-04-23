package org.vaadin.tori.thread;

import java.util.LinkedHashMap;
import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.View;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface ThreadView extends View {
    /**
     * May return <code>null</code>, e.g. when the user visited an invalid URL,
     * or a new thread is being created.
     */
    @CheckForNull
    DiscussionThread getCurrentThread();

    void displayPosts(List<Post> posts, @NonNull DiscussionThread currentThread);

    void displayThreadNotFoundError(String threadIdString);

    void confirmPostReported();

    void confirmBanned();

    void confirmFollowingThread();

    void confirmUnFollowingThread();

    void confirmPostDeleted();

    void refreshScores(Post post, long newScore);

    /**
     * Acknowledge that the post was properly accepted and saved.
     * 
     * @param updatedPost
     *            The new {@link Post} <strong>that should be added to the
     *            thread visually (if applicable).</strong>
     */
    void confirmReplyPostedAndShowIt(Post updatedPost);

    /**
     * This method is called when a reply is tried to be sent, but the current
     * {@link User} doens't have the rights to.
     * <p/>
     * Most probably happens when the <code>User</code> was revoked replying
     * rights while the post was being authored.
     */
    void displayUserCanNotReply();

    /**
     * This method is called when trying to edit a {@link Post}, but the current
     * {@link User} doens't have the rights to do so.
     * <p/>
     * Most probably happens when the <code>User</code> was revoked editing
     * rights while the post was being modified.
     */
    void displayUserCanNotEdit();

    void redirectToDashboard();

    void displayNewThreadFormFor(Category category);

    /** May return <code>null</code>, e.g. when the user visited an invalid URL */
    @CheckForNull
    Category getCurrentCategory();

    /** For those occasions when a regular error message simply doesn't suffice. */
    void panic();

    void appendToReply(String textToAppend);

    /** Re-renders the post with the given post. */
    void refresh(Post post);

    void updateAttachmentList(LinkedHashMap<String, byte[]> attachments);

}
