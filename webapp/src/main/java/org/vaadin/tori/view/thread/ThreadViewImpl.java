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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.AuthoringComponent;
import org.vaadin.tori.component.AuthoringComponent.AuthoringListener;
import org.vaadin.tori.component.BBCodeWysiwygEditor;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.post.PostsLayout;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;

    private final static String INPUT_CACHE_NAME = "inputcache";
    private final static String REPLY_ID = "threadreply";
    private PostsLayout postsLayout;
    private AuthoringComponent reply;
    private String threadTopic;

    private long threadId;

    @Override
    protected Component createCompositionRoot() {
        return layout = new CssLayout();
    }

    @Override
    public void initView() {
        setStyleName("threadview");
        layout.setWidth("100%");
        layout.addComponent(buildPostsLayout());
        layout.addComponent(buildReply());
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
            public void submit(String rawBody, Map<String, byte[]> attachments) {
                if (!rawBody.trim().isEmpty()) {
                    getPresenter().sendReply(rawBody, attachments);
                    ToriUI.getCurrent().trackAction("reply");
                }
            }
        };
        reply = new AuthoringComponent(replyListener, "Post body", true);
        reply.setId(REPLY_ID);
        reply.getInput().setValue(getInputCache().get(threadTopic));
        reply.getInput().addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                getInputCache().put(threadTopic,
                        (String) event.getProperty().getValue());
                getPresenter().inputValueChanged();
            }
        });

        BBCodeWysiwygEditor editor = (BBCodeWysiwygEditor) reply.getInput();
        editor.addBlurListener(new BlurListener() {
            @Override
            public void blur(BlurEvent event) {
                UI.getCurrent().setPollInterval(ToriUI.DEFAULT_POLL_INTERVAL);
            }
        });

        editor.addFocusListener(new FocusListener() {
            @Override
            public void focus(FocusEvent event) {
                UI.getCurrent().setPollInterval(3000);
            }
        });

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
    public void confirmReplyPostedAndShowIt(final Post newPost) {
        // postsLayout.addComponent(newPostComponent(newPost));
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
        JavaScript.eval("document.getElementById('" + REPLY_ID
                + "').scrollIntoView()");
    }

    @Override
    public void otherUserAuthored(final Post post) {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                // postsLayout.addComponent(newPostComponent(post));
            }
        });
    }

    @Override
    public void otherUserTyping(final User user) {
        getUI().access(new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                String userName = "Anonymous user";
                if (user != null) {
                    userName = "User " + user.getDisplayedName();
                }
                getUI().showNotification(userName + " is typing to this thread");
            }
        });
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
    public void setViewPermissions(ViewPermissions viewPermissions) {
        reply.setUserMayAddFiles(viewPermissions.mayAddFiles());
        reply.setMaxFileSize(viewPermissions.getMaxFileSize());
        reply.setVisible(viewPermissions.mayReplyInThread());
    }

    @Override
    public String getTitle() {
        return threadTopic;
    }

    @Override
    public Long getUrlParameterId() {
        return threadId;
    }

    @Override
    public void setThread(DiscussionThread currentThread) {
        threadTopic = currentThread.getTopic();
        threadId = currentThread.getId();
    }
}
