package org.vaadin.tori.service;

public class TestAuthorizationService implements DebugAuthorizationService {

    private boolean isCategoryAdministrator = true;
    private boolean mayReportPosts = true;
    private boolean mayEditPosts = true;

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
    public boolean mayEditPosts() {
        return mayEditPosts;
    }

    @Override
    public void setMayEditPosts(final boolean b) {
        mayEditPosts = b;
    }
}
