package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.EntityFactoryUtil;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.post.PostReport;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
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
import com.liferay.portlet.ratings.NoSuchEntryException;
import com.liferay.portlet.ratings.model.RatingsStats;
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil;
import com.liferay.portlet.ratings.service.RatingsEntryServiceUtil;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class LiferayDataSource implements DataSource {

    private static final Logger log = Logger.getLogger(LiferayDataSource.class);

    private static final long ROOT_CATEGORY_ID = 0;
    private static final int QUERY_ALL = com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS;

    private long scopeGroupId = -1;
    private long currentUserId;
    private String imagePath;
    private ServiceContext mbMessageServiceContext;

    @Override
    public List<Category> getRootCategories() throws DataSourceException {
        return internalGetSubCategories(null);
    }

    @Override
    public List<Category> getSubCategories(final Category category)
            throws DataSourceException {
        return internalGetSubCategories(category);
    }

    public static long getRootMessageId(final DiscussionThread thread)
            throws DataSourceException {
        try {
            final long threadId = thread.getId();
            final MBThread liferayThread = MBThreadLocalServiceUtil
                    .getMBThread(threadId);
            return liferayThread.getRootMessageId();
        } catch (final PortalException e) {
            log.error(String.format(
                    "Couldn't get root message id for thread %d.",
                    thread.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get root message id for thread %d.",
                    thread.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @NonNull
    private List<Category> internalGetSubCategories(final Category category)
            throws DataSourceException {
        final long parentCategoryId = (category != null ? category.getId()
                : ROOT_CATEGORY_ID);

        try {
            final List<MBCategory> categories = MBCategoryLocalServiceUtil
                    .getCategories(scopeGroupId, parentCategoryId, QUERY_ALL,
                            QUERY_ALL);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Found %d categories.",
                        categories.size()));
            }
            return EntityFactoryUtil.createCategories(categories);
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get subcategories for parent category %d.",
                    parentCategoryId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category)
            throws DataSourceException {
        ToriUtil.checkForNull(category, "Category must not be null.");

        try {
            final List<MBThread> liferayThreads = getLiferayThreadsForCategory(category
                    .getId());

            // collection for the final result
            final List<DiscussionThread> result = new ArrayList<DiscussionThread>(
                    liferayThreads.size());
            for (final MBThread liferayThread : liferayThreads) {
                final DiscussionThread thread = wrapLiferayThread(
                        liferayThread, category);
                result.add(thread);
            }
            return result;
        } catch (final SystemException e) {
            log.error(String.format("Couldn't get threads for category %d.",
                    category.getId()), e);
            throw new DataSourceException(e);
        } catch (final PortalException e) {
            log.error(String.format("Couldn't get threads for category %d.",
                    category.getId()), e);
            throw new DataSourceException(e);
        }
    }

    private DiscussionThread wrapLiferayThread(final MBThread liferayThread,
            final Category category) throws PortalException, SystemException {
        // get the root message of the thread
        final MBMessage rootMessage = MBMessageLocalServiceUtil
                .getMessage(liferayThread.getRootMessageId());
        // get the author of the root message
        final com.liferay.portal.model.User liferayThreadAuthor = UserLocalServiceUtil
                .getUser(rootMessage.getUserId());
        final User threadAuthor = EntityFactoryUtil.createUser(
                liferayThreadAuthor, imagePath, liferayThreadAuthor.isFemale());
        // get the author of the last post
        final com.liferay.portal.model.User lastPostAuthorLiferay = UserLocalServiceUtil
                .getUser(liferayThread.getLastPostByUserId());
        final User lastPostAuthor = EntityFactoryUtil.createUser(
                lastPostAuthorLiferay, imagePath,
                lastPostAuthorLiferay.isFemale());

        final DiscussionThread thread = EntityFactoryUtil
                .createDiscussionThread(liferayThread, rootMessage,
                        threadAuthor, lastPostAuthor);
        thread.setCategory(category);
        return thread;
    }

    private List<MBThread> getLiferayThreadsForCategory(final long categoryId)
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
    public Category getCategory(final long categoryId)
            throws DataSourceException {
        try {
            return EntityFactoryUtil.createCategory(MBCategoryLocalServiceUtil
                    .getCategory(categoryId));
        } catch (final PortalException e) {
            log.error(String.format("Couldn't get category for id %d.",
                    categoryId), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format("Couldn't get category for id %d.",
                    categoryId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public long getThreadCount(final Category category)
            throws DataSourceException {
        ToriUtil.checkForNull(category, "Category must not be null.");
        try {
            return MBThreadLocalServiceUtil.getCategoryThreadsCount(
                    scopeGroupId, category.getId(),
                    WorkflowConstants.STATUS_APPROVED);
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get thread count for category %d.",
                    category.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public DiscussionThread getThread(final long threadId)
            throws DataSourceException {
        try {
            final MBThread thread = MBThreadLocalServiceUtil
                    .getMBThread(threadId);
            final Category category = EntityFactoryUtil
                    .createCategory(MBCategoryLocalServiceUtil
                            .getCategory(thread.getCategoryId()));
            return wrapLiferayThread(thread, category);
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't get thread for id %d.", threadId),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't get thread for id %d.", threadId),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public List<Post> getPosts(final DiscussionThread thread)
            throws DataSourceException {
        ToriUtil.checkForNull(thread, "DiscussionThread must not be null.");
        try {
            final List<MBMessage> messages = getLiferayPostsForThread(thread
                    .getId());

            final List<Post> result = new ArrayList<Post>(messages.size());
            for (final MBMessage message : messages) {
                final Post post = EntityFactoryUtil.createPost(message);
                post.setThread(thread);

                // get also the author
                final com.liferay.portal.model.User liferayUser = UserLocalServiceUtil
                        .getUser(message.getUserId());
                final User author = EntityFactoryUtil.createUser(liferayUser,
                        imagePath, liferayUser.isFemale());
                post.setAuthor(author);
                result.add(post);
            }
            return result;
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't get posts for thread %d.",
                            thread.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't get posts for thread %d.",
                            thread.getId()), e);
            throw new DataSourceException(e);
        }
    }

    private List<MBMessage> getLiferayPostsForThread(final long threadId)
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
    public void follow(final DiscussionThread thread)
            throws DataSourceException {
        try {
            SubscriptionLocalServiceUtil.addSubscription(currentUserId,
                    MBThread.class.getName(), thread.getId());
        } catch (final PortalException e) {
            log.error(String.format("Cannot follow thread %d", thread.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format("Cannot follow thread %d", thread.getId()),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void unFollow(final DiscussionThread thread) {
        try {
            SubscriptionLocalServiceUtil.deleteSubscription(currentUserId,
                    MBThread.class.getName(), thread.getId());
        } catch (final PortalException e) {
            log.error(
                    String.format("Cannot unfollow thread %d", thread.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Cannot unfollow thread %d", thread.getId()),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public boolean isFollowing(final DiscussionThread thread) {
        try {
            final com.liferay.portal.model.User user = UserLocalServiceUtil
                    .getUser(currentUserId);
            return SubscriptionLocalServiceUtil.isSubscribed(
                    user.getCompanyId(), user.getUserId(),
                    MBThread.class.getName(), thread.getId());
        } catch (final SystemException e) {
            log.error(String.format(
                    "Cannot check if user is following thread %d",
                    thread.getId()), e);
            throw new DataSourceException(e);
        } catch (final PortalException e) {
            log.error(String.format(
                    "Cannot check if user is following thread %d",
                    thread.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void delete(final Post post) throws DataSourceException {
        try {
            MBMessageLocalServiceUtil.deleteMBMessage(post.getId());
        } catch (final PortalException e) {
            log.error(String.format("Couldn't delete post %d.", post.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format("Couldn't delete post %d.", post.getId()),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public PostVote getPostVote(final Post post) throws DataSourceException {
        final PostVote vote = new PostVote();
        try {
            return EntityFactoryUtil
                    .createPostVote(RatingsEntryLocalServiceUtil.getEntry(
                            currentUserId, MBMessage.class.getName(),
                            post.getId()));
        } catch (final NoSuchEntryException e) {
            return vote;
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't get post vote for post %d.",
                            post.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't get post vote for post %d.",
                            post.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void upvote(final Post post) throws DataSourceException {
        ratePost(post, 1);
    }

    @Override
    public void downvote(final Post post) throws DataSourceException {
        ratePost(post, -1);
    }

    private void ratePost(final Post post, final int score)
            throws DataSourceException {
        ToriUtil.checkForNull(post, "Post must not be null.");
        try {
            RatingsEntryServiceUtil.updateEntry(MBMessage.class.getName(),
                    post.getId(), score);
        } catch (final PortalException e) {
            log.error(String.format("Couldn't rate post %d.", post.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't get post vote for post %d.",
                            post.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void removeUserVote(final Post post) throws DataSourceException {
        try {
            RatingsEntryServiceUtil.deleteEntry(MBMessage.class.getName(),
                    post.getId());
        } catch (final PortalException e) {
            log.error(String.format("Couldn't remove user vote for post %d.",
                    post.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format("Couldn't remove user vote for post %d.",
                    post.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public long getScore(final Post post) throws DataSourceException {
        try {
            final RatingsStats ratingsStats = RatingsStatsLocalServiceUtil
                    .getStats(MBMessage.class.getName(), post.getId());
            return (long) (ratingsStats.getAverageScore() * ratingsStats
                    .getTotalEntries());
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't get score for post %d.",
                            post.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void saveAsCurrentUser(final Post post) throws DataSourceException {

        internalSaveAsCurrentUser(post, getRootMessageId(post.getThread()));

    }

    private MBMessage internalSaveAsCurrentUser(final Post post,
            final long parentMessageId) throws DataSourceException {
        final DiscussionThread thread = post.getThread();
        final long groupId = scopeGroupId;
        final long categoryId = thread.getCategory().getId();
        final long threadId = thread.getId();

        String subject = post.getThread().getTopic();
        if (parentMessageId != MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID) {
            subject = "RE: " + subject;
        }
        final String body = post.getBodyRaw();
        final List<ObjectValuePair<String, byte[]>> files = Collections
                .emptyList();
        final boolean anonymous = false;
        final double priority = MBThreadConstants.PRIORITY_NOT_GIVEN;
        final boolean allowPingbacks = false;

        try {
            return MBMessageServiceUtil.addMessage(groupId, categoryId,
                    threadId, parentMessageId, subject, body, files, anonymous,
                    priority, allowPingbacks, mbMessageServiceContext);
        } catch (final PortalException e) {
            log.error("Couldn't save post.", e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error("Couldn't save post.", e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void move(final DiscussionThread thread,
            final Category destinationCategory) {
        log.warn("Not yet implemented.");
    }

    @Override
    @SuppressWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Incomplete method")
    public DiscussionThread sticky(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
        return null;
    }

    @Override
    @SuppressWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Incomplete method")
    public DiscussionThread unsticky(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
        return null;
    }

    @Override
    @SuppressWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Incomplete method")
    public DiscussionThread lock(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
        return null;
    }

    @Override
    @SuppressWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Incomplete method")
    public DiscussionThread unlock(final DiscussionThread thread) {
        log.warn("Not yet implemented.");
        return null;
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
                currentUserId = themeDisplay.getUserId();
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

    @Override
    public DiscussionThread saveNewThread(final DiscussionThread newThread,
            final Post firstPost) throws DataSourceException {
        final MBMessage savedRootMessage = internalSaveAsCurrentUser(firstPost,
                MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID);
        if (savedRootMessage != null) {
            final DiscussionThread savedThread = getThread(savedRootMessage
                    .getThreadId());
            if (savedThread != null) {
                return savedThread;
            }
        }
        // if we get this far, saving has failed -> throw exception
        throw new DataSourceException();
    }

}
