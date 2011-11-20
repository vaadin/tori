package org.vaadin.tori.thread;

import java.util.List;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.mvp.View;

public interface ThreadView extends View {
    DiscussionThread getCurrentThread();

    void displayPosts(List<Post> posts);

    void displayThreadNotFoundError(String threadIdString);

    void confirmPostReported();

    void confirmReplyPosted();
}
