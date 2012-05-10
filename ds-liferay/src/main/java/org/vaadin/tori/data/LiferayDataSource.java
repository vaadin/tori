package org.vaadin.tori.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.entity.Attachment;
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
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.documentlibrary.store.DLStoreUtil;
import com.liferay.portlet.flags.service.FlagsEntryServiceUtil;
import com.liferay.portlet.messageboards.model.MBBan;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBThreadConstants;
import com.liferay.portlet.messageboards.service.MBBanServiceUtil;
import com.liferay.portlet.messageboards.service.MBCategoryLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBCategoryServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadFlagLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadServiceUtil;
import com.liferay.portlet.messageboards.util.comparator.MessageCreateDateComparator;
import com.liferay.portlet.ratings.NoSuchEntryException;
import com.liferay.portlet.ratings.model.RatingsStats;
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil;
import com.liferay.portlet.ratings.service.RatingsEntryServiceUtil;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;

import edu.umd.cs.findbugs.annotations.NonNull;

public class LiferayDataSource implements DataSource, PortletRequestAware {

    private static final Logger log = Logger.getLogger(LiferayDataSource.class);

    private static final long ROOT_CATEGORY_ID = 0;
    private static final int QUERY_ALL = com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS;

    // TODO this should be dynamic as it can be customized in liferay
    private static final double STICKY_PRIORITY = 2.0d;

    private long scopeGroupId = -1;
    private long companyId;
    private long currentUserId;
    private com.liferay.portal.model.User currentUser;
    private String imagePath;
    private String mainPath;

    private ServiceContext mbMessageServiceContext;
    private ServiceContext mbBanServiceContext;
    private ServiceContext flagsServiceContext;
    private ServiceContext mbCategoryServiceContext;

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
    public List<DiscussionThread> getThreads(final Category category,
            final int startIndex, int endIndex) throws DataSourceException {
        ToriUtil.checkForNull(category, "Category must not be null.");

        try {
            if (endIndex != QUERY_ALL) {
                // adjust the endIndex to be inclusive
                endIndex += 1;
            }
            final List<MBThread> liferayThreads = getLiferayThreadsForCategory(
                    category.getId(), startIndex, endIndex);

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

    @Override
    public List<DiscussionThread> getThreads(final Category category)
            throws DataSourceException {
        final int startIndex = QUERY_ALL; // use QUERY_ALL to get all
        final int endIndex = QUERY_ALL; // use QUERY_ALL get all
        return getThreads(category, startIndex, endIndex);
    }

    @Override
    public List<DiscussionThread> getRecentPosts() throws DataSourceException {
        try {
            final List<MBThread> liferayThreads = getLiferayRecentThreads();

            // collection for the final result
            final List<DiscussionThread> result = new ArrayList<DiscussionThread>(
                    liferayThreads.size());
            for (final MBThread liferayThread : liferayThreads) {
                final DiscussionThread thread = wrapLiferayThread(
                        liferayThread, null);
                result.add(thread);
            }
            return result;
        } catch (final SystemException e) {
            log.error("Couldn't get recent threads.", e);
            throw new DataSourceException(e);
        } catch (final PortalException e) {
            log.error("Couldn't get recent threads.", e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public List<DiscussionThread> getMyPosts() throws DataSourceException {
        try {
            final List<MBThread> liferayThreads = getLiferayMyPosts();

            // collection for the final result
            final List<DiscussionThread> result = new ArrayList<DiscussionThread>(
                    liferayThreads.size());
            for (final MBThread liferayThread : liferayThreads) {
                final DiscussionThread thread = wrapLiferayThread(
                        liferayThread, null);
                result.add(thread);
            }
            return result;
        } catch (final SystemException e) {
            log.error("Couldn't get my posts.", e);
            throw new DataSourceException(e);
        } catch (final PortalException e) {
            log.error("Couldn't get my posts.", e);
            throw new DataSourceException(e);
        }
    }

    private DiscussionThread wrapLiferayThread(final MBThread liferayThread,
            Category category) throws PortalException, SystemException,
            DataSourceException {
        // get the root message of the thread
        final MBMessage rootMessage = MBMessageLocalServiceUtil
                .getMessage(liferayThread.getRootMessageId());
        // get the author of the root message
        final User threadAuthor = getUser(rootMessage.getUserId());
        // get the author of the last post
        final User lastPostAuthor = getUser(liferayThread.getLastPostByUserId());

        if (category == null) {
            // fetch the category
            category = getCategory(liferayThread.getCategoryId());
        }

        final DiscussionThread thread = EntityFactoryUtil
                .createDiscussionThread(liferayThread, rootMessage,
                        threadAuthor, lastPostAuthor);
        thread.setCategory(category);
        thread.setSticky(liferayThread.getPriority() >= STICKY_PRIORITY);
        return thread;
    }

    private User getUser(final long userId) throws PortalException,
            SystemException {
        if (userId == 0) {
            return EntityFactoryUtil.createAnonymousUser(imagePath);
        } else {
            final com.liferay.portal.model.User liferayUser = UserLocalServiceUtil
                    .getUser(userId);
            if (liferayUser.isDefaultUser()) {
                return EntityFactoryUtil.createAnonymousUser(imagePath);
            } else {
                return EntityFactoryUtil.createUser(liferayUser, imagePath,
                        liferayUser.isFemale());
            }
        }
    }

    private List<MBThread> getLiferayThreadsForCategory(final long categoryId,
            final int start, final int end) throws SystemException {
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

    private List<MBThread> getLiferayRecentThreads() throws SystemException,
            PortalException {
        final int start = 0; // use QUERY_ALL if you'd like to get all
        final int end = 40; // use QUERY_ALL if you'd like to get all
        final List<MBThread> liferayThreads = MBThreadServiceUtil
                .getGroupThreads(scopeGroupId, 0,
                        WorkflowConstants.STATUS_APPROVED, false, false, start,
                        end);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Found %d recent threads.",
                    liferayThreads.size()));
        }
        return liferayThreads;
    }

    private List<MBThread> getLiferayMyPosts() throws SystemException,
            PortalException {
        final int start = 0; // use QUERY_ALL if you'd like to get all
        final int end = 40; // use QUERY_ALL if you'd like to get all
        final List<MBThread> liferayThreads = MBThreadServiceUtil
                .getGroupThreads(scopeGroupId, currentUserId,
                        WorkflowConstants.STATUS_ANY, start, end);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Found %d my threads.",
                    liferayThreads.size()));
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
    public long getThreadCountRecursively(final Category category)
            throws DataSourceException {
        ToriUtil.checkForNull(category, "Category must not be null.");
        try {
            long count = MBThreadLocalServiceUtil.getCategoryThreadsCount(
                    scopeGroupId, category.getId(),
                    WorkflowConstants.STATUS_APPROVED);

            // recursively add thread count of all sub categories
            final List<Category> subCategories = getSubCategories(category);
            for (final Category subCategory : subCategories) {
                count += getThreadCountRecursively(subCategory);
            }
            return count;
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get recursive thread count for category %d.",
                    category.getId()), e);
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
    public void incrementViewCount(final DiscussionThread thread)
            throws DataSourceException {
        try {
            // Reload the thread to get the latest view count.
            // Here we have a race condition, but this is the same way Liferay
            // handles the view count incrementation.
            final MBThread liferayThread = MBThreadLocalServiceUtil
                    .getThread(thread.getId());
            MBThreadLocalServiceUtil.updateThread(liferayThread.getThreadId(),
                    liferayThread.getViewCount() + 1);
        } catch (final PortalException e) {
            log.error(String.format(
                    "Couldn't increment view count for thread %d.",
                    thread.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't increment view count for thread %d.",
                    thread.getId()), e);
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
                final User author = getUser(message.getUserId());
                final List<Attachment> attachments = getAttachments(message);

                final Post post = EntityFactoryUtil.createPost(message, author,
                        thread, attachments);
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

    @NonNull
    private List<Attachment> getAttachments(final MBMessage message)
            throws PortalException, SystemException {
        if (message.isAttachments()) {
            final String[] filenames = message.getAttachmentsFiles();
            final List<Attachment> attachments = new ArrayList<Attachment>(
                    filenames.length);
            for (final String filename : filenames) {
                final String shortFilename = FileUtil
                        .getShortFileName(filename);
                final long fileSize = DLStoreUtil.getFileSize(companyId,
                        CompanyConstants.SYSTEM, filename);

                final Attachment attachment = new Attachment(shortFilename,
                        fileSize);
                attachment.setDownloadUrl(getAttachmentDownloadUrl(
                        shortFilename, message.getMessageId()));
                attachments.add(attachment);
            }
            return attachments;
        }
        return Collections.emptyList();
    }

    private String getAttachmentDownloadUrl(final String filename,
            final long messageId) throws SystemException {
        return mainPath + "/message_boards/get_message_attachment?messageId="
                + messageId + "&attachment=" + HttpUtil.encodeURL(filename);
    }

    private List<MBMessage> getLiferayPostsForThread(final long threadId)
            throws SystemException {
        @SuppressWarnings("unchecked")
        final Comparator<MBMessage> comparator = new MessageCreateDateComparator(
                true);

        final List<MBMessage> liferayPosts = MBMessageLocalServiceUtil
                .getThreadMessages(threadId, WorkflowConstants.STATUS_APPROVED,
                        comparator);
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
    public void save(final Category categoryToSave) throws DataSourceException {
        try {
            if (categoryToSave.getId() > 0) {
                log.debug("Updating existing category: "
                        + categoryToSave.getName());
                final MBCategory category = MBCategoryLocalServiceUtil
                        .getCategory(categoryToSave.getId());
                EntityFactoryUtil.copyFields(categoryToSave, category);
                MBCategoryLocalServiceUtil.updateMBCategory(category);
            } else {
                log.debug("Adding new category: " + categoryToSave.getName());
                final long parentCategoryId = categoryToSave
                        .getParentCategory() != null ? categoryToSave
                        .getParentCategory().getId() : ROOT_CATEGORY_ID;

                MBCategoryServiceUtil.addCategory(parentCategoryId,
                        categoryToSave.getName(),
                        categoryToSave.getDescription(), null, null, null,
                        null, 0, false, null, null, 0, null, false, null, 0,
                        false, null, null, false, false,
                        mbCategoryServiceContext);
            }
        } catch (final PortalException e) {
            log.error(
                    String.format("Cannot save category %d",
                            categoryToSave.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Cannot save category %d",
                            categoryToSave.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void delete(final Category categoryToDelete)
            throws DataSourceException {
        try {
            MBCategoryServiceUtil.deleteCategory(scopeGroupId,
                    categoryToDelete.getId());
        } catch (final PortalException e) {
            log.error(
                    String.format("Cannot delete category %d",
                            categoryToDelete.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Cannot delete category %d",
                            categoryToDelete.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void reportPost(final PostReport report) {
        String reporterEmailAddress = "";
        try {
            reporterEmailAddress = getCurrentUser().getEmailAddress();
        } catch (final PortalException e) {
            log.error("Couldn't get the email address of current user.", e);
        } catch (final SystemException e) {
            log.error("Couldn't get the email address of current user.", e);
        }

        final long reportedUserId = report.getPost().getAuthor().getId();
        final String contentTitle = report.getPost().getThread().getTopic();
        final String contentURL = report.getPostUrl();
        String reason = report.getReason().toString();
        if (report.getAdditionalInfo().length() > 0) {
            reason += ": " + report.getAdditionalInfo();
        }

        FlagsEntryServiceUtil.addEntry(MBMessage.class.getName(), report
                .getPost().getId(), reporterEmailAddress, reportedUserId,
                contentTitle, contentURL, reason, flagsServiceContext);
    }

    @Override
    public long getUnreadThreadCount(final Category category)
            throws DataSourceException {
        if (currentUserId <= 0) {
            return 0;
        }

        // FIXME Directly accessing Liferay's JDBC DataSource seems very
        // fragile, but the most straightforward way to access the total unread
        // count.
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        final long totalThreadCount = getThreadCountRecursively(category);
        try {
            connection = JdbcUtil.getJdbcConnection();
            statement = connection
                    .prepareStatement("select (? - count(MBThreadFlag.threadFlagId)) "
                            + "from MBThreadFlag, MBThread "
                            + "where MBThreadFlag.threadId = MBThread.threadId "
                            + "and MBThread.categoryId = ? and MBThreadFlag.userId = ?");

            statement.setLong(1, totalThreadCount);
            statement.setLong(2, category.getId());
            statement.setLong(3, currentUserId);

            result = statement.executeQuery();
            if (result.next()) {
                return result.getLong(1);
            } else {
                return 0;
            }
        } catch (final SQLException e) {
            log.error(e);
            throw new DataSourceException(e);
        } finally {
            JdbcUtil.closeAndLogException(result);
            JdbcUtil.closeAndLogException(statement);
            JdbcUtil.closeAndLogException(connection);
        }
    }

    @Override
    public void save(final Post post) {
        log.warn("Not yet implemented.");
    }

    @Override
    public void ban(final User user) throws DataSourceException {
        try {
            MBBanServiceUtil.addBan(user.getId(), mbBanServiceContext);
        } catch (final PortalException e) {
            log.error(String.format("Cannot ban user %d", user.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format("Cannot ban user %d", user.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void follow(final DiscussionThread thread)
            throws DataSourceException {
        try {
            SubscriptionLocalServiceUtil.addSubscription(currentUserId,
                    currentUser.getGroupId(), MBThread.class.getName(),
                    thread.getId());
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
    public void unFollow(final DiscussionThread thread)
            throws DataSourceException {
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
    public boolean isFollowing(final DiscussionThread thread)
            throws DataSourceException {
        if (currentUserId <= 0) {
            return false;
        }

        try {
            final com.liferay.portal.model.User user = getCurrentUser();
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
    public void markRead(final DiscussionThread thread)
            throws DataSourceException {
        if (currentUserId > 0) {
            try {
                MBThreadFlagLocalServiceUtil.addThreadFlag(currentUserId,
                        MBThreadLocalServiceUtil.getThread(thread.getId()));
            } catch (final PortalException e) {
                log.error(String.format("Couldn't mark thread %d as read.",
                        thread.getId()), e);
                throw new DataSourceException(e);
            } catch (final SystemException e) {
                log.error(String.format("Couldn't mark thread %d as read.",
                        thread.getId()), e);
                throw new DataSourceException(e);
            }
        }
    }

    @Override
    public boolean isRead(final DiscussionThread thread)
            throws DataSourceException {
        if (currentUserId > 0) {
            try {
                return MBThreadFlagLocalServiceUtil.hasThreadFlag(
                        currentUserId,
                        MBThreadLocalServiceUtil.getThread(thread.getId()));
            } catch (final PortalException e) {
                log.error(String.format(
                        "Couldn't check for read flag on thread %d.",
                        thread.getId()), e);
                throw new DataSourceException(e);
            } catch (final SystemException e) {
                log.error(String.format(
                        "Couldn't check for read flag on thread %d.",
                        thread.getId()), e);
                throw new DataSourceException(e);
            }
        }
        // default to read in case of an anonymous user
        return true;
    }

    @Override
    public void delete(final Post post) throws DataSourceException {
        try {
            MBMessageServiceUtil.deleteMessage(post.getId());
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
    public Post saveAsCurrentUser(final Post post,
            final Map<String, byte[]> files) throws DataSourceException {
        try {
            final MBMessage newPost = internalSaveAsCurrentUser(post, files,
                    getRootMessageId(post.getThread()));
            return EntityFactoryUtil.createPost(newPost,
                    getUser(currentUserId), getThread(newPost.getThreadId()),
                    getAttachments(newPost));
        } catch (final PortalException e) {
            log.error("Couldn't save post.", e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error("Couldn't save post.", e);
            throw new DataSourceException(e);
        }
    }

    private MBMessage internalSaveAsCurrentUser(final Post post,
            final Map<String, byte[]> files, final long parentMessageId)
            throws PortalException, SystemException {
        final DiscussionThread thread = post.getThread();
        final long groupId = scopeGroupId;
        final long categoryId = thread.getCategory().getId();
        final long threadId = thread.getId();

        String subject = post.getThread().getTopic();
        if (parentMessageId != MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID) {
            subject = "RE: " + subject;
        }
        final String body = post.getBodyRaw();
        final List<ObjectValuePair<String, InputStream>> attachments = new ArrayList<ObjectValuePair<String, InputStream>>();

        if (files != null) {
            for (final Entry<String, byte[]> file : files.entrySet()) {
                final String fileName = file.getKey();
                final byte[] bytes = file.getValue();

                if ((bytes != null) && (bytes.length > 0)) {
                    final ObjectValuePair<String, InputStream> ovp = new ObjectValuePair<String, InputStream>(
                            fileName, new ByteArrayInputStream(bytes));

                    attachments.add(ovp);
                }
            }
        }

        final boolean anonymous = false;
        final double priority = MBThreadConstants.PRIORITY_NOT_GIVEN;
        final boolean allowPingbacks = false;

        return MBMessageServiceUtil.addMessage(groupId, categoryId, threadId,
                parentMessageId, subject, body, "UTF-8", attachments,
                anonymous, priority, allowPingbacks, mbMessageServiceContext);

    }

    @Override
    public void move(final DiscussionThread thread,
            final Category destinationCategory) throws DataSourceException {
        try {
            MBThreadLocalServiceUtil.moveThread(scopeGroupId,
                    destinationCategory.getId(), thread.getId());
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't move thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't move thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        }

    }

    @Override
    public DiscussionThread sticky(final DiscussionThread thread)
            throws DataSourceException {
        updateThreadPriority(thread, STICKY_PRIORITY);
        thread.setSticky(true);
        return thread;
    }

    @Override
    public DiscussionThread unsticky(final DiscussionThread thread)
            throws DataSourceException {
        updateThreadPriority(thread, MBThreadConstants.PRIORITY_NOT_GIVEN);
        thread.setSticky(false);
        return thread;
    }

    private void updateThreadPriority(final DiscussionThread thread,
            final double newPriority) throws DataSourceException {
        try {
            final MBThread liferayThread = MBThreadLocalServiceUtil
                    .getThread(thread.getId());
            liferayThread.setPriority(newPriority);
            MBThreadLocalServiceUtil.updateMBThread(liferayThread);
        } catch (final PortalException e) {
            log.error(String.format("Couldn't change priority for thread %d.",
                    thread.getId()), e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(String.format("Couldn't change priority for thread %d.",
                    thread.getId()), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public DiscussionThread lock(final DiscussionThread thread)
            throws DataSourceException {
        try {
            MBThreadServiceUtil.lockThread(thread.getId());
            thread.setLocked(true);
            return thread;
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't lock thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't lock thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public DiscussionThread unlock(final DiscussionThread thread)
            throws DataSourceException {
        try {
            MBThreadServiceUtil.unlockThread(thread.getId());
            thread.setLocked(false);
            return thread;
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't unlock thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't unlock thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void delete(final DiscussionThread thread)
            throws DataSourceException {
        try {
            MBThreadLocalServiceUtil.deleteMBThread(thread.getId());
        } catch (final PortalException e) {
            log.error(
                    String.format("Couldn't delete thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error(
                    String.format("Couldn't delete thread %d.", thread.getId()),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void setRequest(final PortletRequest request) {
        final ThemeDisplay themeDisplay = (ThemeDisplay) request
                .getAttribute("THEME_DISPLAY");

        if (themeDisplay != null) {
            if (scopeGroupId < 0) {
                // scope not defined yet -> get if from the theme display
                scopeGroupId = themeDisplay.getScopeGroupId();
                log.info("Using groupId " + scopeGroupId + " as the scope.");
            }
            long remoteUser = 0;
            if (request.getRemoteUser() != null) {
                remoteUser = Long.valueOf(request.getRemoteUser());
            }
            if (currentUserId != remoteUser) {
                // current user is changed
                currentUserId = remoteUser;
                currentUser = null;
            }
            if (imagePath == null) {
                imagePath = themeDisplay.getPathImage();
            }
            mainPath = themeDisplay.getPathMain();
            companyId = themeDisplay.getCompanyId();
        }

        try {
            mbMessageServiceContext = ServiceContextFactory.getInstance(
                    MBMessage.class.getName(), request);
            mbBanServiceContext = ServiceContextFactory.getInstance(
                    MBBan.class.getName(), request);
            mbCategoryServiceContext = ServiceContextFactory.getInstance(
                    MBCategory.class.getName(), request);
            flagsServiceContext = ServiceContextFactory.getInstance(
                    "com.liferay.portlet.flags.model.FlagsEntry", request);
        } catch (final PortalException e) {
            log.error("Couldn't create ServiceContext.", e);
        } catch (final SystemException e) {
            log.error("Couldn't create ServiceContext.", e);
        }
    }

    private com.liferay.portal.model.User getCurrentUser()
            throws PortalException, SystemException {
        if (currentUser == null && currentUserId > 0) {
            currentUser = UserLocalServiceUtil.getUser(currentUserId);
        }
        return currentUser;
    }

    @Override
    public DiscussionThread saveNewThread(final DiscussionThread newThread,
            final Map<String, byte[]> files, final Post firstPost)
            throws DataSourceException {
        try {
            final MBMessage savedRootMessage = internalSaveAsCurrentUser(
                    firstPost, files,
                    MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID);
            if (savedRootMessage != null) {
                final DiscussionThread savedThread = getThread(savedRootMessage
                        .getThreadId());
                if (savedThread != null) {
                    return savedThread;
                }
            }
        } catch (final PortalException e) {
            log.error("Couldn't save new thread.", e);
            throw new DataSourceException(e);
        } catch (final SystemException e) {
            log.error("Couldn't save new thread.", e);
            throw new DataSourceException(e);
        }
        // if we get this far, saving has failed -> throw exception
        throw new DataSourceException();
    }

    @Override
    public int getAttachmentMaxFileSize() {
        try {
            return Integer.parseInt(PrefsPropsUtil
                    .getString(PropsKeys.DL_FILE_MAX_SIZE));
        } catch (final Exception e) {
            log.error("Couldn't get max file size");
            return 307200;
        }
    }
}
