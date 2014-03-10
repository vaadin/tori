package org.vaadin.tori.util;

import java.util.Date;

public interface ToriActivityMessaging {

    void addUserTypingListener(UserTypingListener listener);

    void addUserAuthoredListener(UserAuthoredListener listener);

    void removeUserTypingListener(UserTypingListener listener);

    void removeUserAuthoredListener(UserAuthoredListener listener);

    void sendUserTyping(long threadId, Date startedTyping);

    void sendUserAuthored(long postId, long threadId);

    void deregister();

    public interface UserTypingListener {
        void userTyping(long userId, long threadId, Date startedTyping);
    }

    public interface UserAuthoredListener {
        void userAuthored(long postId, long threadId);
    }
}
