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
    public boolean mayFollow(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayDelete(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayEdit(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayEdit(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayReplyIn(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayBan() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayFollow(final DiscussionThread currentThread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayDelete(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayVote() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayMove(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean maySticky(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayLock(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
