package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

/**
 * Provides methods for specifying access rights to resources or certain
 * operations.
 */
public interface AuthorizationService {

    /**
     * Returns {@code true} if the current user is authorized to create and
     * rearrange categories.
     * 
     * @return {@code true} if the user can create and rearrange categories.
     */
    boolean isCategoryAdministrator();

    boolean mayReportPosts();

    boolean mayFollow(Category category);

    boolean mayDelete(Category category);

    boolean mayEdit(Category category);

    boolean mayEdit(Post post);

    boolean mayReplyIn(DiscussionThread thread);

    boolean mayBan();

    boolean mayFollow(DiscussionThread currentThread);

    boolean mayDelete(Post post);

    boolean mayVote();

    boolean mayMove(DiscussionThread thread);

    boolean maySticky(DiscussionThread thread);

    boolean mayLock(DiscussionThread thread);

    boolean mayDelete(DiscussionThread thread);

    boolean mayCreateThreadIn(Category category);

    /**
     * Passes the current request to this DataSource. It can be an instance of
     * {@code PortletRequest} or {@code HttpServletRequest} depending on the
     * context. The implementation is free to ignore the request if it doesn't
     * need any parameters from the request.
     * 
     * @param request
     *            {@code PortletRequest} or {@code HttpServletRequest}
     */
    void setRequest(Object request);
}
