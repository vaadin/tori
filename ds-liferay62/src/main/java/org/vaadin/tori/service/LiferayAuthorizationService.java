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

package org.vaadin.tori.service;

import java.lang.reflect.Method;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.LiferayDataSource;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.LiferayAuthorizationConstants.CategoryAction;
import org.vaadin.tori.service.LiferayAuthorizationConstants.MbAction;
import org.vaadin.tori.service.LiferayAuthorizationConstants.MessageAction;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.ReflectionUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBBanLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;

public class LiferayAuthorizationService implements AuthorizationService,
        PortletRequestAware {

    private static final Logger LOG = Logger
            .getLogger(LiferayAuthorizationService.class);
    private long scopeGroupId = -1;
    private String currentUser;
    private boolean banned;

    @Override
    public boolean mayEditCategories() {
        return hasPermission(MbAction.ADD_CATEGORY);
    }

    @Override
    public boolean mayReportPosts() {
        return isLoggedIn() && !isBanned();
    }

    @Override
    public boolean mayFollowCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.SUBSCRIBE, categoryId);
    }

    @Override
    public boolean mayDeleteCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.DELETE, categoryId);
    }

    @Override
    public boolean mayEditCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.UPDATE, categoryId);
    }

    @Override
    public boolean mayEditPost(final long postId) {
        return hasMessagePermission(MessageAction.UPDATE, postId);
    }

    @Override
    public boolean mayReplyInThread(final long threadid) {
        MBThread mbThread = null;
        try {
            mbThread = MBThreadLocalServiceUtil.getThread(threadid);
        } catch (final NestableException e) {
            LOG.error(e);
        }
        return mbThread != null
                && !mbThread.isLocked()
                && hasCategoryPermission(CategoryAction.REPLY_TO_MESSAGE,
                        mbThread.getCategoryId());
    }

    @Override
    public boolean mayAddFilesInCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.ADD_FILE, categoryId);
    }

    @Override
    public boolean mayBan() {
        return hasPermission(MbAction.BAN_USER);
    }

    @Override
    public boolean mayFollowThread(final long threadId) {
        try {
            return hasMessagePermission(MessageAction.SUBSCRIBE,
                    LiferayDataSource.getRootMessageId(threadId));
        } catch (final DataSourceException e) {
            LOG.error(e);
        }
        return false;
    }

    @Override
    public boolean mayDeletePost(final long postId) {
        return hasMessagePermission(MessageAction.DELETE, postId);
    }

    @Override
    public boolean mayVote() {
        return isLoggedIn() && !isBanned();
    }

    @Override
    public boolean mayMoveThreadInCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.MOVE_THREAD, categoryId);
    }

    @Override
    public boolean mayStickyThreadInCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.UPDATE_THREAD_PRIORITY,
                categoryId);
    }

    @Override
    public boolean mayLockThreadInCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.LOCK_THREAD, categoryId);
    }

    @Override
    public boolean mayDeleteThread(final long threadId) {
        try {
            return hasMessagePermission(MessageAction.DELETE,
                    LiferayDataSource.getRootMessageId(threadId));
        } catch (final DataSourceException e) {
            LOG.error(e);
        }
        return false;
    }

    @Override
    public boolean mayCreateThreadInCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.ADD_MESSAGE, categoryId);
    }

    private PermissionChecker getPermissionChecker() {
        final PermissionChecker pc = PermissionThreadLocal
                .getPermissionChecker();
        if (pc == null) {
            throw new IllegalStateException(
                    "PermissionChecker is not initialized.");
        }
        return pc;
    }

    private static final Method CATEGORY_PERMISSION_CONTAINS = getCategoryPermissionContainsMethod();
    private static final Method MESSAGE_PERMISSION_CONTAINS = getMessagePermissionContainsMethod();

    private static Method getMessagePermissionContainsMethod() {
        Method result = null;
        try {
            Class<?> mbMessagePermissionClass = PortalClassLoaderUtil
                    .getClassLoader()
                    .loadClass(
                            "com.liferay.portlet.messageboards.service.permission.MBMessagePermission");
            result = ReflectionUtil.getDeclaredMethod(mbMessagePermissionClass,
                    "contains", PermissionChecker.class, long.class,
                    String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Method getCategoryPermissionContainsMethod() {
        Method result = null;
        try {
            Class<?> mbCategoryPermissionClass = PortalClassLoaderUtil
                    .getClassLoader()
                    .loadClass(
                            "com.liferay.portlet.messageboards.service.permission.MBCategoryPermission");
            result = ReflectionUtil.getDeclaredMethod(
                    mbCategoryPermissionClass, "contains",
                    PermissionChecker.class, long.class, long.class,
                    String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean hasCategoryPermission(final CategoryAction action,
            final Long categoryId) {
        if (isBanned()) {
            return false;
        }
        try {
            long normalizeCategoryId = LiferayDataSource
                    .normalizeCategoryId(categoryId);
            Object result = CATEGORY_PERMISSION_CONTAINS.invoke(null,
                    getPermissionChecker(), scopeGroupId, normalizeCategoryId,
                    action.toString());
            return (Boolean) result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasMessagePermission(final MessageAction action,
            final long messageId) {
        if (isBanned()) {
            return false;
        }
        try {
            Object result = MESSAGE_PERMISSION_CONTAINS.invoke(null,
                    getPermissionChecker(), messageId, action.toString());
            return (Boolean) result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasPermission(final MbAction action) {
        if (isBanned()) {
            return false;
        }
        return getPermissionChecker().hasPermission(scopeGroupId,
                MbAction.getScope(), scopeGroupId, action.toString());
    }

    private boolean isBanned() {
        return banned;
    }

    private boolean isLoggedIn() {
        return currentUser != null;
    }

    private void setCurrentUser(final String user) {
        if (currentUser == null && user != null || currentUser != null
                && !currentUser.equals(user)) {
            // user has changed
            currentUser = user;
            LOG.debug(String.format("Current user is now %s.", currentUser));
        }
    }

    private void setBannedStatus() {
        // check the ban status and store in the banned field
        if (currentUser != null) {
            try {
                banned = MBBanLocalServiceUtil.hasBan(scopeGroupId,
                        Long.valueOf(currentUser));
            } catch (final SystemException e) {
                LOG.error("Cannot check ban status for user " + currentUser, e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setRequest(final PortletRequest request) {
        if (scopeGroupId < 0) {
            // scope not defined yet -> get if from the request
            final ThemeDisplay themeDisplay = (ThemeDisplay) request
                    .getAttribute(WebKeys.THEME_DISPLAY);

            if (themeDisplay != null) {
                scopeGroupId = themeDisplay.getScopeGroupId();
                LOG.debug("Using groupId " + scopeGroupId + " as the scope.");
            }
        }

        setCurrentUser(request.getRemoteUser());
        setBannedStatus();
    }

    @Override
    public boolean mayViewCategory(final Long categoryId) {
        return hasCategoryPermission(CategoryAction.VIEW, categoryId);
    }

    @Override
    public boolean mayViewThread(final long threadId) {
        boolean result = false;
        try {
            result = hasMessagePermission(MessageAction.VIEW,
                    LiferayDataSource.getRootMessageId(threadId));
        } catch (DataSourceException e) {
            LOG.error(e);
        }
        return result;
    }

    @Override
    public boolean mayViewPost(final long postId) {
        return hasMessagePermission(MessageAction.VIEW, postId);
    }

}
