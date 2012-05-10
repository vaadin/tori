package org.vaadin.tori.util;

import org.vaadin.tori.ToriRoot;

import com.github.wolfie.blackboard.Blackboard;
import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.vaadin.ui.Root;

/**
 * Utility class that provides static methods for Blackboard operations.
 * 
 */
public final class EventBus {

    private final transient Blackboard blackboard = new Blackboard();
    private transient Listener currentListener;

    // TODO: Remove. A temporary hack needed currently.
    public static ThreadLocal<ToriRoot> currentRoot = new ThreadLocal<ToriRoot>();

    public static EventBus getCurrent() {
        try {
            return ((ToriRoot) Root.getCurrentRoot()).getEventBus();
        } catch (final Exception e) {
            // TODO: Remove. Development-time nightly build of V7 will return
            // null on Root.getCurrentRoot() in certain cases so we'll use this
            // temporary hack.
            return currentRoot.get().getEventBus();
        }
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
}
