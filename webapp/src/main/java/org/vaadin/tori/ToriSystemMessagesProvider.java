package org.vaadin.tori;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;

@SuppressWarnings("serial")
public class ToriSystemMessagesProvider implements SystemMessagesProvider {
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
