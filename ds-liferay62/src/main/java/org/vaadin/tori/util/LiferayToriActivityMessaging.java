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

public class LiferayToriActivityMessaging implements
        ToriActivityMessaging, PortletRequestAware, MessageListener {

    private static final String TORI_DESTINATION = "tori/activity";

    private static final String TORI_ACTIVITY_ID = "toriactivity";
    private static final String TORI_ACTIVITY_USERTYPING = "usertyping";
    private static final String TORI_ACTIVITY_USERAUTHORED = "userauthored";

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
            .getLogger(LiferayToriActivityMessaging.class);

    private Date lastSent;

    @Override
    public void sendUserTyping(final long threadId, final Date startedTyping) {
        if (lastSent == null
                || System.currentTimeMillis() - lastSent.getTime() > 10000) {
            Message message = new Message();
            message.put(USER_ID, new Long(currentUserId));
            message.put(THREAD_ID, new Long(threadId));
            message.put(STARTED_TYPING, startedTyping.getTime());
            message.put(TORI_ACTIVITY_ID, TORI_ACTIVITY_USERTYPING);
            sendMessage(message);
            lastSent = new Date();
        }
    }

    @Override
    public void sendUserAuthored(final long postId, final long threadId) {
        Message message = new Message();
        message.put(POST_ID, new Long(postId));
        message.put(THREAD_ID, new Long(threadId));
        message.put(TORI_ACTIVITY_ID, TORI_ACTIVITY_USERAUTHORED);
        sendMessage(message);
    }

    private void sendMessage(final Message message) {
        message.put(SENDER_ID, getSenderId());
        MessageBusUtil.sendMessage(TORI_DESTINATION, message);
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
                if (TORI_ACTIVITY_USERAUTHORED.equals(message
                        .get(TORI_ACTIVITY_ID))) {
                    // Fire user authored events
                    for (UserAuthoredListener listener : userAuthoredListeners) {
                        listener.userAuthored(message.getLong(POST_ID),
                                message.getLong(THREAD_ID));
                    }
                } else if (TORI_ACTIVITY_USERTYPING.equals(message
                        .get(TORI_ACTIVITY_ID))) {
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
        MessageBusUtil.unregisterMessageListener(TORI_DESTINATION, this);
    }

    @Override
    public void register() {
        if (!MessageBusUtil.getMessageBus().hasDestination(TORI_DESTINATION)) {
            log.info("Adding a message bus destination: " + TORI_DESTINATION);
            @SuppressWarnings("deprecation")
            Destination destination = new ParallelDestination(TORI_DESTINATION);
            destination.open();
            MessageBusUtil.addDestination(destination);
        }

        MessageBusUtil.registerMessageListener(TORI_DESTINATION, this);
    }

}
