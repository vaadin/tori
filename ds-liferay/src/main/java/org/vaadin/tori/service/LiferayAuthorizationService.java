package org.vaadin.tori.service;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.DiscussionThreadWrapper;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.service.LiferayAuthorizationConstants.CategoryAction;
import org.vaadin.tori.service.LiferayAuthorizationConstants.MbAction;
import org.vaadin.tori.service.LiferayAuthorizationConstants.MessageAction;

import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.theme.ThemeDisplay;

public class LiferayAuthorizationService implements AuthorizationService {

    private static final Logger log = Logger
            .getLogger(LiferayAuthorizationService.class);
    private long scopeGroupId;

    @Override
    public boolean isCategoryAdministrator() {
        // TODO
        log.warn("Not yet implemented.");
        return true;
    }

    @Override
    public boolean mayReportPosts() {
        // Liferay doesn't provide any checks for this permission.
        // Always allowed.
        return true;
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
        return hasMessagePermission(MessageAction.UPDATE, post);
    }

    @Override
    public boolean mayReplyIn(final DiscussionThread thread) {
        return hasCategoryPermission(CategoryAction.REPLY_TO_MESSAGE,
                thread.getCategory());
    }

    @Override
    public boolean mayBan() {
        final boolean permission = getPermissionChecker().hasPermission(
                scopeGroupId, MbAction.getScope(), scopeGroupId,
                MbAction.BAN_USER.toString());
        log.debug("mayBan: " + permission);
        return permission;
    }

    @Override
    public boolean mayFollow(final DiscussionThread currentThread) {
        if (currentThread instanceof DiscussionThreadWrapper) {
            return hasMessagePermission(MessageAction.SUBSCRIBE,
                    ((DiscussionThreadWrapper) currentThread)
                            .getRootMessageId());
        }
        return false;
    }

    @Override
    public boolean mayDelete(final Post post) {
        return hasMessagePermission(MessageAction.DELETE, post);
    }

    @Override
    public boolean mayVote() {
        // TODO should simply check if the user is logged in
        log.warn("Not yet implemented.");
        return true;
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
        if (thread instanceof DiscussionThreadWrapper) {
            return hasMessagePermission(MessageAction.DELETE,
                    ((DiscussionThreadWrapper) thread).getRootMessageId());
        }
        return false;
    }

    @Override
    public boolean mayCreateThreadIn(final Category category) {
        // TODO
        log.warn("Not yet implemented.");
        return true;
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
        final boolean permission = getPermissionChecker().hasPermission(
                scopeGroupId, CategoryAction.getScope(), category.getId(),
                action.toString());
        if (log.isDebugEnabled()) {
            log.debug("hasCategoryPermission(" + action.toString() + ", "
                    + category.getId() + "): " + permission);
        }
        return permission;
    }

    private boolean hasMessagePermission(final MessageAction action,
            final long messageId) {
        final boolean permission = getPermissionChecker().hasPermission(
                scopeGroupId, MessageAction.getScope(), messageId,
                action.toString());
        if (log.isDebugEnabled()) {
            log.debug("hasMessagePermission(" + action.toString() + ", "
                    + messageId + "): " + permission);
        }
        return permission;
    }

    private boolean hasMessagePermission(final MessageAction action,
            final Post message) {
        return hasMessagePermission(action, message.getId());
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
                log.info("Using groupId " + scopeGroupId + " as the scope.");
            }
        }
    }
}
