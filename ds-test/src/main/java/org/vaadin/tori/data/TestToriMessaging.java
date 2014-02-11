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
import java.util.HashSet;
import java.util.Set;

import org.vaadin.tori.util.ToriActivityMessaging;

public class TestToriMessaging implements ToriActivityMessaging {

    private static Set<UserTypingListener> userTypingListeners = Collections
            .synchronizedSet(new HashSet<UserTypingListener>());

    private static Set<UserAuthoredListener> userAuthoredListeners = Collections
            .synchronizedSet(new HashSet<UserAuthoredListener>());

    @Override
    public void addUserTypingListener(UserTypingListener listener) {
        userTypingListeners.add(listener);
    }

    @Override
    public void addUserAuthoredListener(UserAuthoredListener listener) {
        userAuthoredListeners.add(listener);
    }

    @Override
    public void removeUserTypingListener(UserTypingListener listener) {
        userTypingListeners.remove(listener);
    }

    @Override
    public void removeUserAuthoredListener(UserAuthoredListener listener) {
        userAuthoredListeners.remove(listener);
    }

    @Override
    public void sendUserTyping(long threadId) {
        for (UserTypingListener listener : userTypingListeners) {
            listener.userTyping(TestDataSource.CURRENT_USER_ID, threadId);
        }
    }

    @Override
    public void sendUserAuthored(long postId, long threadId) {
        for (UserAuthoredListener listener : userAuthoredListeners) {
            listener.userAuthored(postId, threadId);
        }
    }

}
