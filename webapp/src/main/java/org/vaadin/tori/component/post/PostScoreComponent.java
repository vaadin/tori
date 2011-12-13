package org.vaadin.tori.component.post;

import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.thread.ThreadPresenter;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;

@SuppressWarnings("serial")
public class PostScoreComponent extends CustomComponent {
    private final ThreadPresenter presenter;
    private final Post post;

    private final CssLayout layout = new CssLayout();
    private final Label score;

    public PostScoreComponent(final Post post, final ThreadPresenter presenter) {
        this.post = post;
        this.presenter = presenter;
        setCompositionRoot(layout);
        layout.setWidth("100%");
        setWidth("50px");
        setStyleName("scorecomponent");

        score = new Label();
        layout.addComponent(score);
    }

    public void enableUpDownVoting(final PostVote postVote) {
        layout.removeAllComponents();
        final NativeButton upvote = new NativeButton();
        upvote.setStyleName("upvote");
        upvote.addStyleName("vote");
        if (postVote.isUpvote()) {
            upvote.addStyleName("done");
        }
        upvote.addListener(new NativeButton.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                try {
                    presenter.upvote(post);
                } catch (final DataSourceException e) {
                    getApplication().getMainWindow().showNotification(
                            DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                }
            }
        });
        layout.addComponent(upvote);

        final NativeButton downvote = new NativeButton();
        downvote.setStyleName("downvote");
        downvote.addStyleName("vote");
        if (postVote.isDownvote()) {
            downvote.addStyleName("done");
        }
        downvote.addListener(new NativeButton.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                try {
                    presenter.downvote(post);
                } catch (final DataSourceException e) {
                    getApplication().getMainWindow().showNotification(
                            DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                }
            }
        });
        layout.addComponent(downvote);
        layout.addComponent(score);
    }

    public void setScore(final long newScore) {
        score.setValue(String.valueOf(newScore));
    }
}
