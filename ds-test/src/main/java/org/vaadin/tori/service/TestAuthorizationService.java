package org.vaadin.tori.service;

public class TestAuthorizationService implements AuthorizationService {

    private final boolean isCategoryAdministrator = true;

    @Override
    public boolean isCategoryAdministrator() {
        return isCategoryAdministrator;
    }

}
