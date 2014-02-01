/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.view.thread;

import java.util.LinkedHashMap;
import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.View;

public interface ThreadView extends View {
    /**
     * May return <code>null</code>, e.g. when the user visited an invalid URL,
     * or a new thread is being created.
     */

    DiscussionThread getCurrentThread();

    void displayPosts(List<Post> posts, Long selectedPostId);

    void displayThreadNotFoundError(String threadIdString);

    void confirmPostReported();

    void confirmBanned(User user);

    void confirmUnbanned(User user);

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

    Category getCurrentCategory();

    /** For those occasions when a regular error message simply doesn't suffice. */
    void panic();

    void appendToReply(String textToAppend);

    /** Re-renders the post with the given post. */
    void refresh(Post post);

    void updateAttachmentList(LinkedHashMap<String, byte[]> attachments);

    void otherUserAuthored(Post post);

    void otherUserTyping(User user);

}
