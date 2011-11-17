package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

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
    public boolean mayEditPost(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayReplyInThread(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
