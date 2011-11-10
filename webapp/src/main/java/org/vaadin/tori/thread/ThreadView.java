package org.vaadin.tori.thread;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.mvp.View;

public interface ThreadView extends View {

    DiscussionThread getCurrentThread();

}
