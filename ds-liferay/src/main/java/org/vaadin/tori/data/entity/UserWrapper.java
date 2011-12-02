package org.vaadin.tori.data.entity;

import org.apache.log4j.Logger;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;

public class UserWrapper extends User {

    private static final Logger log = Logger.getLogger(UserWrapper.class);

    public final com.liferay.portal.model.User liferayUser;
    private final String imagePath;

    private UserWrapper(final com.liferay.portal.model.User liferayUser,
            final String imagePath) {
        this.liferayUser = liferayUser;
        this.imagePath = imagePath;
    }

    @Override
    public long getId() {
        return liferayUser.getUserId();
    }

    @Override
    public String getDisplayedName() {
        return liferayUser.getFullName();
    }

    @Override
    public String getAvatarUrl() {
        if (imagePath != null) {
            try {
                return imagePath + "/user_"
                        + (liferayUser.isFemale() ? "female" : "male")
                        + "_portrait?img_id=" + liferayUser.getPortraitId();
            } catch (final PortalException e) {
                log.warn("Cannot display avatar for user " + getId() + ".", e);
            } catch (final SystemException e) {
                log.warn("Cannot display avatar for user " + getId() + ".", e);
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof UserWrapper) {
            return liferayUser.equals(((UserWrapper) obj).liferayUser);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return liferayUser.hashCode();
    }

    public static User wrap(final com.liferay.portal.model.User liferayUser,
            final String imagePath) {
        if (liferayUser != null) {
            return new UserWrapper(liferayUser, imagePath);
        } else {
            return null;
        }
    }

}
