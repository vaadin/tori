package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.CategoryWrapper;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.DiscussionThreadWrapper;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.PostWrapper;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.data.entity.UserWrapper;
import org.vaadin.tori.service.post.PostReport;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBCategoryLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;

public class LiferayDataSource implements DataSource {

    private static final Logger log = Logger.getLogger(LiferayDataSource.class);

    // TODO retrieve this from somewhere instead of using a constant
    private static final long SCOPE_GROUP_ID = 10187;

    private static final long ROOT_CATEGORY_ID = 0;
    private static final int QUERY_ALL = com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS;

    @Override
    public List<Category> getRootCategories() {
        try {
            final List<MBCategory> rootCategories = MBCategoryLocalServiceUtil
                    .getCategories(SCOPE_GROUP_ID, ROOT_CATEGORY_ID, QUERY_ALL,
                            QUERY_ALL);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Found %d root level categories.",
                        rootCategories.size()));
            }
            return CategoryWrapper.wrap(rootCategories);
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        ToriUtil.checkForNull(category, "Category must not be null.");
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category) {
        ToriUtil.checkForNull(category, "Category must not be null.");

        try {
            final List<MBThread> liferayThreads = getLiferayThreads(category
                    .getId());

            // collection for the final result
            final List<DiscussionThread> result = new ArrayList<DiscussionThread>(
                    liferayThreads.size());
            for (final MBThread liferayThread : liferayThreads) {
                // must still get the root message of each thread
                final MBMessage rootMessage = MBMessageLocalServiceUtil
                        .getMessage(liferayThread.getRootMessageId());
                final User author = UserWrapper.wrap(UserLocalServiceUtil
                        .getUser(rootMessage.getUserId()));

                final DiscussionThread wrappedThread = DiscussionThreadWrapper
                        .wrap(liferayThread, rootMessage, author);
                wrappedThread.setCategory(category);
                result.add(wrappedThread);
            }
            return result;
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
            return Collections.emptyList();
        } catch (final PortalException e) {
            // TODO error handling
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<MBThread> getLiferayThreads(final long categoryId)
            throws SystemException {
        final int start = 0; // use QUERY_ALL if you'd like to get all
        final int end = 20; // use QUERY_ALL if you'd like to get all
        final List<MBThread> liferayThreads = MBThreadLocalServiceUtil
                .getThreads(SCOPE_GROUP_ID, categoryId,
                        WorkflowConstants.STATUS_APPROVED, start, end);
        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Found %d threads for category with id %d.",
                    liferayThreads.size(), categoryId));
        }
        return liferayThreads;
    }

    @Override
    public Category getCategory(final long categoryId) {
        try {
            return CategoryWrapper.wrap(MBCategoryLocalServiceUtil
                    .getCategory(categoryId));
        } catch (final PortalException e) {
            // TODO error handling
            e.printStackTrace();
            return null;
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getThreadCount(final Category category) {
        ToriUtil.checkForNull(category, "Category must not be null.");
        try {
            return MBThreadLocalServiceUtil.getCategoryThreadsCount(SCOPE_GROUP_ID,
                    category.getId(), WorkflowConstants.STATUS_APPROVED);
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public DiscussionThread getThread(final long threadId) {
        try {
            final MBThread thread = MBThreadLocalServiceUtil
                    .getMBThread(threadId);
            final MBMessage rootMessage = MBMessageLocalServiceUtil
                    .getMBMessage(thread.getRootMessageId());
            final User author = UserWrapper.wrap(UserLocalServiceUtil
                    .getUser(rootMessage.getUserId()));
            final Category wrappedCategory = CategoryWrapper
                    .wrap(MBCategoryLocalServiceUtil.getCategory(thread
                            .getCategoryId()));

            final DiscussionThread wrappedThread = DiscussionThreadWrapper
                    .wrap(thread, rootMessage, author);
            wrappedThread.setCategory(wrappedCategory);
            return wrappedThread;
        } catch (final PortalException e) {
            // TODO error handling
            e.printStackTrace();
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Post> getPosts(final DiscussionThread thread) {
        ToriUtil.checkForNull(thread, "DiscussionThread must not be null.");
        try {
            final List<MBMessage> messages = getLiferayPosts(thread.getId());

            final List<Post> result = new ArrayList<Post>(messages.size());
            for (final MBMessage message : messages) {
                final Post wrappedPost = PostWrapper.wrap(message);
                wrappedPost.setThread(thread);

                // get also the author
                User wrappedUser;
                wrappedUser = org.vaadin.tori.data.entity.UserWrapper
                        .wrap(UserLocalServiceUtil.getUser(message.getUserId()));
                wrappedPost.setAuthor(wrappedUser);
                wrappedPost.setThread(thread);
                result.add(wrappedPost);
            }
            return result;
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
            return Collections.emptyList();
        } catch (final PortalException e) {
            // TODO error handling
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<MBMessage> getLiferayPosts(final long threadId)
            throws SystemException {
        final List<MBMessage> liferayPosts = MBMessageLocalServiceUtil
                .getThreadMessages(threadId, WorkflowConstants.STATUS_APPROVED);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Found %d messages for thread with id %d.",
                    liferayPosts.size(), threadId));
        }
        return liferayPosts;
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
        // TODO
        return 0;
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
        // TODO
        return false;
    }

    @Override
    public void delete(final Post post) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public PostVote getPostVote(final Post post) {

        // TODO
        final PostVote dummyVote = new PostVote();
        dummyVote.setPost(post);
        dummyVote.setUpvote();
        final User dummyUser = new User();
        dummyUser.setDisplayedName("Ville Voter");
        dummyVote.setVoter(dummyUser);
        return dummyVote;
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
        // TODO
        return 0;
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

    @Override
    public void delete(final DiscussionThread thread) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
