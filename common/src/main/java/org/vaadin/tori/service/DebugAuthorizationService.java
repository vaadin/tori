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
}
