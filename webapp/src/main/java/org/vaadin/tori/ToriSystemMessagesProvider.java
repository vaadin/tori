package org.vaadin.tori;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;

public class ToriSystemMessagesProvider implements SystemMessagesProvider {
    private static final long serialVersionUID = -7073677084227514918L;

    /**
     * Lazy loaded singleton, courtesy of the JVM. See: <a href=
     * "http://stackoverflow.com/questions/3635396/pattern-for-lazy-thread-safe-singleton-instantiation-in-java"
     * >StackOverflow</a>
     */
    private static final ToriSystemMessagesProvider INSTANCE = new ToriSystemMessagesProvider();

    private ToriSystemMessagesProvider() {
    }

    @Override
    public SystemMessages getSystemMessages(
            final SystemMessagesInfo systemMessagesInfo) {
        final CustomizedSystemMessages msgs = new CustomizedSystemMessages();
        msgs.setCommunicationErrorNotificationEnabled(false);
        msgs.setSessionExpiredNotificationEnabled(false);
        return msgs;
    }

    public static SystemMessagesProvider get() {
        return INSTANCE;
    }

}
