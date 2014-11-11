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