package org.vaadin.tori.service;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.LiferayDataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.LiferayAuthorizationConstants.CategoryAction;
import org.vaadin.tori.service.LiferayAuthorizationConstants.MbAction;
import org.vaadin.tori.service.LiferayAuthorizationConstants.MessageAction;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.service.MBBanLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;

public class LiferayAuthorizationService implements AuthorizationService,
        PortletRequestAware {

    private static final Logger log = Logger
            .getLogger(LiferayAuthorizationService.class);
    private long scopeGroupId = -1;
    private String currentUser;
    private boolean banned;

    @Override
    public boolean mayEditCategories() {
        return hasPermission(MbAction.ADD_CATEGORY);
    }

    @Override
    public boolean mayRearrangeCategories() {
        // Liferay doesn't support reordering of categories.
        return false;
    }

    @Override
    public boolean mayReportPosts() {
        return isLoggedIn() && !isBanned();
    }

    @Override
    public boolean mayFollow(final Category category) {
        return hasCategoryPermission(CategoryAction.SUBSCRIBE, category);
    }

    @Override
    public boolean mayDelete(final Category category) {
        return hasCategoryPermission(CategoryAction.DELETE, category);
    }

    @Override
    public boolean mayEdit(final Category category) {
        return hasCategoryPermission(CategoryAction.UPDATE, category);
    }

    @Override
    public boolean mayEdit(final Post post) {
        return hasMessagePermission(MessageAction.UPDATE, post.getId());
    }

    @Override
    public boolean mayReplyIn(final DiscussionThread thread) {
        return hasCategoryPermission(CategoryAction.REPLY_TO_MESSAGE,
                thread.getCategory());
    }

    @Override
    public boolean mayAddFiles(final Category category) {
        return hasCategoryPermission(CategoryAction.ADD_FILE, category);
    }

    @Override
    public boolean mayBan() {
        return hasPermission(MbAction.BAN_USER);
    }

    @Override
    public boolean mayFollow(final DiscussionThread thread) {
        try {
            return hasMessagePermission(MessageAction.SUBSCRIBE,
                    LiferayDataSource.getRootMessageId(thread));
        } catch (final DataSourceException e) {
            log.error(e);
        }
        return false;
    }

    @Override
    public boolean mayDelete(final Post post) {
        return hasMessagePermission(MessageAction.DELETE, post.getId());
    }

    @Override
    public boolean mayVote() {
        return isLoggedIn() && !isBanned();
    }

    @Override
    public boolean mayMove(final DiscussionThread thread) {
        return hasCategoryPermission(CategoryAction.MOVE_THREAD,
                thread.getCategory());
    }

    @Override
    public boolean maySticky(final DiscussionThread thread) {
        return hasCategoryPermission(CategoryAction.UPDATE_THREAD_PRIORITY,
                thread.getCategory());
    }

    @Override
    public boolean mayLock(final DiscussionThread thread) {
        return hasCategoryPermission(CategoryAction.LOCK_THREAD,
                thread.getCategory());
    }

    @Override
    public boolean mayDelete(final DiscussionThread thread) {
        try {
            return hasMessagePermission(MessageAction.DELETE,
                    LiferayDataSource.getRootMessageId(thread));
        } catch (final DataSourceException e) {
            log.error(e);
        }
        return false;
    }

    @Override
    public boolean mayCreateThreadIn(final Category category) {
        return hasCategoryPermission(CategoryAction.ADD_MESSAGE, category);
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

    private boolean hasCategoryPermission(final CategoryAction action,
            final Category category) {
        if (isBanned()) {
            return false;
        }
        return getPermissionChecker().hasPermission(scopeGroupId,
                CategoryAction.getScope(), category.getId(), action.toString());
    }

    private boolean hasMessagePermission(final MessageAction action,
            final long messageId) {
        if (isBanned()) {
            return false;
        }

        try {
            final MBMessage message = MBMessageLocalServiceUtil
                    .getMBMessage(messageId);

            // check for owner permission
            if (getPermissionChecker().hasOwnerPermission(
                    message.getCompanyId(), MBMessage.class.getName(),
                    message.getRootMessageId(), message.getUserId(),
                    action.toString())) {
                return true;
            }

            // check for other permissions
            return getPermissionChecker().hasPermission(message.getGroupId(),
                    MBMessage.class.getName(), message.getRootMessageId(),
                    action.toString());
        } catch (final PortalException e) {
            log.error(e);
        } catch (final SystemException e) {
            log.error(e);
        }
        // default to false
        return false;
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
            log.debug(String.format("Current user is now %s.", currentUser));
        }
    }

    private void setBannedStatus() {
        // check the ban status and store in the banned field
        if (currentUser != null) {
            try {
                banned = MBBanLocalServiceUtil.hasBan(scopeGroupId,
                        Long.valueOf(currentUser));
            } catch (final SystemException e) {
                log.error("Cannot check ban status for user " + currentUser, e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setRequest(final PortletRequest request) {
        if (scopeGroupId < 0) {
            // scope not defined yet -> get if from the request
            final ThemeDisplay themeDisplay = (ThemeDisplay) request
                    .getAttribute("THEME_DISPLAY");

            if (themeDisplay != null) {
                scopeGroupId = themeDisplay.getScopeGroupId();
                log.info("Using groupId " + scopeGroupId + " as the scope.");
            }
        }

        setCurrentUser(request.getRemoteUser());
        setBannedStatus();
    }

}
