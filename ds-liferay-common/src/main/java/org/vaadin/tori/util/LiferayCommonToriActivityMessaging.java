/*
 * Copyright 2014 Vaadin Ltd.
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

package org.vaadin.tori.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.portlet.PortletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.PortletRequestAware;

import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.messaging.ParallelDestination;

public class LiferayCommonToriActivityMessaging implements
        ToriActivityMessaging, PortletRequestAware, MessageListener {

    private static final String USER_TYPING_DESTINATION = "tori/activity/usertyping";
    private static final String USER_AUTHORED_DESTINATION = "tori/activity/userauthored";

    private static final String SENDER_ID = "SENDER_ID";

    private static final String USER_ID = "USER_ID";
    private static final String THREAD_ID = "THREAD_ID";
    private static final String STARTED_TYPING = "STARTED_TYPING";
    private static final String POST_ID = "POST_ID";

    private PortletRequest request;
    private long currentUserId;

    private final Collection<UserTypingListener> userTypingListeners = new HashSet<UserTypingListener>();
    private final Collection<UserAuthoredListener> userAuthoredListeners = new HashSet<UserAuthoredListener>();

    private final Logger log = Logger
            .getLogger(LiferayCommonToriActivityMessaging.class);

    public LiferayCommonToriActivityMessaging() {
        for (String destinationName : Arrays.asList(USER_AUTHORED_DESTINATION,
                USER_TYPING_DESTINATION)) {
            if (!MessageBusUtil.getMessageBus().hasDestination(destinationName)) {
                log.info("Adding a message bus destination: " + destinationName);
                @SuppressWarnings("deprecation")
                Destination destination = new ParallelDestination(
                        destinationName);
                destination.open();
                MessageBusUtil.addDestination(destination);
            }
        }

        MessageBusUtil.registerMessageListener(USER_AUTHORED_DESTINATION, this);
        MessageBusUtil.registerMessageListener(USER_TYPING_DESTINATION, this);
    }

    @Override
    public void sendUserTyping(final long threadId, final Date startedTyping) {
        Message message = new Message();
        message.put(USER_ID, new Long(currentUserId));
        message.put(THREAD_ID, new Long(threadId));
        message.put(STARTED_TYPING, startedTyping.getTime());
        sendMessage(message, USER_TYPING_DESTINATION);
    }

    @Override
    public void sendUserAuthored(final long postId, final long threadId) {
        Message message = new Message();
        message.put(POST_ID, new Long(postId));
        message.put(THREAD_ID, new Long(threadId));
        sendMessage(message, USER_AUTHORED_DESTINATION);
    }

    private void sendMessage(final Message message, final String destinationName) {
        message.put(SENDER_ID, getSenderId());
        MessageBusUtil.sendMessage(destinationName, message);
    }

    private String getSenderId() {
        return request.getPortletSession().getId();
    }

    private boolean isThisSender(final Message message) {
        Object senderId = message.get(SENDER_ID);
        return senderId != null && senderId.equals(getSenderId());
    }

    @Override
    public void addUserTypingListener(final UserTypingListener listener) {
        userTypingListeners.add(listener);
    }

    @Override
    public void addUserAuthoredListener(final UserAuthoredListener listener) {
        userAuthoredListeners.add(listener);
    }

    @Override
    public void removeUserTypingListener(final UserTypingListener listener) {
        userTypingListeners.remove(listener);
    }

    @Override
    public void removeUserAuthoredListener(final UserAuthoredListener listener) {
        userAuthoredListeners.remove(listener);
    }

    private boolean isSessionAlive() {
        boolean result = true;
        try {
            request.getPortletSession().getAttribute("test");
        } catch (IllegalStateException e) {
            result = false;
        }
        return result;
    }

    @Override
    public void setRequest(final PortletRequest request) {
        this.request = request;
        if (currentUserId == 0 && request.getRemoteUser() != null) {
            currentUserId = Long.valueOf(request.getRemoteUser());
        }
    }

    @Override
    public void receive(final Message message) {
        if (isSessionAlive()) {
            if (!isThisSender(message)) {
                if (USER_AUTHORED_DESTINATION.equals(message
                        .getDestinationName())) {
                    // Fire user authored events
                    for (UserAuthoredListener listener : userAuthoredListeners) {
                        listener.userAuthored(message.getLong(POST_ID),
                                message.getLong(THREAD_ID));
                    }
                } else if (USER_TYPING_DESTINATION.equals(message
                        .getDestinationName())) {
                    // Fire user typing events
                    for (UserTypingListener listener : userTypingListeners) {
                        listener.userTyping(message.getLong(USER_ID),
                                message.getLong(THREAD_ID),
                                new Date(message.getLong(STARTED_TYPING)));
                    }
                }
            }
        } else {
            deregister();
        }
    }

    @Override
    public void deregister() {
        MessageBusUtil.unregisterMessageListener(USER_AUTHORED_DESTINATION,
                this);
        MessageBusUtil.unregisterMessageListener(USER_TYPING_DESTINATION, this);
    }

}
