package org.vaadin.tori.thread;

import org.vaadin.tori.thread.event.AddAttachmentEvent;
import org.vaadin.tori.thread.event.RemoveAttachmentEvent;
import org.vaadin.tori.thread.event.ResetInputEvent;

import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

public interface ThreadViewListener extends Listener {
    @ListenerMethod
    void addAttachment(final AddAttachmentEvent event);

    @ListenerMethod
    void removeAttachment(final RemoveAttachmentEvent event);

    @ListenerMethod
    void resetInput(final ResetInputEvent event);
}