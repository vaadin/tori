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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

import com.liferay.documentlibrary.service.DLServiceUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBMessageFlagConstants;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.model.MBThreadConstants;
import com.liferay.portlet.messageboards.service.MBCategoryServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageFlagLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;

public class LiferayDataSource extends LiferayCommonDataSource implements
        DataSource, PortletRequestAware {

    private static final Logger log = Logger.getLogger(LiferayDataSource.class);

    @Override
    public void followThread(long threadId) throws DataSourceException {
        try {
            SubscriptionLocalServiceUtil.addSubscription(currentUserId,
                    MBThread.class.getName(), threadId);
        } catch (final NestableException e) {
            log.error(String.format("Cannot follow thread %d", threadId), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    public int getUnreadThreadCount(final long categoryId)
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
        final long totalThreadCount = getThreadCountRecursively(categoryId);
        try {
            connection = JdbcUtil.getJdbcConnection();
            statement = connection
                    .prepareStatement("select (? - count(MBMessageFlag.messageFlagId)) "
                            + "from MBMessageFlag, MBThread "
                            + "where flag = ? and MBMessageFlag.threadId = MBThread.threadId "
                            + "and MBThread.categoryId = ? and MBMessageFlag.userId = ?");

            statement.setLong(1, totalThreadCount);
            statement.setLong(2, MBMessageFlagConstants.READ_FLAG);
            statement.setLong(3, categoryId);
            statement.setLong(4, currentUserId);

            result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
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
    public boolean isThreadRead(long threadId) {
        boolean result = true;
        if (currentUserId > 0) {
            try {
                result = MBMessageFlagLocalServiceUtil.hasReadFlag(
                        currentUserId,
                        MBThreadLocalServiceUtil.getThread(threadId));
            } catch (final NestableException e) {
                log.error(
                        String.format(
                                "Couldn't check for read flag on thread %d.",
                                threadId), e);
            }
        }
        return result;
    }

    @Override
    public void markThreadRead(long threadId) throws DataSourceException {
        if (currentUserId > 0) {
            try {
                MBMessageFlagLocalServiceUtil.addReadFlags(currentUserId,
                        MBThreadLocalServiceUtil.getThread(threadId));
            } catch (final NestableException e) {
                log.error(String.format("Couldn't mark thread %d as read.",
                        threadId), e);
                throw new DataSourceException(e);
            }
        }
    }

    @Override
    public void saveNewCategory(Long parentCategoryId, String name,
            String description) throws DataSourceException {
        try {
            final MBCategory c = MBCategoryServiceUtil.addCategory(
                    parentCategoryId, name, description, null, null, null, 0,
                    false, null, null, 0, null, false, null, 0, false, null,
                    null, false, mbCategoryServiceContext);
        } catch (final NestableException e) {
            log.error(String.format("Cannot save category"), e);
            throw new DataSourceException(e);
        }
    }

    @Override
    protected List<Attachment> getAttachments(MBMessage message)
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
    protected MBMessage internalSaveAsCurrentUser(String rawBody,
            Map<String, byte[]> files, DiscussionThread thread,
            long parentMessageId) throws PortalException, SystemException {
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

}
