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
}
