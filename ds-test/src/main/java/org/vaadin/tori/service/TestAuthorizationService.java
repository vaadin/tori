package org.vaadin.tori.service;

public class TestAuthorizationService implements AuthorizationService {

    private final boolean isCategoryAdministrator = true;
    private final boolean mayReportPosts = true;

    @Override
    public boolean isCategoryAdministrator() {
        return isCategoryAdministrator;
    }

    @Override
    public boolean mayReportPosts() {
        return mayReportPosts;
    }

}
