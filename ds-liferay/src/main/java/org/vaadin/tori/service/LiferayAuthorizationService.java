package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

public class LiferayAuthorizationService implements AuthorizationService {

    @Override
    public boolean isCategoryAdministrator() {
        // TODO
        return true;
    }

    @Override
    public boolean mayReportPosts() {
        // TODO
        return true;
    }

    @Override
    public boolean mayFollow(final Category category) {
        // TODO
        return true;
    }

    @Override
    public boolean mayDelete(final Category category) {
        // TODO
        return true;
    }

    @Override
    public boolean mayEdit(final Category category) {
        // TODO
        return true;
    }

    @Override
    public boolean mayEdit(final Post post) {
        // TODO
        return true;
    }

    @Override
    public boolean mayReplyIn(final DiscussionThread thread) {
        // TODO
        return true;
    }

    @Override
    public boolean mayBan() {
        // TODO
        return true;
    }

    @Override
    public boolean mayFollow(final DiscussionThread currentThread) {
        // TODO
        return true;
    }

    @Override
    public boolean mayDelete(final Post post) {
        // TODO
        return true;
    }

    @Override
    public boolean mayVote() {
        // TODO
        return true;
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

    @Override
    public boolean mayDelete(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean mayCreateThreadIn(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
