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
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;

    private final static String INPUT_CACHE_NAME = "inputcache";
    private final static String REPLY_ID = "threadreply";
    private final static String STYLE_REPLY_HIDDEN = "replyhidden";
    private PostsLayout postsLayout;
    private AuthoringComponent reply;
    private ViewData viewData;
    private ThreadUpdatesComponent threadUpdatesComponent;

    @Override
    protected Component createCompositionRoot() {
        return layout = new CssLayout();
    }

    @Override
    public void initView() {
        setStyleName("threadview");
        layout.setWidth("100%");
        layout.addComponent(buildPostsLayout());

        final Component reply = buildReply();
        ToriScheduler.get().scheduleManual(new ScheduledCommand() {
            @Override
            public void execute() {
                threadUpdatesComponent = new ThreadUpdatesComponent(
                        getPresenter());
                layout.addComponent(threadUpdatesComponent);
                layout.addComponent(reply);
            }
        });
    }

    private Component buildPostsLayout() {
        return postsLayout = new PostsLayout(getPresenter());
    }

    @Override
    protected ThreadPresenter createPresenter() {
        return new ThreadPresenter(this);
    }

    private Component buildReply() {
        AuthoringListener replyListener = new AuthoringListener() {
            @Override
            public void submit(String rawBody, Map<String, byte[]> attachments,
                    boolean follow) {
                if (!rawBody.trim().isEmpty()) {
                    getPresenter().sendReply(rawBody, attachments, follow);
                    getInputCache().remove(viewData.getThreadTopic());
                    ToriUI.getCurrent().trackAction("reply");
                    reply.addStyleName(STYLE_REPLY_HIDDEN);
                    ToriScheduler.get().scheduleDeferred(
                            new ScheduledCommand() {
                                @Override
                                public void execute() {
                                    reply.removeStyleName(STYLE_REPLY_HIDDEN);
                                }
                            });
                }
            }

            @Override
            public void inputValueChanged(String value) {
                if (viewData != null) {
                    getInputCache().put(viewData.getThreadTopic(), value);
                }
                getPresenter().inputValueChanged();
            }
        };
        reply = new AuthoringComponent(replyListener);
        reply.setId(REPLY_ID);
        return reply;
    }

    @Override
    public void setPosts(final List<PostData> posts) {
        postsLayout.setPosts(posts);
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
                + REPLY_ID + "').scrollIntoView()\",100)");
    }

    @Override
    public void showNotification(String message) {
        Notification.show(message);
    }

    @Override
    public void showError(String message) {
        Notification.show(message, Type.ERROR_MESSAGE);
    }

    @Override
    public void setViewData(ViewData viewData) {
        reply.setViewData(viewData);
        reply.setVisible(viewData.mayReplyInThread()
                && !viewData.isUserBanned());
        reply.insertIntoMessage(getInputCache().get(viewData.getThreadTopic()));
        this.viewData = viewData;
    }

    @Override
    public String getTitle() {
        return viewData.getThreadTopic();
    }

    @Override
    public Long getUrlParameterId() {
        return viewData.getThreadId();
    }

    @Override
    public void setThreadUpdates(final int newPostsCount,
            final Map<User, Date> pendingReplies) {
        try {
            getUI().access(new Runnable() {
                @Override
                public void run() {
                    threadUpdatesComponent.setNewPostsCount(newPostsCount);
                    threadUpdatesComponent.setPendingReplies(pendingReplies);
                }
            });
        } catch (UIDetachedException e) {
            // Ignore
        }
    }

    @Override
    public void updatePost(PostData postData) {
        postsLayout.updatePost(postData);
    }

}
