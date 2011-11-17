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
    private final Map<Category, Boolean> mayMoveCategory = new HashMap<Category, Boolean>();;
    private final Map<Category, Boolean> mayDeleteCategory = new HashMap<Category, Boolean>();
    private final Map<Category, Boolean> mayEditCategory = new HashMap<Category, Boolean>();;
    private final Map<DiscussionThread, Boolean> mayReplyInThread = new HashMap<DiscussionThread, Boolean>();
    private final Map<Post, Boolean> mayEditPost = new HashMap<Post, Boolean>();

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
    public void setMayFollowCategory(final Category category, final boolean b) {
        mayFollowCategory.put(category, b);
    }

    @Override
    public boolean mayFollowCategory(final Category category) {
        return get(mayFollowCategory, category, true);
    }

    @Override
    public void setMayMoveCategory(final Category category, final boolean b) {
        mayMoveCategory.put(category, b);
    }

    @Override
    public boolean mayMoveCategory(final Category category) {
        return get(mayMoveCategory, category, true);
    }

    @Override
    public boolean mayDeleteCategory(final Category category) {
        return get(mayDeleteCategory, category, true);
    }

    @Override
    public void setMayDeleteCategory(final Category category, final boolean b) {
        mayDeleteCategory.put(category, b);
    }

    @Override
    public boolean mayEditCategory(final Category category) {
        return get(mayEditCategory, category, true);
    }

    @Override
    public void setMayEditCategory(final Category category, final boolean b) {
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
    public boolean mayEditPost(final Post post) {
        return get(mayEditPost, post, true);
    }

    @Override
    public void setMayEditPost(final Post post, final boolean b) {
        mayEditPost.put(post, b);
    }

    @Override
    public boolean mayReplyInThread(final DiscussionThread thread) {
        return get(mayReplyInThread, thread, true);
    }

    @Override
    public void setMayReplyInThread(final DiscussionThread thread,
            final boolean b) {
        mayReplyInThread.put(thread, b);
    }
}
