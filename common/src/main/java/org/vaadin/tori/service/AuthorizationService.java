package org.vaadin.tori.service;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides methods for specifying access rights to resources or certain
 * operations.
 */
public interface AuthorizationService {

    boolean mayEditCategories();

    boolean mayRearrangeCategories();

    boolean mayReportPosts();

    boolean mayFollow(@NonNull Category category);

    boolean mayDelete(@NonNull Category category);

    boolean mayEdit(@NonNull Category category);

    boolean mayEdit(@NonNull Post post);

    boolean mayReplyIn(@NonNull DiscussionThread thread);

    boolean mayBan();

    boolean mayFollow(@NonNull DiscussionThread currentThread);

    boolean mayDelete(@NonNull Post post);

    boolean mayVote();

    boolean mayMove(@NonNull DiscussionThread thread);

    boolean maySticky(@NonNull DiscussionThread thread);

    boolean mayLock(@NonNull DiscussionThread thread);

    boolean mayDelete(@NonNull DiscussionThread thread);

    boolean mayCreateThreadIn(@NonNull Category category);

    boolean mayAddFiles(@NonNull Category category);

    boolean mayView(@NonNull Category category);

}
