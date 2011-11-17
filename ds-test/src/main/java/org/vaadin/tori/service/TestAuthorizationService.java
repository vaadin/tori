package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;

public class TestAuthorizationService implements DebugAuthorizationService {

    private boolean isCategoryAdministrator = true;
    private boolean mayReportPosts = true;
    private final boolean mayFollowCategory = true;
    private final boolean mayMoveCategory = true;
    private final boolean mayDeleteCategory = true;
    private final boolean mayEditCategory = true;
    private boolean mayEditPosts = true;
    private boolean mayReplyInThreads = true;

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
    public boolean mayDeleteCategory(final Category category) {
        return mayDeleteCategory;
    }

    @Override
    public boolean mayEditCategory(final Category category) {
        return mayEditCategory;
    }

    @Override
    public boolean mayEditPosts() {
        return mayEditPosts;
    }

    @Override
    public void setMayEditPosts(final boolean b) {
        mayEditPosts = b;
    }

    @Override
    public boolean mayReplyInThreads() {
        return mayReplyInThreads;
    }

    @Override
    public void setMayReplyInThreads(final boolean b) {
        mayReplyInThreads = b;
    }
}
