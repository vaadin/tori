package org.vaadin.tori.service;

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

    /** @see AuthorizationService#mayFollowCategory(org.vaadin.tori.data.entity.Category) */
    void setMayFollowCategory(boolean mayFollowCategory);

    /** @see AuthorizationService#mayMoveCategory(org.vaadin.tori.data.entity.Category) */
    void setMayMoveCategory(boolean mayMoveCategory);

    /** @see AuthorizationService#mayDeleteCategory(org.vaadin.tori.data.entity.Category) */
    void setMayDeleteCategory(boolean mayDeleteCategory);

    /** @see AuthorizationService#mayEditCategory(org.vaadin.tori.data.entity.Category) */
    void setMayEditCategory(boolean mayEditCategory);
}
