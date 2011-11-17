package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;

public class TestAuthorizationService implements DebugAuthorizationService {

    private boolean isCategoryAdministrator = true;
    private boolean mayReportPosts = true;
    private boolean mayFollowCategory = true;
    private boolean mayMoveCategory = true;
    private boolean mayDeleteCategory = true;
    private boolean mayEditCategory = true;

    @Override
    public boolean isCategoryAdministrator() {
        return isCategoryAdministrator;
    }

    @Override
    public void setIsCategoryAdministrator(final boolean b) {
        isCategoryAdministrator = b;
    }

    @Override
    public boolean mayReportPosts() {
        return mayReportPosts;
    }

    @Override
    public void setMayReportPosts(final boolean b) {
        mayReportPosts = b;
    }

    @Override
    public boolean mayFollowCategory(final Category category) {
        return mayFollowCategory;
    }

    @Override
    public void setMayFollowCategory(final boolean mayFollowCategory) {
        this.mayFollowCategory = mayFollowCategory;
    }

    @Override
    public boolean mayMoveCategory(final Category category) {
        return mayMoveCategory;
    }

    @Override
    public void setMayMoveCategory(final boolean mayMoveCategory) {
        this.mayMoveCategory = mayMoveCategory;
    }

    @Override
    public boolean mayDeleteCategory(final Category category) {
        return mayDeleteCategory;
    }

    @Override
    public void setMayDeleteCategory(final boolean mayDeleteCategory) {
        this.mayDeleteCategory = mayDeleteCategory;
    }

    @Override
    public boolean mayEditCategory(final Category category) {
        return mayEditCategory;
    }

    @Override
    public void setMayEditCategory(final boolean mayEditCategory) {
        this.mayEditCategory = mayEditCategory;
    }

}
