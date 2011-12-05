package org.vaadin.tori.service;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

public class LiferayAuthorizationService implements AuthorizationService {

    private static final Logger log = Logger
            .getLogger(LiferayAuthorizationService.class);

    @Override
    public boolean isCategoryAdministrator() {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayReportPosts() {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayFollow(final Category category) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayDelete(final Category category) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayEdit(final Category category) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayEdit(final Post post) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayReplyIn(final DiscussionThread thread) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayBan() {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayFollow(final DiscussionThread currentThread) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayDelete(final Post post) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayVote() {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayMove(final DiscussionThread thread) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean maySticky(final DiscussionThread thread) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayLock(final DiscussionThread thread) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayDelete(final DiscussionThread thread) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayCreateThreadIn(final Category category) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

}
