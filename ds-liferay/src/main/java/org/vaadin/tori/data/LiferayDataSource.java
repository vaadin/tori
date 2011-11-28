package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.service.post.PostReport;

public class LiferayDataSource implements DataSource {

    @Override
    public List<Category> getRootCategories() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Category getCategory(final long categoryId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public long getThreadCount(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public DiscussionThread getThread(final long threadId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<Post> getPosts(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void save(final Iterable<Category> categoriesToSave) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void save(final Category categoryToSave) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void delete(final Category categoryToDelete) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void reportPost(final PostReport report) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public long getUnreadThreadCount(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void save(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void ban(final User user) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void follow(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void unFollow(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean isFollowing(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void delete(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public PostVote getPostVote(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void upvote(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void downvote(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void removeUserVote(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public long getScore(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void saveAsCurrentUser(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void move(final DiscussionThread thread,
            final Category destinationCategory) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void sticky(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void unsticky(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void lock(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void unlock(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
