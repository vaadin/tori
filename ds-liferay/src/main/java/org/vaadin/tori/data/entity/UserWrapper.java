package org.vaadin.tori.data.entity;

public class UserWrapper extends User {

    public final com.liferay.portal.model.User liferayUser;

    private UserWrapper(final com.liferay.portal.model.User liferayUser) {
        this.liferayUser = liferayUser;
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

    public static User wrap(final com.liferay.portal.model.User liferayUser) {
        if (liferayUser != null) {
            return new UserWrapper(liferayUser);
        } else {
            return null;
        }
    }

}
