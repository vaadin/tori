package org.vaadin.tori.service;

/**
 * These are defined for Liferay in
 * portal-impl/resource-actions/messageboards.xml file (see Liferay source
 * code).
 */
public class LiferayAuthorizationConstants {

    public enum MbAction {
        ADD_CATEGORY,
        ADD_FILE,
        ADD_MESSAGE,
        BAN_USER,
        MOVE_THREAD,
        LOCK_THREAD,
        PERMISSIONS,
        REPLY_TO_MESSAGE,
        SUBSCRIBE,
        UPDATE_THREAD_PRIORITY;

        public static String getScope() {
            return "com.liferay.portlet.messageboards";
        }
    }

    public enum CategoryAction {
        ADD_FILE,
        ADD_MESSAGE,
        ADD_SUBCATEGORY,
        DELETE,
        LOCK_THREAD,
        MOVE_THREAD,
        PERMISSIONS,
        REPLY_TO_MESSAGE,
        SUBSCRIBE,
        UPDATE,
        UPDATE_THREAD_PRIORITY,
        VIEW;

        public static String getScope() {
            return "com.liferay.portlet.messageboards.model.MBCategory";
        }
    }

    public enum MessageAction {
        DELETE, PERMISSIONS, SUBSCRIBE, UPDATE, VIEW;

        public static String getScope() {
            return "com.liferay.portlet.messageboards.model.MBMessage";
        }
    }

}