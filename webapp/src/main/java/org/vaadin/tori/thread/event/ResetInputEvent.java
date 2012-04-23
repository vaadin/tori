package org.vaadin.tori.thread.event;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class ResetInputEvent implements Event {
    public interface ResetInputListener extends Listener {
        void resetInput(final ResetInputEvent event);
    }
}
