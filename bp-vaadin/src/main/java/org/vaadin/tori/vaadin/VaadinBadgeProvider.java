package org.vaadin.tori.vaadin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.util.UserBadgeProvider;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class VaadinBadgeProvider implements UserBadgeProvider {

    private static final String BADGE_FONT_ICON_CHARACTER = "\ue015";
    private static final String LINK_URL_FSTRING = "/certificate/-/%s";

    @Override
    @CheckForNull
    public String getHtmlBadgeFor(@NonNull final User user) {
        final String userId = getBadgeUserId(user);
        if (userId == null) {
            return null;
        }

        final String certificateLink = String.format(LINK_URL_FSTRING, userId);
        return String.format(
                "<a href=\"%s\" title=\"Vaadin Certified\">%s</a>",
                certificateLink, BADGE_FONT_ICON_CHARACTER);
    }

    private String getBadgeUserId(final User user) {
        final com.liferay.portal.model.User liferayUser = (com.liferay.portal.model.User) user
                .getOriginalUserObject();
        return md5(liferayUser.getUuid());
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