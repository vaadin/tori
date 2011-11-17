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
    /** @see AuthorizationService#isCategoryAdministrator() */
    void setIsCategoryAdministrator(boolean b);

    /** @see AuthorizationService#mayReportPosts() */
    void setMayReportPosts(boolean b);

    /** @see AuthorizationService#mayFollowCategory(Category) */
    void setMayFollowCategory(Category category, boolean b);

    /** @see AuthorizationService#mayDeleteCategory(org.vaadin.tori.data.entity.Category) */
    void setMayDeleteCategory(Category category, boolean b);

    /** @see AuthorizationService#mayEditCategory(Category) */
    void setMayEditCategory(Category category, boolean b);

    /** @see AuthorizationService#mayEditPost(Post) */
    void setMayEditPost(Post post, boolean b);

    /** @see AuthorizationService#mayReplyInThread(DiscussionThread) */
    void setMayReplyInThread(DiscussionThread thread, boolean b);
}
