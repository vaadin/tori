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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBThreadConstants;
import com.liferay.portlet.messageboards.model.MBThreadFlag;
import com.liferay.portlet.messageboards.service.MBCategoryServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadFlagLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;

public class LiferayDataSource extends LiferayCommonDataSource implements
        DataSource, PortletRequestAware {

    private static final Logger LOG = Logger.getLogger(LiferayDataSource.class);

    @Override
    public void followThread(final long threadId) throws DataSourceException {
        if (isLoggedInUser()) {
            try {
                SubscriptionLocalServiceUtil.addSubscription(currentUserId,
                        currentUser.getGroupId(), MBThread.class.getName(),
                        threadId);
            } catch (final NestableException e) {
                LOG.error(String.format("Cannot follow thread %d", threadId), e);
                throw new DataSourceException(e);
            } catch (final NullPointerException e) {
                LOG.error(String.format("Cannot follow thread %d", threadId), e);
            }
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
                    .forClass(MBThreadFlag.class,
                            PortalClassLoaderUtil.getClassLoader())
                    .setProjection(ProjectionFactoryUtil.property("threadId"))
                    .add(PropertyFactoryUtil.forName("userId")
                            .eq(currentUserId));

            // 2. Query the threads that are in one of the categories
            // from 0. AND are not read 1. AND are approved by status.
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
                result = MBThreadFlagLocalServiceUtil.hasThreadFlag(
                        currentUserId,
                        MBThreadLocalServiceUtil.getThread(threadId));
            } catch (final NestableException e) {
                LOG.error(
                        String.format(
                                "Couldn't check for read flag on thread %d.",
                                threadId), e);
            }
        }
        // default to read in case of an anonymous user
        return result;
    }

    @Override
    public void markThreadRead(final long threadId) throws DataSourceException {
        if (isLoggedInUser()) {
            try {
                MBThreadFlagLocalServiceUtil.addThreadFlag(currentUserId,
                        MBThreadLocalServiceUtil.getThread(threadId),
                        flagsServiceContext);
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
            LOG.debug("Adding new category: " + name);
            final long parentId = normalizeCategoryId(parentCategoryId);

            final String displayStyle = "default";

            MBCategoryServiceUtil.addCategory(parentId, name, description,
                    displayStyle, null, null, null, 0, false, null, null, 0,
                    null, false, null, 0, false, null, null, false, false,
                    mbCategoryServiceContext);
        } catch (final NestableException e) {
            LOG.error("Cannot persist category", e);
            throw new DataSourceException(e);
        }
    }

    @Override
    protected List<Attachment> getAttachments(final MBMessage message)
            throws NestableException {
        if (message.getAttachmentsFileEntriesCount() > 0) {
            final List<FileEntry> filenames = message
                    .getAttachmentsFileEntries();
            final List<Attachment> attachments = new ArrayList<Attachment>(
                    filenames.size());
            for (final FileEntry fileEntry : filenames) {
                String downloadUrl = PortletFileRepositoryUtil
                        .getPortletFileEntryURL(themeDisplay, fileEntry,
                                StringPool.BLANK);

                final String shortFilename = fileEntry.getTitle();
                final long fileSize = fileEntry.getSize();

                final Attachment attachment = new Attachment(shortFilename,
                        fileSize);
                attachment.setDownloadUrl(downloadUrl);
                attachments.add(attachment);
            }
            return attachments;
        }
        return Collections.emptyList();
    }

    @Override
    protected MBMessage internalSaveAsCurrentUser(final String rawBody,
            final Map<String, byte[]> files, final DiscussionThread thread,
            final long parentMessageId) throws PortalException, SystemException {
        final long groupId = scopeGroupId;
        final long categoryId = thread.getCategory() != null ? thread
                .getCategory().getId() : normalizeCategoryId(null);

        // trim because liferay seems to bug out otherwise
        String subject = thread.getTopic().trim();
        final String body = rawBody.trim();
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
        final String format = "bbcode";

        MBMessage message = null;

        if (parentMessageId == MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID) {
            // Post new thread
            message = MBMessageServiceUtil.addMessage(groupId, categoryId,
                    subject, body, format, attachments, anonymous, priority,
                    allowPingbacks, mbMessageServiceContext);
        } else {
            // Post reply
            message = MBMessageServiceUtil.addMessage(parentMessageId, "RE: "
                    + subject, body, format, attachments, anonymous, priority,
                    allowPingbacks, mbMessageServiceContext);
        }
        return message;
    }

    @Override
    protected String getThemeDisplayKey() {
        return WebKeys.THEME_DISPLAY;
    }

    @Override
    protected boolean isFormatBBCode(final MBMessage message) {
        return message.isFormatBBCode();
    }

}
