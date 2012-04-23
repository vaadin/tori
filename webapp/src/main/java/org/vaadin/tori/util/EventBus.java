package org.vaadin.tori.util;

import org.vaadin.tori.ToriApplication;

import com.github.wolfie.blackboard.Blackboard;
import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.exception.DuplicateRegistrationException;

/**
 * Utility class that provides static methods for Blackboard operations.
 * 
 */
public final class EventBus {

    private final transient Blackboard blackboard = new Blackboard();
    private transient Listener currentListener;

    public static EventBus getCurrent() {
        return ToriApplication.getCurrent().getEventBus();
    }

    public static void fire(final Event event) {
        getCurrent().blackboard.fire(event);
    }

    private void setListener_(final Listener listener) {
        if (currentListener != null) {
            blackboard.removeListener(currentListener);
        }
        blackboard.addListener(listener);
        currentListener = listener;
    }

    public static void setListener(final Listener listener) {
        getCurrent().setListener_(listener);
    }

    public static void register(final Class<? extends Event> event) {
        try {
            for (final Class<? extends Object> innerClass : event
                    .getDeclaredClasses()) {
                if (innerClass != null
                        && Listener.class.isAssignableFrom(innerClass)) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends Listener> listenerClass = (Class<? extends Listener>) innerClass;
                    getCurrent().blackboard.register(listenerClass, event);
                }
            }
        } catch (final DuplicateRegistrationException e) {
            // NOP
        }
    }
}
