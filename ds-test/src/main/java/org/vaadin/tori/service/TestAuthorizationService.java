package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;

public class TestAuthorizationService implements AuthorizationService {

    private final boolean isCategoryAdministrator = true;
    private final boolean mayReportPosts = true;
    private final boolean mayFollowCategory = true;
    private final boolean mayMoveCategory = true;
    private final boolean mayDeleteCategory = true;
    private final boolean mayEditCategory = true;

    @Override
    public boolean isCategoryAdministrator() {
        return isCategoryAdministrator;
    }

    @Override
    public boolean mayReportPosts() {
        return mayReportPosts;
    }

    @Override
    public boolean mayFollowCategory(final Category category) {
        return mayFollowCategory;
    }

    @Override
    public boolean mayMoveCategory(final Category category) {
        return mayMoveCategory;
    }

    @Override
    public boolean mayDeleteCategory(final Category category) {
        return mayDeleteCategory;
    }

    @Override
    public boolean mayEditCategory(final Category category) {
        return mayEditCategory;
    }

}
