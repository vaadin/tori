package org.vaadin.tori.util;

import java.util.Date;

public interface ToriActivityMessaging {

    public void addUserTypingListener(UserTypingListener listener);

    public void addUserAuthoredListener(UserAuthoredListener listener);

    public void removeUserTypingListener(UserTypingListener listener);

    public void removeUserAuthoredListener(UserAuthoredListener listener);

    public void sendUserTyping(long threadId, Date startedTyping);

    public void sendUserAuthored(long postId, long threadId);

    public interface UserTypingListener {
        void userTyping(long userId, long threadId, Date startedTyping);
    }

    public interface UserAuthoredListener {
        void userAuthored(long postId, long threadId);
    }
}
