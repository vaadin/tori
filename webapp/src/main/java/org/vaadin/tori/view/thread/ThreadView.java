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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.View;

public interface ThreadView extends View {
    void setPosts(List<PostData> posts);

    void showNotification(String message);

    void showError(String message);

    /**
     * Acknowledge that the post was properly accepted and saved.
     * 
     * @param updatedPost
     *            The new {@link Post} <strong>that should be added to the
     *            thread visually (if applicable).</strong>
     */
    void confirmReplyPostedAndShowIt(Post updatedPost);

    void redirectToDashboard();

    /** For those occasions when a regular error message simply doesn't suffice. */
    void panic();

    void appendQuote(String textToAppend);

    void otherUserAuthored(Post post);

    void otherUserTyping(User user);

    void setThread(DiscussionThread currentThread);

    void setViewPermissions(ViewPermissions viewPermissions);

    public interface PostData {

        long getId();

        long getThreadId();

        String getAuthorName();

        Date getTime();

        String getAuthorAvatarUrl();

        long getScore();

        String getRawBody();

        String getFormattedBody(boolean allowHtml);

        boolean isSelected();

        boolean hasAttachments();

        Map<String, String> getAttachments();

        long getAuthorId();

        Boolean getUpVoted();

        String getBadgeHTML();

        boolean isAuthorBanned();

        boolean userMayReportPosts();

        boolean userMayEdit();

        boolean userMayQuote();

        boolean userMayVote();

        boolean userMayBanAuthor();

        boolean userMayDelete();

        String getAuthorLink();

    }

    interface ViewPermissions {
        boolean mayAddFiles();

        int getMaxFileSize();

        boolean mayReplyInThread();
    }

}
