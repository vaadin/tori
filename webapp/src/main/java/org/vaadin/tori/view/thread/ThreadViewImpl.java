/*
 * Copyright 2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.view.thread;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.AuthoringComponent;
import org.vaadin.tori.component.AuthoringComponent.AuthoringListener;
import org.vaadin.tori.component.Breadcrumbs;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.RecentBar;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;

import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private static final String INPUT_CACHE_NAME = "inputcache";
    private static final String REPLY_ID = "threadreply";
    private static final String STYLE_REPLY_HIDDEN = "replyhidden";

    private CssLayout layout;
    private PostsLayout postsLayout;
    private ThreadUpdatesComponent threadUpdatesComponent;
    private Label mayNotReplyNote;
    private AuthoringComponent reply;

    private ViewData viewData;
    private AuthoringData authoringData;

    private final AuthoringListener replyListener = new AuthoringListener() {
        @Override
        public void submit(final String rawBody,
                final Map<String, byte[]> attachments, final boolean follow) {
            if (!rawBody.trim().isEmpty()) {
                getPresenter().sendReply(rawBody, attachments, follow);
                Breadcrumbs.getCurrent().updateFollowButtonStyle();
            }
        }

        @Override
        public void inputValueChanged(final String value) {
            if (viewData != null) {
                getInputCache().put(viewData.getThreadTopic(), value);
            }
            getPresenter().inputValueChanged();
        }
    };

    @Override
    protected Component createCompositionRoot() {
        layout = new CssLayout();
        return layout;
    }

    @Override
    public void initView() {
        setStyleName("threadview");
        layout.setWidth("100%");
        postsLayout = new PostsLayout(getPresenter());
        layout.addComponent(postsLayout);

        ToriScheduler.get().scheduleManual(new ScheduledCommand() {

            @Override
            public void execute() {
                if (viewData != null) {
                    threadUpdatesComponent = new ThreadUpdatesComponent(
                            getPresenter());
                    layout.addComponent(threadUpdatesComponent);
                    mayNotReplyNote = new Label();
                    mayNotReplyNote.setContentMode(ContentMode.HTML);
                    mayNotReplyNote.addStyleName("maynotreplynote");
                    mayNotReplyNote.setVisible(false);
                    layout.addComponent(mayNotReplyNote);
                    appendNewReply();
                }
            }
        });
    }

    @Override
    protected ThreadPresenter createPresenter() {
        return new ThreadPresenter(this);
    }

    @Override
    public void setPosts(final List<PostData> posts, final Integer selectedIndex) {
        postsLayout.setPosts(posts, selectedIndex);
    }

    @SuppressWarnings("unchecked")
    private Map<Object, String> getInputCache() {
        VaadinSession session = UI.getCurrent().getSession();
        if (session.getAttribute(INPUT_CACHE_NAME) == null) {
            session.setAttribute(INPUT_CACHE_NAME,
                    new HashMap<Object, String>());
        }
        return (Map<Object, String>) session.getAttribute(INPUT_CACHE_NAME);
    }

    @Override
    public void appendPosts(final List<PostData> posts) {
        for (PostData postData : posts) {
            postsLayout
                    .addComponent(new PostComponent(postData, getPresenter()));
        }
        ToriScheduler.get().executeManualCommands();
        appendNewReply();
    }

    @Override
    public void redirectToDashboard() {
        UI.getCurrent()
                .getNavigator()
                .navigateTo(
                        ToriNavigator.ApplicationView.DASHBOARD
                                .getNavigatorUrl());
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

    @Override
    public void appendQuote(final String textToAppend) {
        reply.insertIntoMessage(textToAppend + "\n\n ");
        // Scroll to reply component
        UI.getCurrent().scrollIntoView(reply);
        JavaScript.eval("window.setTimeout(\"document.getElementById('"
                + REPLY_ID + "').scrollIntoView(true)\",100)");
    }

    @Override
    public void showNotification(final String message) {
        Notification.show(message);
    }

    @Override
    public void showError(final String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void setViewData(final ViewData viewData,
            final AuthoringData authoringData) {
        if (viewData == null) {
            ToriNavigator.getCurrent().navigateToDashboard();
        } else {
            this.authoringData = authoringData;
            this.viewData = viewData;
        }
    }

    @Override
    public String getTitle() {
        return viewData != null ? viewData.getThreadTopic() : null;
    }

    @Override
    public Long getUrlParameterId() {
        return viewData != null ? viewData.getThreadId() : null;
    }

    @Override
    public void setThreadUpdates(final int newPostsCount,
            final Map<User, Date> pendingReplies) {
        try {
            getUI().access(new Runnable() {
                @Override
                public void run() {
                    try {
                        threadUpdatesComponent.setNewPostsCount(newPostsCount);
                        threadUpdatesComponent
                                .setPendingReplies(pendingReplies);
                    } catch (RuntimeException e) {
                        // Ignore
                    }
                }
            });
        } catch (RuntimeException e) {
            // Ignore
        }
    }

    @Override
    public void updatePost(final PostData postData) {
        postsLayout.updatePost(postData);
    }

    @Override
    public void replySent() {
        getInputCache().remove(viewData.getThreadTopic());
        ToriUI.getCurrent().trackAction("reply");
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                RecentBar.getCurrent().refresh();
            }
        });
    }

    private void appendNewReply() {
        if (viewData.mayReplyInThread() && !viewData.isUserBanned()) {
            mayNotReplyNote.setVisible(false);

            if (reply != null) {
                reply.addStyleName(STYLE_REPLY_HIDDEN);
            }

            ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {

                    final Component oldReply = reply;
                    reply = new AuthoringComponent(replyListener);
                    reply.setId(REPLY_ID);

                    reply.setAuthoringData(authoringData);
                    reply.insertIntoMessage(getInputCache().get(
                            viewData.getThreadTopic()));
                    layout.addComponent(reply);

                    // Fade in
                    reply.addStyleName(STYLE_REPLY_HIDDEN);
                    ToriScheduler.get().scheduleDeferred(
                            new ScheduledCommand() {
                                @Override
                                public void execute() {
                                    if (oldReply != null) {
                                        layout.removeComponent(oldReply);
                                    }
                                    reply.removeStyleName(STYLE_REPLY_HIDDEN);
                                }
                            });
                }
            });
        } else {
            mayNotReplyNote.setVisible(true);
            String note = viewData.getMayNotReplyNote();
            if (note == null) {
                note = "Please log in to reply";
            }
            if (viewData.isThreadLocked()) {
                note = "Thread locked";
            }

            mayNotReplyNote.setValue(note);
        }
    }

    @Override
    public void exit() {
        super.exit();
        ToriScheduler.get().executeManualCommands();
    }

    @Override
    public void threadDeleted() {
        ToriNavigator.getCurrent().navigateToCategory(viewData.getCategoryId());
    }

    @Override
    public void authoringFailed() {
        reply.reEnablePosting();
    }

}
