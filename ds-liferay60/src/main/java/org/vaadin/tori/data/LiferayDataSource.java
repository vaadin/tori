/*
 * Copyright 2012 Vaadin Ltd.
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

import com.liferay.documentlibrary.service.DLServiceUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portlet.messageboards.NoSuchThreadException;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBMessageFlag;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBThreadConstants;
import com.liferay.portlet.messageboards.service.MBCategoryServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageFlagLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadServiceUtil;

public class LiferayDataSource extends LiferayCommonDataSource implements
        DataSource, PortletRequestAware {

    private static final Logger LOG = Logger.getLogger(LiferayDataSource.class);

    @Override
    public void followThread(final long threadId) throws DataSourceException {
        if (isLoggedInUser()) {
            try {
                SubscriptionLocalServiceUtil.addSubscription(currentUserId,
                        MBThread.class.getName(), threadId);
            } catch (final NestableException e) {
                LOG.error(String.format("Cannot follow thread %d", threadId), e);
                throw new DataSourceException(e);
            }
        }
    }

    @Override
    public int getMyPostThreadsCount() throws DataSourceException {
        // Not an optimal solution (performance-wise), but currently
        // MBThreadServiceUtil.getGroupThreadsCount doesn't _always_ give the
        // same count for my threads as getMyPostThreads does.
        final int groupThreadsCount = getMyPostThreads(QUERY_ALL, QUERY_ALL)
                .size();
        LOG.debug("LiferayDataSource.getMyPostThreadsCount(): "
                + groupThreadsCount);
        return groupThreadsCount;

        // MBThreadServiceUtil.getGroupThreadsCount(scopeGroupId, currentUserId,
        // WorkflowConstants.STATUS_ANY);
    }

    @Override
    public List<DiscussionThread> getMyPostThreads(final int from, final int to)
            throws DataSourceException {
        if (isLoggedInUser()) {
            try {
                final List<MBThread> liferayThreads = MBThreadServiceUtil
                        .getGroupThreads(scopeGroupId, currentUserId,
                                WorkflowConstants.STATUS_ANY, from, to);
                final List<DiscussionThread> result = new ArrayList<DiscussionThread>(
                        liferayThreads.size());
                for (final MBThread liferayThread : liferayThreads) {
                    final DiscussionThread thread = wrapLiferayThread(
                            liferayThread, null);
                    result.add(thread);
                }

                return result;
            } catch (Exception e) {
                // getGroupThreads() failed, handle with getGroupMessages
                return getMyPostThreadsFromMessages(from, to);
            }
        } else {
            return Collections.emptyList();
        }
    }

    private List<DiscussionThread> getMyPostThreadsFromMessages(final int from,
            final int to) throws DataSourceException {
        try {
            // collection for the final result
            final List<DiscussionThread> threads = new ArrayList<DiscussionThread>();
            final Map<Long, Date> myLastPostDates = new HashMap<Long, Date>();
            final Set<Long> processedThreads = new HashSet<Long>();
            for (final MBMessage liferayMessage : MBMessageLocalServiceUtil
                    .getGroupMessages(scopeGroupId, currentUserId,
                            WorkflowConstants.STATUS_ANY, QUERY_ALL, QUERY_ALL)) {
                if (processedThreads.add(liferayMessage.getThreadId())) {
                    try {
                        MBThread liferayThread = liferayMessage.getThread();
                        myLastPostDates.put(liferayMessage.getThreadId(),
                                liferayThread.getLastPostDate());
                        final DiscussionThread thread = wrapLiferayThread(
                                liferayThread, null);
                        threads.add(thread);
                    } catch (NoSuchThreadException e) {
                        // Ignore and continue
                    }
                }

            }

            Collections.sort(threads, new Comparator<DiscussionThread>() {
                @Override
                public int compare(final DiscussionThread t1,
                        final DiscussionThread t2) {
                    return myLastPostDates.get(t2.getId()).compareTo(
                            myLastPostDates.get(t1.getId()));

                }
            });

            int toIndex = to == -1 ? threads.size() - 1 : to;

            return threads.subList(Math.max(0, from), toIndex);
        } catch (final NestableException e) {
            LOG.error("Couldn't get my posts.", e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public int getUnreadThreadCount(final long categoryId)
            throws DataSourceException {

        int result = 0;
        if (isLoggedInUser()) {
            // 0. All the category ids (recursively) including the parameter
            @SuppressWarnings("rawtypes")
            Collection categoryIds = getCategoryIdsRecursively(categoryId);

            // 1. Ids of all the threads user has read
            DynamicQuery readThreadIds = DynamicQueryFactoryUtil
                    .forClass(MBMessageFlag.class,
                            PortalClassLoaderUtil.getClassLoader())
                    .setProjection(ProjectionFactoryUtil.property("threadId"))
                    .add(PropertyFactoryUtil.forName("userId")
                            .eq(currentUserId));

            // 2. Query the threads that are in one of the categories
            // from 0. AND are not read 1. AND are approved by status.
            @SuppressWarnings("unchecked")
            DynamicQuery resultQuery = DynamicQueryFactoryUtil
                    .forClass(MBThread.class,
                            PortalClassLoaderUtil.getClassLoader())
                    .add(PropertyFactoryUtil.forName("categoryId").in(
                            categoryIds))
                    .add(PropertyFactoryUtil.forName("threadId").notIn(
                            readThreadIds))
                    .add(PropertyFactoryUtil.forName("status").eq(
                            WorkflowConstants.STATUS_APPROVED));

            try {
                result = new Long(
                        MBThreadLocalServiceUtil.dynamicQueryCount(resultQuery))
                        .intValue();
            } catch (SystemException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean isThreadRead(final long threadId) {
        boolean result = true;
        if (isLoggedInUser()) {
            try {
                result = MBMessageFlagLocalServiceUtil.hasReadFlag(
                        currentUserId,
                        MBThreadLocalServiceUtil.getThread(threadId));
            } catch (final NestableException e) {
                LOG.error(
                        String.format(
                                "Couldn't check for read flag on thread %d.",
                                threadId), e);
            }
        }
        return result;
    }

    @Override
    public void markThreadRead(final long threadId) throws DataSourceException {
        if (isLoggedInUser()) {
            try {
                MBMessageFlagLocalServiceUtil.addReadFlags(currentUserId,
                        MBThreadLocalServiceUtil.getThread(threadId));
            } catch (final NestableException e) {
                LOG.error(String.format("Couldn't mark thread %d as read.",
                        threadId), e);
                throw new DataSourceException(e);
            }
        }
    }

    @Override
    public void saveNewCategory(final Long parentCategoryId, final String name,
            final String description) throws DataSourceException {
        try {
            MBCategoryServiceUtil
                    .addCategory(normalizeCategoryId(parentCategoryId), name,
                            description, null, null, null, 0, false, null,
                            null, 0, null, false, null, 0, false, null, null,
                            false, mbCategoryServiceContext);
        } catch (final NestableException e) {
            LOG.error(String.format("Cannot save category"), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    protected List<Attachment> getAttachments(final MBMessage message)
            throws NestableException {
        final List<Attachment> attachments = new ArrayList<Attachment>();
        if (message.isAttachments()) {
            final String[] filenames = message.getAttachmentsFiles();
            for (final String filename : filenames) {
                final String shortFilename = FileUtil
                        .getShortFileName(filename);
                final long fileSize = DLServiceUtil.getFileSize(
                        themeDisplay.getCompanyId(), CompanyConstants.SYSTEM,
                        filename);

                final Attachment attachment = new Attachment(shortFilename,
                        fileSize);
                attachment.setDownloadUrl(getAttachmentDownloadUrl(
                        shortFilename, message.getMessageId()));
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    private String getAttachmentDownloadUrl(final String filename,
            final long messageId) throws SystemException {
        return themeDisplay.getPathMain()
                + "/message_boards/get_message_attachment?messageId="
                + messageId + "&attachment=" + HttpUtil.encodeURL(filename);
    }

    @Override
    protected MBMessage internalSaveAsCurrentUser(final String rawBody,
            final Map<String, byte[]> files, final DiscussionThread thread,
            final long parentMessageId) throws PortalException, SystemException {
        final long groupId = scopeGroupId;
        final long categoryId = thread.getCategory().getId();
        final long threadId = thread.getId();

        // trim because liferay seems to bug out otherwise
        String subject = thread.getTopic().trim();
        if (parentMessageId != MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID) {
            subject = "RE: " + subject;
        }
        final String body = rawBody.trim();
        final List<ObjectValuePair<String, byte[]>> attachments = new ArrayList<ObjectValuePair<String, byte[]>>();

        if (files != null) {
            for (final Entry<String, byte[]> file : files.entrySet()) {
                final String fileName = file.getKey();
                final byte[] bytes = file.getValue();

                if ((bytes != null) && (bytes.length > 0)) {
                    final ObjectValuePair<String, byte[]> ovp = new ObjectValuePair<String, byte[]>(
                            fileName, bytes);

                    attachments.add(ovp);
                }
            }
        }

        final boolean anonymous = false;
        final double priority = MBThreadConstants.PRIORITY_NOT_GIVEN;
        final boolean allowPingbacks = false;

        return MBMessageServiceUtil.addMessage(groupId, categoryId, threadId,
                parentMessageId, subject, body, attachments, anonymous,
                priority, allowPingbacks, mbMessageServiceContext);
    }

    @Override
    protected String getThemeDisplayKey() {
        return WebKeys.THEME_DISPLAY;
    }

    @Override
    protected boolean isFormatBBCode(final MBMessage message) {
        return true;
    }

}
