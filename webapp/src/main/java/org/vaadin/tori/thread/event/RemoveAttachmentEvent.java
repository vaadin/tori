package org.vaadin.tori.thread.event;

import com.github.wolfie.blackboard.Event;

public class RemoveAttachmentEvent implements Event {
    private final String fileName;

    public RemoveAttachmentEvent(final String fileName) {
        super();
        this.fileName = fileName;
    }

    public final String getFileName() {
        return fileName;
    }
}
