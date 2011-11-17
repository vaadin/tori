package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;

public class LiferayAuthorizationService implements AuthorizationService {

    @Override
    public boolean isCategoryAdministrator() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayReportPosts() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayFollowCategory(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayDeleteCategory(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayEditCategory(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayEditPosts() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayReplyInThreads() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
