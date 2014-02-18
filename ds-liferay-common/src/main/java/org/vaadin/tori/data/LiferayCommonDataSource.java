/*
 * Copyright 2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.Configuration;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.LiferayCommonEntityFactoryUtil;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.post.PostReport.Reason;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.flags.service.FlagsEntryServiceUtil;
import com.liferay.portlet.messageboards.NoSuchCategoryException;
import com.liferay.portlet.messageboards.NoSuchMessageException;
import com.liferay.portlet.messageboards.NoSuchThreadException;
import com.liferay.portlet.messageboards.model.MBBan;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBThreadConstants;
import com.liferay.portlet.messageboards.service.MBBanLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBBanServiceUtil;
import com.liferay.portlet.messageboards.service.MBCategoryLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBCategoryServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadServiceUtil;
import com.liferay.portlet.messageboards.util.comparator.MessageCreateDateComparator;
import com.liferay.portlet.ratings.NoSuchEntryException;
import com.liferay.portlet.ratings.model.RatingsEntry;
import com.liferay.portlet.ratings.model.RatingsStats;
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil;
import com.liferay.portlet.ratings.service.RatingsEntryServiceUtil;
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil;

public abstract class LiferayCommonDataSource implements DataSource,
        PortletRequestAware {

    private static final boolean INCLUDE_SUBSCRIBED = false;
    private static final boolean INCLUDE_ANONYMOUS = false;

    private static final Logger log = Logger
            .getLogger(LiferayCommonDataSource.class);

    private static final long ROOT_CATEGORY_ID = 0;
    private static final int QUERY_ALL = com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS;
    // TODO this should be dynamic as it can be customized in liferay
    private static final double STICKY_PRIORITY = 2.0d;

    protected long scopeGroupId = -1;
    protected long currentUserId;
    protected com.liferay.portal.model.User currentUser;
    private String imagePath;

    private ServiceContext mbBanServiceContext;
    protected ServiceContext flagsServiceContext;
    protected ServiceContext mbCategoryServiceContext;
    protected ServiceContext mbMessageServiceContext;

    private PortletPreferences portletPreferences;
    protected ThemeDisplay themeDisplay;

    private static final String PREFS_ANALYTICS_ID = "analytics";
    private static final String PREFS_REPLACE_MESSAGE_BOARDS_LINKS = "toriReplaceMessageBoardsLinks";
    private static final String PREFS_UPDATE_PAGE_TITLE = "toriUpdatePageTitle";
    private static final String PREFS_PAGE_TITLE_PREFIX = "toriPageTitlePrefix";

    private static final String PREFS_PATHROOT = "pathroot";

    private static final String URL_PREFIX = "/#!/";
    private static final String CATEGORIES = URL_PREFIX + "category/";
    private static final String THREADS = URL_PREFIX + "thread/";

    private static final String PREFS_REPLACEMENTS_KEY = "toriPostReplacements";
    private static final String REPLACEMENT_SEPARATOR = "<TORI-REPLACEMENT>";

    @Override
    public List<Category> getSubCategories(Long categoryId)
            throws DataSourceException {
        final long parentCategoryId = normalizeCategoryId(categoryId);

        try {
            List<MBCategory> categories = MBCategoryLocalServiceUtil
                    .getCategories(scopeGroupId, parentCategoryId, QUERY_ALL,
                            QUERY_ALL);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Found %d categories.",
                        categories.size()));
            }
            return LiferayCommonEntityFactoryUtil.createCategories(categories);
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get subcategories for parent category %d.",
                    parentCategoryId), e);
            throw new DataSourceException(e);
        }
    }

    public static long getRootMessageId(final long threadId)
            throws DataSourceException {
        try {
            final MBThread liferayThread = MBThreadLocalServiceUtil
                    .getMBThread(threadId);
            return liferayThread.getRootMessageId();
        } catch (final NestableException e) {
            log.error(String.format(
                    "Couldn't get root message id for thread %d.", threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public List<DiscussionThread> getThreads(final Long categoryId,
            final int startIndex, int endIndex) throws DataSourceException {
        try {
            if (endIndex != QUERY_ALL) {
                // adjust the endIndex to be inclusive
                endIndex += 1;
            }
            final List<MBThread> liferayThreads = getLiferayThreadsForCategory(
                    normalizeCategoryId(categoryId), startIndex, endIndex);

            final Category category = getCategory(categoryId);

            // collection for the final result
            final List<DiscussionThread> result = new ArrayList<DiscussionThread>(
                    liferayThreads.size());
            for (final MBThread liferayThread : liferayThreads) {
                final DiscussionThread thread = wrapLiferayThread(
                        liferayThread, category);
                result.add(thread);
            }
            return result;
        } catch (final NestableException e) {
            log.error(String.format("Couldn't get threads for category %d.",
                    categoryId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category)
            throws DataSourceException {
        final int startIndex = QUERY_ALL; // use QUERY_ALL to get all
        final int endIndex = QUERY_ALL; // use QUERY_ALL get all
        return getThreads(category.getId(), startIndex, endIndex);
    }

    @Override
    public List<DiscussionThread> getRecentPosts(final int from, final int to)
            throws DataSourceException {
        // TODO
        try {
            final List<MBThread> liferayThreads = getLiferayRecentThreads(from,
                    to);

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
    public int getRecentPostsCount() throws DataSourceException {
        try {
            return MBThreadServiceUtil.getGroupThreadsCount(scopeGroupId, 0,
                    WorkflowConstants.STATUS_APPROVED, INCLUDE_ANONYMOUS,
                    INCLUDE_SUBSCRIBED);
        } catch (final SystemException e) {
            log.error("Couldn't get amount of recent threads.", e);
            throw new DataSourceException(e);
        }
    };

    @Override
    public List<DiscussionThread> getMyPostThreads(final int from, final int to)
            throws DataSourceException {
        try {
            final List<MBThread> liferayThreads = getLiferayMyPosts(from, to);

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

    @Override
    public int getMyPostThreadsCount() throws DataSourceException {
        try {
            final int groupThreadsCount = MBThreadServiceUtil
                    .getGroupThreadsCount(scopeGroupId, currentUserId,
                            WorkflowConstants.STATUS_ANY);
            log.debug("LiferayDataSource.getMyPostThreadsCount(): "
                    + groupThreadsCount);
            return groupThreadsCount;
        } catch (final SystemException e) {
            log.error("Couldn't get my posts' count.", e);
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

        final DiscussionThread thread = LiferayCommonEntityFactoryUtil
                .createDiscussionThread(liferayThread, rootMessage,
                        threadAuthor, lastPostAuthor);
        thread.setCategory(category);
        thread.setSticky(liferayThread.getPriority() >= STICKY_PRIORITY);
        return thread;
    }

    private User getUser(final long userId) throws PortalException,
            SystemException {
        if (userId == 0) {
            return LiferayCommonEntityFactoryUtil
                    .createAnonymousUser(imagePath);
        } else {
            final com.liferay.portal.model.User liferayUser = UserLocalServiceUtil
                    .getUser(userId);
            if (liferayUser.isDefaultUser()) {
                return LiferayCommonEntityFactoryUtil
                        .createAnonymousUser(imagePath);
            } else {
                final boolean isBanned = MBBanLocalServiceUtil.hasBan(
                        scopeGroupId, liferayUser.getUserId());
                return LiferayCommonEntityFactoryUtil.createUser(liferayUser,
                        imagePath, liferayUser.isFemale(), isBanned);
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

    private List<MBThread> getLiferayRecentThreads(final int start,
            final int end) throws SystemException, PortalException {
        return MBThreadServiceUtil.getGroupThreads(scopeGroupId, 0,
                WorkflowConstants.STATUS_APPROVED, INCLUDE_ANONYMOUS,
                INCLUDE_SUBSCRIBED, start, end + 1);
    }

    private List<MBThread> getLiferayMyPosts(final int start, final int end)
            throws SystemException, PortalException {
        return MBThreadServiceUtil.getGroupThreads(scopeGroupId, currentUserId,
                WorkflowConstants.STATUS_ANY, start, end + 1);
    }

    @Override
    public Category getCategory(final Long categoryId)
            throws DataSourceException {
        try {
            return LiferayCommonEntityFactoryUtil
                    .createCategory(MBCategoryLocalServiceUtil
                            .getCategory(normalizeCategoryId(categoryId)));
        } catch (final NoSuchCategoryException e) {
            throw new org.vaadin.tori.exception.NoSuchCategoryException(
                    categoryId, e);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't get category for id %d.",
                    categoryId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public int getThreadCountRecursively(Long categoryId)
            throws DataSourceException {
        try {
            int count = MBThreadLocalServiceUtil.getCategoryThreadsCount(
                    scopeGroupId, normalizeCategoryId(categoryId),
                    WorkflowConstants.STATUS_APPROVED);

            // recursively add thread count of all sub categories
            List<MBCategory> subCategories = MBCategoryLocalServiceUtil
                    .getCategories(scopeGroupId, categoryId, QUERY_ALL,
                            QUERY_ALL);
            for (final MBCategory subCategory : subCategories) {
                count += getThreadCountRecursively(subCategory.getCategoryId());
            }
            return count;
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get recursive thread count for category %d.",
                    categoryId), e);
            throw new DataSourceException(e);
        }
    }

    protected Collection<Long> getCategoryIdsRecursively(Long rootCategoryId)
            throws DataSourceException {
        Collection<Long> categories = new ArrayList<Long>();
        categories.add(rootCategoryId);
        try {
            List<MBCategory> subCategories = MBCategoryLocalServiceUtil
                    .getCategories(scopeGroupId, rootCategoryId, QUERY_ALL,
                            QUERY_ALL);
            for (final MBCategory subCategory : subCategories) {
                categories.addAll(getCategoryIdsRecursively(subCategory
                        .getCategoryId()));
            }
            return categories;
        } catch (final SystemException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public int getThreadCount(Long categoryId) throws DataSourceException {
        try {
            return MBThreadLocalServiceUtil.getCategoryThreadsCount(
                    scopeGroupId, normalizeCategoryId(categoryId),
                    WorkflowConstants.STATUS_APPROVED);
        } catch (final SystemException e) {
            log.error(String.format(
                    "Couldn't get thread count for category %d.", categoryId),
                    e);
            throw new DataSourceException(e);
        }
    }

    public static long normalizeCategoryId(Long categoryId) {
        return categoryId == null ? ROOT_CATEGORY_ID : categoryId;
    }

    @Override
    public DiscussionThread getThread(final long threadId)
            throws DataSourceException {
        try {
            final MBThread thread = MBThreadLocalServiceUtil
                    .getMBThread(threadId);
            final Category category = LiferayCommonEntityFactoryUtil
                    .createCategory(MBCategoryLocalServiceUtil
                            .getCategory(thread.getCategoryId()));
            return wrapLiferayThread(thread, category);
        } catch (final NoSuchThreadException e) {
            throw new org.vaadin.tori.exception.NoSuchThreadException(threadId,
                    e);
        } catch (final NestableException e) {
            log.error(
                    String.format("Couldn't get thread for id %d.", threadId),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
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
    public List<Post> getPosts(long threadId) throws DataSourceException {
        try {
            final List<MBMessage> messages = getLiferayPostsForThread(threadId);

            final List<Post> result = new ArrayList<Post>(messages.size());
            final DiscussionThread thread = getThread(threadId);
            for (final MBMessage message : messages) {
                final User author = getUser(message.getUserId());
                final List<Attachment> attachments = getAttachments(message);

                final Post post = LiferayCommonEntityFactoryUtil.createPost(
                        message, author, thread, attachments);
                if (getReplaceMessageBoardsLinks()) {
                    String bodyRaw = post.getBodyRaw();
                    bodyRaw = replaceMessageBoardsLinksCategories(bodyRaw);
                    bodyRaw = replaceMessageBoardsLinksMessages(bodyRaw);
                    post.setBodyRaw(bodyRaw);
                }
                result.add(post);
            }
            return result;
        } catch (final NestableException e) {
            log.error(String.format("Couldn't get posts for thread %d.",
                    threadId), e);
            throw new DataSourceException(e);
        }
    }

    private String replaceMessageBoardsLinksCategories(String bodyRaw) {
        // Liferay 6.0 pattern
        final Pattern pattern = Pattern.compile(
                "/-/message_boards\\?[_,\\d]+mbCategoryId=\\d+",
                Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(bodyRaw);
        while (matcher.find()) {
            final String group = matcher.group();
            final String category = "mbCategoryId=";
            final String categoryIdString = group.substring(group
                    .indexOf(category) + category.length());
            final String fragment = CATEGORIES + categoryIdString;
            bodyRaw = bodyRaw.replaceFirst(group.replaceAll("\\?", "\\\\?"),
                    fragment);
        }

        // Liferay 6.1 pattern
        final Pattern pattern61 = Pattern.compile(
                "/-/message_boards/category/\\d+", Pattern.CASE_INSENSITIVE);
        final Matcher matcher61 = pattern61.matcher(bodyRaw);
        while (matcher61.find()) {
            final String group = matcher61.group();
            final String categoryIdString = group.substring(group
                    .lastIndexOf('/') + 1);
            final String fragment = CATEGORIES + categoryIdString;
            bodyRaw = bodyRaw.replaceFirst(group, fragment);
        }

        return bodyRaw;
    }

    private String replaceMessageBoardsLinksMessages(String bodyRaw) {
        final Pattern pattern = Pattern
                .compile(
                        "/-/message_boards/(view_)?message/\\d+(#[_,\\d]+message_\\d+)?",
                        Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(bodyRaw);
        while (matcher.find()) {
            final String group = matcher.group();
            String messageIdString = group
                    .substring(group.lastIndexOf('/') + 1);
            if (messageIdString.contains("#")) {
                messageIdString = messageIdString.substring(0,
                        messageIdString.indexOf('#'));
            }
            long messageId = Long.parseLong(messageIdString);
            try {
                final MBMessage message = MBMessageLocalServiceUtil
                        .getMBMessage(messageId);

                final long threadId = message.getThreadId();
                final String messagePrefix = "_message_";
                final int messageIdIndex = group.indexOf(messagePrefix);
                if (messageIdIndex > -1) {
                    messageId = Long.parseLong(group.substring(messageIdIndex
                            + messagePrefix.length()));
                }

                final String fragment = THREADS + threadId + "/" + messageId;

                bodyRaw = bodyRaw.replaceFirst(group, fragment);
            } catch (final NestableException e) {
                log.warn("Unable to get MBmessage for id: " + messageId);
            }
        }

        return bodyRaw;
    }

    protected abstract List<Attachment> getAttachments(final MBMessage message)
            throws NestableException;

    private List<MBMessage> getLiferayPostsForThread(final long threadId)
            throws SystemException {
        @SuppressWarnings("unchecked")
        final Comparator<MBMessage> comparator = new MessageCreateDateComparator(
                true);
        return MBMessageLocalServiceUtil.getThreadMessages(threadId,
                WorkflowConstants.STATUS_APPROVED, comparator);
    }

    @Override
    public void updateCategory(long categoryId, String name, String description)
            throws DataSourceException {
        try {
            log.debug("Updating existing category: " + categoryId);
            final MBCategory category = MBCategoryLocalServiceUtil
                    .getCategory(categoryId);
            category.setName(name);
            category.setDescription(description);
            MBCategoryLocalServiceUtil.updateMBCategory(category);
        } catch (NestableException e) {
            log.error(String.format("Cannot save category %d", categoryId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void deleteCategory(long categoryId) throws DataSourceException {
        try {
            MBCategoryServiceUtil.deleteCategory(scopeGroupId, categoryId);
        } catch (final NestableException e) {
            log.error(String.format("Cannot delete category %d", categoryId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void reportPost(long postId, Reason reason, String additionalInfo,
            String postUrl) {
        String reporterEmailAddress = "";
        try {
            reporterEmailAddress = getCurrentLiferayUser().getEmailAddress();
        } catch (final PortalException e) {
            log.error("Couldn't get the email address of current user.", e);
        } catch (final SystemException e) {
            log.error("Couldn't get the email address of current user.", e);
        }

        try {
            Post post = getPost(postId);
            final long reportedUserId = post.getAuthor().getId();
            final String contentTitle = post.getThread().getTopic();
            final String contentURL = postUrl;
            String reasonString = reason.toString();
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                reasonString += ": " + additionalInfo;
            }

            FlagsEntryServiceUtil.addEntry(MBMessage.class.getName(), postId,
                    reporterEmailAddress, reportedUserId, contentTitle,
                    contentURL, reasonString, flagsServiceContext);
        } catch (DataSourceException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void savePost(long postId, String bodyRaw) {
        try {
            // Currently only editing of message body allowed
            MBMessageLocalServiceUtil.updateMessage(postId, bodyRaw);
        } catch (final Exception e) {
            log.error("Editing message failed", e);
        }
    }

    @Override
    public void banUser(long userId) throws DataSourceException {
        try {
            MBBanServiceUtil.addBan(userId, mbBanServiceContext);
        } catch (NestableException e) {
            log.error(String.format("Cannot ban user %d", userId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void unbanUser(long userId) throws DataSourceException {
        try {
            MBBanServiceUtil.deleteBan(userId, mbBanServiceContext);
        } catch (final NestableException e) {
            log.error(String.format("Cannot unban user %d", userId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void unfollowThread(long threadId) throws DataSourceException {
        try {
            SubscriptionLocalServiceUtil.deleteSubscription(currentUserId,
                    MBThread.class.getName(), threadId);
        } catch (final NestableException e) {
            log.error(String.format("Cannot unfollow thread %d", threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public boolean isFollowingThread(long threadId) {
        boolean result = false;
        if (currentUserId > 0) {
            try {
                final com.liferay.portal.model.User user = getCurrentLiferayUser();
                result = SubscriptionLocalServiceUtil.isSubscribed(
                        user.getCompanyId(), user.getUserId(),
                        MBThread.class.getName(), threadId);
            } catch (final NestableException e) {
                log.error(String
                        .format("Cannot check if user is following thread %d",
                                threadId), e);
            }
        }
        return result;
    }

    @Override
    public void deletePost(long postId) throws DataSourceException {
        try {
            MBMessageServiceUtil.deleteMessage(postId);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't delete post %d.", postId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public Boolean getPostVote(long postId) throws DataSourceException {
        Boolean result = null;
        try {
            RatingsEntry entry = RatingsEntryLocalServiceUtil.getEntry(
                    currentUserId, MBMessage.class.getName(), postId);
            if (entry != null) {
                result = entry.getScore() > 0;
            }
        } catch (final NoSuchEntryException e) {
        } catch (final NestableException e) {
            log.error(String.format("Couldn't get post vote for post %d.",
                    postId), e);
            throw new DataSourceException(e);
        }
        return result;
    }

    @Override
    public void upvote(long postId) throws DataSourceException {
        ratePost(postId, 1);
    }

    @Override
    public void downvote(long postId) throws DataSourceException {
        ratePost(postId, -1);
    }

    private void ratePost(long postId, final int score)
            throws DataSourceException {
        try {
            RatingsEntryServiceUtil.updateEntry(MBMessage.class.getName(),
                    postId, score);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't rate post %d.", postId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void removeUserVote(long postId) throws DataSourceException {
        try {
            RatingsEntryServiceUtil.deleteEntry(MBMessage.class.getName(),
                    postId);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't remove user vote for post %d.",
                    postId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public long getPostScore(long postId) throws DataSourceException {
        try {
            final RatingsStats ratingsStats = RatingsStatsLocalServiceUtil
                    .getStats(MBMessage.class.getName(), postId);
            return (long) (ratingsStats.getAverageScore() * ratingsStats
                    .getTotalEntries());
        } catch (final SystemException e) {
            log.error(String.format("Couldn't get score for post %d.", postId),
                    e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public Post saveReply(String rawBody, Map<String, byte[]> attachments,
            long threadId) throws DataSourceException {
        try {
            final MBMessage newPost = internalSaveAsCurrentUser(rawBody,
                    attachments, getThread(threadId),
                    getRootMessageId(threadId));
            final Post post2 = LiferayCommonEntityFactoryUtil.createPost(
                    newPost, getUser(currentUserId),
                    getThread(newPost.getThreadId()), getAttachments(newPost));
            if (getReplaceMessageBoardsLinks()) {
                String bodyRaw = post2.getBodyRaw();
                bodyRaw = replaceMessageBoardsLinksCategories(bodyRaw);
                bodyRaw = replaceMessageBoardsLinksMessages(bodyRaw);
                post2.setBodyRaw(bodyRaw);
            }
            return post2;
        } catch (final NestableException e) {
            log.error("Couldn't save post.", e);
            throw new DataSourceException(e);
        }
    }

    protected abstract MBMessage internalSaveAsCurrentUser(
            final String rawBody, final Map<String, byte[]> files,
            DiscussionThread thread, final long parentMessageId)
            throws PortalException, SystemException;

    @Override
    public void moveThread(final long threadId, Long destinationCategoryId)
            throws DataSourceException {
        try {
            MBThreadLocalServiceUtil.moveThread(scopeGroupId,
                    destinationCategoryId, threadId);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't move thread %d.", threadId), e);
            throw new DataSourceException(e);
        }

    }

    @Override
    public void stickyThread(long threadId) throws DataSourceException {
        updateThreadPriority(threadId, STICKY_PRIORITY);
    }

    @Override
    public void unstickyThread(long threadId) throws DataSourceException {
        updateThreadPriority(threadId, MBThreadConstants.PRIORITY_NOT_GIVEN);
    }

    private void updateThreadPriority(long threadId, final double newPriority)
            throws DataSourceException {
        try {
            final MBThread liferayThread = MBThreadLocalServiceUtil
                    .getThread(threadId);
            liferayThread.setPriority(newPriority);
            MBThreadLocalServiceUtil.updateMBThread(liferayThread);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't change priority for thread %d.",
                    threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void lockThread(long threadId) throws DataSourceException {
        try {
            MBThreadServiceUtil.lockThread(threadId);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't lock thread %d.", threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void unlockThread(long threadId) throws DataSourceException {
        try {
            MBThreadServiceUtil.unlockThread(threadId);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't unlock thread %d.", threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void deleteThread(long threadId) throws DataSourceException {
        try {
            MBThreadLocalServiceUtil.deleteMBThread(threadId);
        } catch (final NestableException e) {
            log.error(String.format("Couldn't delete thread %d.", threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public void setRequest(final PortletRequest request) {

        determineMessageBoardsParameters(request);

        themeDisplay = (ThemeDisplay) request
                .getAttribute(getThemeDisplayKey());

        if (themeDisplay != null) {
            if (scopeGroupId < 0) {
                // scope not defined yet -> get if from the theme display
                scopeGroupId = themeDisplay.getScopeGroupId();
                log.debug("Using groupId " + scopeGroupId + " as the scope.");
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
        }

        try {
            mbBanServiceContext = ServiceContextFactory.getInstance(
                    MBBan.class.getName(), request);
            flagsServiceContext = ServiceContextFactory.getInstance(
                    "com.liferay.portlet.flags.model.FlagsEntry", request);
            mbCategoryServiceContext = ServiceContextFactory.getInstance(
                    MBCategory.class.getName(), request);
            mbMessageServiceContext = ServiceContextFactory.getInstance(
                    MBMessage.class.getName(), request);
        } catch (final NestableException e) {
            log.error("Couldn't create ServiceContext.", e);
        }

        try {
            portletPreferences = PortletPreferencesFactoryUtil
                    .getPortletSetup(request);
        } catch (final NestableException e) {
            log.error("Couldn't load PortletPreferences.", e);
        }
    }

    protected abstract String getThemeDisplayKey();

    private static final String MESSAGEB_BOARDS_CATEGORY_ID = "mbCategoryId";
    private static final String MESSAGEB_BOARDS_MESSAGE_ID = "messageId";

    /** @see org.vaadin.tori.ToriApplication.TORI_CATEGORY_ID */
    private static final String TORI_CATEGORY_ID = "toriCategoryId";
    /** @see org.vaadin.tori.ToriApplication.TORI_THREAD_ID */
    private static final String TORI_THREAD_ID = "toriThreadId";
    /** @see org.vaadin.tori.ToriApplication.TORI_MESSAGE_ID */
    private static final String TORI_MESSAGE_ID = "toriMessageId";

    private Map<String, String> postReplacements;

    private void determineMessageBoardsParameters(final PortletRequest request) {
        final PortletSession session = request.getPortletSession();
        final Long categoryId = getOriginalRequestEntityIdParameter(request,
                MESSAGEB_BOARDS_CATEGORY_ID);
        if (categoryId == null) {
            final Long messageId = getOriginalRequestEntityIdParameter(request,
                    MESSAGEB_BOARDS_MESSAGE_ID);
            if (messageId != null) {
                try {
                    final MBMessage message = MBMessageLocalServiceUtil
                            .getMBMessage(messageId);
                    session.setAttribute(TORI_THREAD_ID, message.getThreadId());
                    session.setAttribute(TORI_MESSAGE_ID, messageId);
                } catch (final NestableException e) {
                    log.warn("Unable to load MBMessage for id: " + messageId, e);
                }
            }
        } else {
            session.setAttribute(TORI_CATEGORY_ID, categoryId);
        }
    }

    private Long getOriginalRequestEntityIdParameter(
            final PortletRequest request, final String key) {
        Long entityId = null;
        final HttpServletRequest originalRequest = PortalUtil
                .getOriginalServletRequest(PortalUtil
                        .getHttpServletRequest(request));

        @SuppressWarnings("rawtypes")
        final Map parameters = originalRequest.getParameterMap();
        for (final Object param : parameters.keySet()) {
            if (String.valueOf(param).contains(key)) {
                try {
                    final Object[] value = (Object[]) parameters.get(param);
                    if (value != null && value.length > 0) {
                        entityId = Long.parseLong(String.valueOf(value[0]));
                        break;
                    }
                } catch (final Exception e) {
                    log.warn("Unable to parse parameter value.", e);
                }
            }
        }
        return entityId;
    }

    private com.liferay.portal.model.User getCurrentLiferayUser()
            throws PortalException, SystemException {
        if (currentUser == null && currentUserId > 0) {
            currentUser = UserLocalServiceUtil.getUser(currentUserId);
        }
        return currentUser;
    }

    @Override
    public Post saveNewThread(String topic, String rawBody,
            Map<String, byte[]> attachments, Long categoryId)
            throws DataSourceException {

        try {
            final DiscussionThread thread = new DiscussionThread(topic);
            if (categoryId != null) {
                thread.setCategory(getCategory(categoryId));
            }

            final MBMessage savedRootMessage = internalSaveAsCurrentUser(
                    rawBody, attachments, thread,
                    MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID);
            if (savedRootMessage != null) {
                final DiscussionThread savedThread = getThread(savedRootMessage
                        .getThreadId());
                if (savedThread != null) {
                    return savedThread.getLatestPost();
                }
            }
        } catch (final NestableException e) {
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

    @Override
    public boolean isLoggedInUser() {
        return currentUserId != 0;
    }

    @Override
    public final Map<String, String> getPostReplacements() {
        if (postReplacements == null) {
            if (portletPreferences != null) {
                postReplacements = new HashMap<String, String>();
                final String[] values = portletPreferences.getValues(
                        PREFS_REPLACEMENTS_KEY, new String[0]);
                if (values != null) {
                    for (final String value : values) {
                        final String[] split = value
                                .split(REPLACEMENT_SEPARATOR);
                        if (split.length == 2) {
                            postReplacements.put(split[0], split[1]);
                        }
                    }
                }
            } else {
                return Collections.emptyMap();
            }
        }
        return postReplacements;

    }

    @Override
    public final boolean getReplaceMessageBoardsLinks() {
        boolean replace = true;
        if (portletPreferences != null) {
            final String replaceString = portletPreferences.getValue(
                    PREFS_REPLACE_MESSAGE_BOARDS_LINKS, "");
            replace = !String.valueOf(Boolean.FALSE).equals(replaceString);
        }
        return replace;
    }

    @Override
    public boolean getUpdatePageTitle() {
        boolean result = true;
        if (portletPreferences != null) {
            final String booleanValue = portletPreferences.getValue(
                    PREFS_UPDATE_PAGE_TITLE, Boolean.TRUE.toString());
            result = Boolean.parseBoolean(booleanValue);
        }
        return result;
    }

    @Override
    public String getPageTitlePrefix() {
        return portletPreferences.getValue(PREFS_PAGE_TITLE_PREFIX, null);
    }

    @Override
    public final void save(final Configuration config)
            throws DataSourceException {

        if (portletPreferences == null) {
            @SuppressWarnings("deprecation")
            final DataSourceException e = new DataSourceException(
                    "Portlet preferences not available.");
            throw e;
        } else {

            final Map<String, String> postReplacements = config
                    .getReplacements();
            final String[] values = new String[postReplacements.size()];
            int index = 0;
            for (final Entry<String, String> entry : postReplacements
                    .entrySet()) {
                values[index++] = entry.getKey() + REPLACEMENT_SEPARATOR
                        + entry.getValue();
            }
            try {
                portletPreferences.setValues(PREFS_REPLACEMENTS_KEY, values);

                portletPreferences.setValue(PREFS_REPLACE_MESSAGE_BOARDS_LINKS,
                        Boolean.valueOf(config.replaceMessageBoardsLinks())
                                .toString());

                /*
                 * this will make .getPostReplacements() fetch the replacements
                 * again
                 */
                this.postReplacements = null;

                portletPreferences.setValue(PREFS_ANALYTICS_ID,
                        config.getGoogleAnalyticsTrackerId());
                portletPreferences.setValue(PREFS_PATHROOT,
                        config.getPathRoot());

                portletPreferences.setValue(PREFS_UPDATE_PAGE_TITLE, Boolean
                        .valueOf(config.isUpdatePageTitle()).toString());
                portletPreferences.setValue(PREFS_PAGE_TITLE_PREFIX,
                        config.getPageTitlePrefix());

                portletPreferences.store();
            } catch (final Exception e) {
                log.error("Unable to store portlet preferences", e);
                throw new DataSourceException(e);
            }
        }
    }

    @Override
    public String getGoogleAnalyticsTrackerId() {
        return portletPreferences.getValue(PREFS_ANALYTICS_ID, null);
    }

    @Override
    @Deprecated
    public String getPathRoot() {
        return portletPreferences.getValue(PREFS_PATHROOT, null);
    }

    @Override
    public UrlInfo getUrlInfoFromBackendNativeRequest(
            final HttpServletRequest servletRequest)
            throws org.vaadin.tori.exception.NoSuchThreadException,
            DataSourceException {
        final String portletId = servletRequest.getParameter("p_p_id");
        final String messageId = servletRequest.getParameter("_" + portletId
                + "_messageId");
        final String categoryId = servletRequest.getParameter("_" + portletId
                + "_mbCategoryId");

        if (messageId != null) {
            final long parsedMessageId = Long.parseLong(messageId);
            try {
                final MBMessage message = MBMessageServiceUtil
                        .getMessage(parsedMessageId);
                final long threadId = message.getThreadId();
                return new UrlInfo() {
                    @Override
                    public long getId() {
                        return threadId;
                    }

                    @Override
                    public Destination getDestination() {
                        return Destination.THREAD;
                    }
                };
            } catch (final NoSuchThreadException e) {
                throw new org.vaadin.tori.exception.NoSuchThreadException(
                        parsedMessageId, e);
            } catch (final NoSuchMessageException e) {
                throw new org.vaadin.tori.exception.NoSuchThreadException(
                        parsedMessageId, e);
            } catch (final PortalException e) {
                e.printStackTrace();
                throw new DataSourceException(e);
            } catch (final SystemException e) {
                e.printStackTrace();
                throw new DataSourceException(e);
            }
        }

        else if (categoryId != null) {
            return new UrlInfo() {
                @Override
                public long getId() {
                    return Long.parseLong(categoryId);
                }

                @Override
                public Destination getDestination() {
                    return Destination.CATEGORY;
                }
            };
        }

        return null;
    }

    @Override
    public User getToriUser(long userId) throws DataSourceException {
        User user = null;
        if (userId > 0) {
            try {
                user = getUser(userId);
            } catch (NestableException e) {
                throw new DataSourceException(e);
            }
        }
        return user;
    }

    @Override
    public Post getPost(long postId) throws DataSourceException {
        Post result = null;
        try {
            MBMessage message = MBMessageLocalServiceUtil.getMBMessage(postId);
            final User author = getUser(message.getUserId());
            final List<Attachment> attachments = getAttachments(message);

            DiscussionThread thread = getThread(message.getThreadId());
            result = LiferayCommonEntityFactoryUtil.createPost(message, author,
                    thread, attachments);
            if (getReplaceMessageBoardsLinks()) {
                String bodyRaw = result.getBodyRaw();
                bodyRaw = replaceMessageBoardsLinksCategories(bodyRaw);
                bodyRaw = replaceMessageBoardsLinksMessages(bodyRaw);
                result.setBodyRaw(bodyRaw);
            }
        } catch (NestableException e) {
            throw new DataSourceException(e);
        }
        return result;
    }

    @Override
    public User getCurrentUser() {
        try {
            return getUser(currentUserId);
        } catch (NestableException e) {
            return LiferayCommonEntityFactoryUtil
                    .createAnonymousUser(imagePath);
        }
    }

}
