package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletRequest;

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
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBThreadConstants;
import com.liferay.portlet.messageboards.service.MBCategoryLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;
import com.liferay.portlet.ratings.model.RatingsStats;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;

public class LiferayDataSource implements DataSource {

    private static final Logger log = Logger.getLogger(LiferayDataSource.class);

    private static final long ROOT_CATEGORY_ID = 0;
    private static final int QUERY_ALL = com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS;

    private long scopeGroupId = -1;
    private String imagePath;
    private ServiceContext mbMessageServiceContext;

    @Override
    public List<Category> getRootCategories() {
        try {
            final List<MBCategory> rootCategories = MBCategoryLocalServiceUtil
                    .getCategories(scopeGroupId, ROOT_CATEGORY_ID, QUERY_ALL,
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
                final DiscussionThread wrappedThread = wrapLiferayThread(
                        liferayThread, category);
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

    private DiscussionThread wrapLiferayThread(final MBThread liferayThread,
            final Category category) throws PortalException, SystemException {
        // get the root message of the thread
        final MBMessage rootMessage = MBMessageLocalServiceUtil
                .getMessage(liferayThread.getRootMessageId());
        // get the author of the root message
        final User author = UserWrapper.wrap(
                UserLocalServiceUtil.getUser(rootMessage.getUserId()),
                imagePath);
        // get the author of the last post
        final User lastPostAuthor = UserWrapper.wrap(UserLocalServiceUtil
                .getUser(liferayThread.getLastPostByUserId()), imagePath);

        final DiscussionThread wrappedThread = DiscussionThreadWrapper.wrap(
                liferayThread, rootMessage, author, lastPostAuthor);
        wrappedThread.setCategory(category);
        return wrappedThread;
    }

    private List<MBThread> getLiferayThreads(final long categoryId)
            throws SystemException {
        final int start = 0; // use QUERY_ALL if you'd like to get all
        final int end = 20; // use QUERY_ALL if you'd like to get all
        final List<MBThread> liferayThreads = MBThreadLocalServiceUtil
                .getThreads(scopeGroupId, categoryId,
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
            return MBThreadLocalServiceUtil.getCategoryThreadsCount(
                    scopeGroupId, category.getId(),
                    WorkflowConstants.STATUS_APPROVED);
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
            final Category wrappedCategory = CategoryWrapper
                    .wrap(MBCategoryLocalServiceUtil.getCategory(thread
                            .getCategoryId()));
            return wrapLiferayThread(thread, wrappedCategory);
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
                wrappedUser = org.vaadin.tori.data.entity.UserWrapper.wrap(
                        UserLocalServiceUtil.getUser(message.getUserId()),
                        imagePath);
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
        log.warn("Not yet implemented.");
    }

    @Override
    public void save(final Category categoryToSave) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void delete(final Category categoryToDelete) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void reportPost(final PostReport report) {
        log.warn("Not yet implemented.");
    }

    @Override
    public long getUnreadThreadCount(final Category category) {
        log.warn("Not yet implemented.");
        return 0;
    }

    @Override
    public void save(final Post post) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void ban(final User user) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void follow(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void unFollow(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public boolean isFollowing(final DiscussionThread thread) {
        // TODO
        return false;
    }

    @Override
    public void delete(final Post post) {
        try {
            MBMessageLocalServiceUtil.deleteMBMessage(post.getId());
        } catch (final PortalException e) {
            // TODO error handling
            e.printStackTrace();
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
        }
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
        log.warn("Not yet implemented.");
    }

    @Override
    public void downvote(final Post post) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void removeUserVote(final Post post) {
        log.warn("Not yet implemented.");
    }

    @Override
    public long getScore(final Post post) {
        try {
            final RatingsStats ratingsStats = RatingsStatsLocalServiceUtil
                    .getStats(MBMessage.class.getName(), post.getId());
            return (long) (ratingsStats.getAverageScore() * ratingsStats
                    .getTotalEntries());
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void saveAsCurrentUser(final Post post) {
        final DiscussionThreadWrapper thread = (DiscussionThreadWrapper) post
                .getThread();

        final long groupId = scopeGroupId;
        final long categoryId = thread.getCategory().getId();
        final long threadId = thread.getId();

        // TODO actually decide between reply and a new thread
        final boolean createNewThread = false;
        long parentMessageId = MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID;
        if (!createNewThread) {
            // TODO can we actually use the root message here?
            parentMessageId = thread.getRootMessageId();
        }
        final String subject = "RE: " + post.getThread().getTopic();
        final String body = post.getBodyRaw();
        final List<ObjectValuePair<String, byte[]>> files = Collections
                .emptyList();
        final boolean anonymous = false;
        final double priority = MBThreadConstants.PRIORITY_NOT_GIVEN;
        final boolean allowPingbacks = false;

        try {
            MBMessageServiceUtil.addMessage(groupId, categoryId, threadId,
                    parentMessageId, subject, body, files, anonymous, priority,
                    allowPingbacks, mbMessageServiceContext);
        } catch (final PortalException e) {
            // TODO error handling
            e.printStackTrace();
        } catch (final SystemException e) {
            // TODO error handling
            e.printStackTrace();
        }
    }

    @Override
    public void move(final DiscussionThread thread,
            final Category destinationCategory) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void sticky(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void unsticky(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void lock(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void unlock(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void delete(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void setRequest(final Object request) {
        if (!(request instanceof PortletRequest)) {
            log.warn("Given request was not an instance of PortletRequest.");
            return;
        }

        final PortletRequest portletRequest = (PortletRequest) request;
        if (scopeGroupId < 0) {
            // scope not defined yet -> get if from the request
            final ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest
                    .getAttribute("THEME_DISPLAY");

            if (themeDisplay != null) {
                scopeGroupId = themeDisplay.getScopeGroupId();
                imagePath = themeDisplay.getPathImage();
                log.info("Using groupId " + scopeGroupId + " as the scope.");
            }
        }

        try {
            mbMessageServiceContext = ServiceContextFactory.getInstance(
                    MBMessage.class.getName(), portletRequest);
        } catch (final PortalException e) {
            log.error("Couldn't create ServiceContext.", e);
        } catch (final SystemException e) {
            log.error("Couldn't create ServiceContext.", e);
        }
    }
}
