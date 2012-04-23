package org.vaadin.tori.thread.event;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class AddAttachmentEvent implements Event {
    private final String fileName;
    private final byte[] data;

    public AddAttachmentEvent(final String fileName, final byte[] data) {
        super();
        this.fileName = fileName;
        this.data = data;
    }

    public final String getFileName() {
        return fileName;
    }

    public final byte[] getData() {
        return data;
    }

    public interface AddAttachmentListener extends Listener {
        void addAttachment(final AddAttachmentEvent event);
    }
}
