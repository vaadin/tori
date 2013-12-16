package org.vaadin.tori.util.vaadin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.util.UserBadgeProvider;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.RoleServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class VaadinBadgeProvider implements UserBadgeProvider {

    private static final String BADGE_FONT_ICON_CHARACTER = "\ue015";
    private static final String LINK_URL_FSTRING = "/certificate/-/view/%s";

    private static final String BADGE_EXPANDO_COLUMN_NAME = "showCertificationBadge";
    private static final String BADGE_ROLE_NAME = "Vaadin 7 Certified Developer";

    @Override
    @CheckForNull
    public String getHtmlBadgeFor(@NonNull final User user) {
        final String userId = getBadgeUserId(user);
        if (userId == null) {
            return null;
        }

        try {
            getLogger()
                    .debug("Generating badge for " + user.getDisplayedName());
            if (hasBadgeRole(user) && certificationIsPublicInfoFor(user)) {
                final String certificateLink = String.format(LINK_URL_FSTRING,
                        userId);
                return String
                        .format("<a href=\"%s\" title=\"Vaadin 7 Certified Developer\">%s</a>",
                                certificateLink, BADGE_FONT_ICON_CHARACTER);
            }
        } catch (final SystemException e) {
            getLogger().error(e);
        }

        return null;
    }

    private static Logger getLogger() {
        return Logger.getLogger(VaadinBadgeProvider.class);
    }

    private boolean hasBadgeRole(final User user) throws SystemException {
        try {
            List<Role> userRoles = RoleServiceUtil.getUserRoles(liferayUser(
                    user).getUserId());
            for (final Role role : userRoles) {
                if (BADGE_ROLE_NAME.equals(role.getName())) {
                    getLogger().debug("Has badge role " + BADGE_ROLE_NAME);
                    return true;
                }
            }
        } catch (Exception e) {
            getLogger().error("Exception while trying to list user roles", e);
        }

        getLogger().debug("Didn't have badge role " + BADGE_ROLE_NAME);
        return false;
    }

    private boolean certificationIsPublicInfoFor(final User user) {
        final com.liferay.portal.model.User liferayUser = liferayUser(user);
        boolean badgeShown = true;
        getLogger().debug("Defaulting 'show badge' to " + badgeShown);
        try {
            getLogger()
                    .debug("Looking up user Custom Field "
                            + BADGE_EXPANDO_COLUMN_NAME);
            badgeShown = ExpandoValueLocalServiceUtil.getData(
                    liferayUser.getCompanyId(),
                    com.liferay.portal.model.User.class.getName(),
                    "CUSTOM_FIELDS", BADGE_EXPANDO_COLUMN_NAME,
                    liferayUser.getUserId(), true);
            getLogger().debug("Liferay said " + badgeShown);
        } catch (final NestableException e) {
            getLogger().error(e);
        } catch (final Exception e) {
            getLogger().error(e);
        }
        getLogger().debug("Showing badge: " + badgeShown);
        return badgeShown;
    }

    private String getBadgeUserId(final User user) {
        return md5(liferayUser(user).getUuid());
    }

    /** cleans up the code a bit */
    private com.liferay.portal.model.User liferayUser(final User user) {
        return (com.liferay.portal.model.User) user.getOriginalUserObject();
    }

    @NonNull
    private static String md5(final String stringToHash) {
        try {
            final MessageDigest md = MessageDigest.getInstance("md5");
            final byte[] digestBytes = md.digest(stringToHash.getBytes());

            // Convert to hex String.
            final StringBuilder hex = new StringBuilder();
            for (final byte digestByte : digestBytes) {
                hex.append(String.format("%02x", digestByte));
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException e) {
            // We should never get here.
            throw new RuntimeException("Algorithm md5 not supported.", e);
        }
    }
}