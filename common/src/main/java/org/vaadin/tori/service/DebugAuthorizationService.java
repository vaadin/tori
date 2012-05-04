package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

/**
 * <p>
 * If the deployed {@link AuthorizationService} is an instance of
 * <code>DebugAuthorizationService</code>, the developer will be presented with
 * additional testing controls. These controls allows the developer direct
 * manipulation of any and all values retrieved via the
 * <code>AuthorizationService</code>.
 * </p>
 * 
 * <p>
 * This interface contains a symmetric setter for each method found in
 * <code>AuthorizationService</code>
 * </p>
 */
public interface DebugAuthorizationService extends AuthorizationService {
    /** @see AuthorizationService#mayEditCategories() */
    void setMayEditCategories(boolean b);

    /** @see AuthorizationService#mayReportPosts() */
    void setMayReportPosts(boolean b);

    /** @see AuthorizationService#mayFollow(Category) */
    void setMayFollow(Category category, boolean b);

    /** @see AuthorizationService#mayDelete(org.vaadin.tori.data.entity.Category) */
    void setMayDelete(Category category, boolean b);

    /** @see AuthorizationService#mayEdit(Category) */
    void setMayEdit(Category category, boolean b);

    /** @see AuthorizationService#mayEdit(Post) */
    void setMayEdit(Post post, boolean b);

    /** @see AuthorizationService#mayReplyIn(DiscussionThread) */
    void setMayReplyIn(DiscussionThread thread, boolean b);

    /** @see AuthorizationService#mayAddFiles(DiscussionThread) */
    void setMayAddFiles(Category category, boolean b);

    /** @see AuthorizationService#mayBan() */
    void setMayBan(boolean b);

    /** @see AuthorizationService#mayFollow(DiscussionThread) */
    void setMayFollow(DiscussionThread thread, boolean b);

    /** @see AuthorizationService#mayDelete(Post) */
    void setMayDelete(Post post, boolean b);

    /** @see AuthorizationService#mayVote() */
    void setMayVote(boolean b);

    /** @see AuthorizationService#mayMove(DiscussionThread) */
    void setMayMove(DiscussionThread thread, boolean b);

    /** @see AuthorizationService#maySticky(DiscussionThread) */
    void setMaySticky(DiscussionThread thread, boolean b);

    /** @see AuthorizationService#mayLock(DiscussionThread) */
    void setMayLock(DiscussionThread thread, boolean b);

    /** @see AuthorizationService#mayLock(DiscussionThread) */
    void setMayDelete(DiscussionThread thread, boolean b);

    /** @see AuthorizationService#mayCreateThreadIn(Category) */
    void setMayCreateThreadIn(Category category, boolean b);

}
