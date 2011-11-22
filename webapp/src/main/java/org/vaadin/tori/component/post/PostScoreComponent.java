package org.vaadin.tori.component.post;

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.thread.ThreadPresenter;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;

@SuppressWarnings("serial")
public class PostScoreComponent extends CustomComponent {
    private final ThreadPresenter presenter;
    private final Post post;

    private final CssLayout layout = new CssLayout();

    public PostScoreComponent(final Post post, final ThreadPresenter presenter) {
        this.post = post;
        this.presenter = presenter;
        setCompositionRoot(layout);
        layout.setWidth("100%");
        setWidth("50px");
        setStyleName("scorecomponent");

        layout.addComponent(new Label(String.valueOf(post.getScore())));
    }

    public void enableUpDownVoting() {
        layout.removeAllComponents();
        final NativeButton upvote = new NativeButton();
        upvote.setStyleName("upvote");
        upvote.addStyleName("vote");
        layout.addComponent(upvote);

        final NativeButton downvote = new NativeButton();
        downvote.setStyleName("downvote");
        downvote.addStyleName("vote");
        layout.addComponent(downvote);

        layout.addComponent(new Label(String.valueOf(post.getScore())));
    }
}
