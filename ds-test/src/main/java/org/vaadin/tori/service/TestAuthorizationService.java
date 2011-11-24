package org.vaadin.tori.service;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.tori.data.entity.AbstractEntity;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

public class TestAuthorizationService implements DebugAuthorizationService {

    private boolean isCategoryAdministrator = true;
    private boolean mayReportPosts = true;
    private final Map<Category, Boolean> mayFollowCategory = new HashMap<Category, Boolean>();;
    private final Map<Category, Boolean> mayDeleteCategory = new HashMap<Category, Boolean>();
    private final Map<Category, Boolean> mayEditCategory = new HashMap<Category, Boolean>();;
    private final Map<DiscussionThread, Boolean> mayReplyInThread = new HashMap<DiscussionThread, Boolean>();
    private final Map<Post, Boolean> mayEditPost = new HashMap<Post, Boolean>();
    private boolean mayBan = true;
    private final Map<DiscussionThread, Boolean> mayFollow = new HashMap<DiscussionThread, Boolean>();
    private final Map<Post, Boolean> mayDelete = new HashMap<Post, Boolean>();
    private boolean mayVote = true;

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
    public void setMayFollow(final Category category, final boolean b) {
        mayFollowCategory.put(category, b);
    }

    @Override
    public boolean mayFollow(final Category category) {
        return get(mayFollowCategory, category, true);
    }

    @Override
    public boolean mayDelete(final Category category) {
        return get(mayDeleteCategory, category, true);
    }

    @Override
    public void setMayDelete(final Category category, final boolean b) {
        mayDeleteCategory.put(category, b);
    }

    @Override
    public boolean mayEdit(final Category category) {
        return get(mayEditCategory, category, true);
    }

    @Override
    public void setMayEdit(final Category category, final boolean b) {
        mayEditCategory.put(category, b);
    }

    private static <T extends AbstractEntity> boolean get(
            final Map<T, Boolean> rights, final T referenceEntity,
            final boolean defaultValue) {
        final Boolean value = rights.get(referenceEntity);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public boolean mayEdit(final Post post) {
        return get(mayEditPost, post, true);
    }

    @Override
    public void setMayEdit(final Post post, final boolean b) {
        mayEditPost.put(post, b);
    }

    @Override
    public boolean mayReplyIn(final DiscussionThread thread) {
        return get(mayReplyInThread, thread, true);
    }

    @Override
    public void setMayReplyIn(final DiscussionThread thread, final boolean b) {
        mayReplyInThread.put(thread, b);
    }

    @Override
    public boolean mayBan() {
        return mayBan;
    }

    @Override
    public void setMayBan(final boolean b) {
        mayBan = b;
    }

    @Override
    public boolean mayFollow(final DiscussionThread currentThread) {
        return get(mayFollow, currentThread, true);
    }

    @Override
    public void setMayFollow(final DiscussionThread thread, final boolean b) {
        mayFollow.put(thread, b);
    }

    @Override
    public boolean mayDelete(final Post post) {
        return get(mayDelete, post, true);
    }

    @Override
    public void setMayDelete(final Post post, final boolean b) {
        mayDelete.put(post, b);
    }

    @Override
    public boolean mayVote() {
        return mayVote;
    }

    @Override
    public void setMayVote(final boolean b) {
        mayVote = b;
    }
}
