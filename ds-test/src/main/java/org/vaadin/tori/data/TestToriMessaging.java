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

package org.vaadin.tori.data;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.tori.util.ToriActivityMessaging;

public class TestToriMessaging implements ToriActivityMessaging {

    private static Set<UserTypingListener> userTypingListeners = Collections
            .synchronizedSet(new HashSet<UserTypingListener>());

    private static Set<UserAuthoredListener> userAuthoredListeners = Collections
            .synchronizedSet(new HashSet<UserAuthoredListener>());

    private final Set<Object> thisListeners = new HashSet<Object>();;

    @Override
    public void addUserTypingListener(UserTypingListener listener) {
        userTypingListeners.add(listener);
        thisListeners.add(listener);
    }

    @Override
    public void addUserAuthoredListener(UserAuthoredListener listener) {
        userAuthoredListeners.add(listener);
        thisListeners.add(listener);
    }

    @Override
    public void removeUserTypingListener(UserTypingListener listener) {
        userTypingListeners.remove(listener);
        thisListeners.remove(listener);
    }

    @Override
    public void removeUserAuthoredListener(UserAuthoredListener listener) {
        userAuthoredListeners.remove(listener);
        thisListeners.remove(listener);
    }

    @Override
    public void sendUserTyping(long threadId, Date startedTyping) {
        for (UserTypingListener listener : userTypingListeners) {
            if (!thisListeners.contains(listener)) {
                listener.userTyping(TestDataSource.CURRENT_USER_ID, threadId,
                        startedTyping);
            }
        }
    }

    @Override
    public void sendUserAuthored(long postId, long threadId) {
        for (UserAuthoredListener listener : userAuthoredListeners) {
            if (!thisListeners.contains(listener)) {
                listener.userAuthored(postId, threadId);
            }
        }
    }

}
