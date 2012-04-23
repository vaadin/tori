package org.vaadin.tori.thread;

import org.vaadin.tori.thread.event.AddAttachmentEvent.AddAttachmentListener;
import org.vaadin.tori.thread.event.RemoveAttachmentEvent.RemoveAttachmentListener;
import org.vaadin.tori.thread.event.ResetInputEvent.ResetInputListener;

public interface ThreadViewListener extends AddAttachmentListener,
        ResetInputListener, RemoveAttachmentListener {

}