/*
 * Copyright 2012 Vaadin Ltd.
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

package org.vaadin.tori.thread;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.AuthoringComponent;
import org.vaadin.tori.component.AuthoringComponent.AuthoringListener;
import org.vaadin.tori.component.BBCodeWysiwygEditor;
import org.vaadin.tori.component.FloatingBar;
import org.vaadin.tori.component.FloatingBar.FloatingAlignment;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.PanicComponent;
import org.vaadin.tori.component.post.PostComponent;
import org.vaadin.tori.component.post.PostsLayout;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.mvp.AbstractView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import edu.umd.cs.findbugs.annotations.CheckForNull;

@SuppressWarnings("serial")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD_STORE", justification = "We're ignoring serialization")
public class ThreadViewImpl extends AbstractView<ThreadView, ThreadPresenter>
        implements ThreadView {

    private CssLayout layout;
    private final AuthoringListener replyListener = new AuthoringListener() {
        @Override
        public void submit(final String rawBody) {
            if (!rawBody.trim().isEmpty()) {
                try {
                    getPresenter().sendReply(rawBody);
                    ToriUI.getCurrent().trackAction(null, "reply");
                } catch (final DataSourceException e) {
                    Notification
                            .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                }
            }
        }

        @Override
        public void addAttachment(final String attachmentFileName,
                final byte[] data) {
            getPresenter().addAttachment(attachmentFileName, data);
        }

        @Override
        public void resetInput() {
            getInputCache().put(getCurrentThread().getId(), null);
            getPresenter().resetInput();
        }

        @Override
        public void removeAttachment(final String fileName) {
            getPresenter().removeAttachment(fileName);
        }
    };

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "We don't care about serialization")
    private final static String COLLAPSED = "collapsed";
    private final static String INPUT_CACHE_NAME = "inputcache";
    private final Map<Post, PostComponent> postsToComponents = new HashMap<Post, PostComponent>();
    private final PostsLayout postsLayout;
    private AuthoringComponent reply;
    private AuthoringComponent newThreadComponent;
    private AuthoringComponent quickReply;
    private Label showOrHideLabel;
    private CssLayout quickReplyLayout;

    public ThreadViewImpl() {
        setStyleName("threadview");

        postsLayout = new PostsLayout();
    }

    @Override
    protected Component createCompositionRoot() {
        return layout = new CssLayout();
    }

    @Override
    public void initView() {
        layout.setWidth("100%");
    }

    @Override
    protected ThreadPresenter createPresenter() {
        return new ThreadPresenter(this);
    }

    /**
     * @return returns <code>null</code> if the visitor has entered an invalid
     *         URL or a new thread is being created.
     */
    @CheckForNull
    @Override
    public DiscussionThread getCurrentThread() {
        return getPresenter().getCurrentThread();
    }

    /**
     * @return <code>null</code> if the visitor has entered an invalid URL.
     */
    @CheckForNull
    @Override
    public Category getCurrentCategory() {
        return getPresenter().getCurrentCategory();
    }

    @Override
    public void displayPosts(final List<Post> posts, Long selectedPostId) {
        layout.removeAllComponents();

        postsLayout.removeAllComponents();

        for (Post post : posts) {
            Component postComponent = newPostComponent(post);
            postsLayout.addComponent(postComponent);
            if (selectedPostId != null && selectedPostId.equals(post.getId())) {
                postsLayout.setScrollToComponent(postComponent);
            }
        }

        layout.addComponent(postsLayout);

        final Post post = posts.get(0);
        final PostComponent c = postsToComponents.get(post);
        final FloatingBar summaryBar = getSummaryBar(post, c);
        summaryBar.setScrollComponent(c);
        summaryBar.setAlignment(FloatingAlignment.TOP);
        layout.addComponent(summaryBar);

        if (getPresenter().userMayReply()) {
            final Label spacer = new Label("<span class=\"eof\">eof</span>",
                    ContentMode.HTML);
            spacer.setStyleName("spacer");
            layout.addComponent(spacer);

            reply = new AuthoringComponent(replyListener, "Post Reply", true);
            reply.setUserMayAddFiles(getPresenter().userMayAddFiles());
            reply.setMaxFileSize(getPresenter().getMaxFileSize());
            reply.getInput().setValue(
                    getInputCache().get(getCurrentThread().getId()));
            reply.getInput().addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    getInputCache().put(getCurrentThread().getId(),
                            (String) event.getProperty().getValue());

                    getPresenter().inputValueChanged();

                }
            });

            BBCodeWysiwygEditor editor = (BBCodeWysiwygEditor) reply.getInput();
            editor.addBlurListener(new BlurListener() {
                @Override
                public void blur(BlurEvent event) {
                    UI.getCurrent().setPollInterval(
                            ToriUI.DEFAULT_POLL_INTERVAL);
                }
            });

            editor.addFocusListener(new FocusListener() {
                @Override
                public void focus(FocusEvent event) {
                    UI.getCurrent().setPollInterval(3000);
                }
            });

            layout.addComponent(reply);

            layout.addComponent(getQuickReplyBar(reply));
            setQuickReplyVisible(false);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, String> getInputCache() {
        VaadinSession session = UI.getCurrent().getSession();
        if (session.getAttribute(INPUT_CACHE_NAME) == null) {
            session.setAttribute(INPUT_CACHE_NAME, new HashMap<Long, String>());
        }
        return (Map<Long, String>) session.getAttribute(INPUT_CACHE_NAME);
    }

    private PostComponent newPostComponent(final Post post) {
        final PostComponent c = new PostComponent(post, getPresenter(), true);
        postsToComponents.put(post, c);

        if (post.getAuthor().isBanned()) {
            c.setUserIsBanned();
        }

        // main component permissions

        if (getPresenter().userMayReportPosts()) {
            c.enableReporting();
        }
        if (getPresenter().userMayEdit(post)) {
            c.enableEditing();
        }
        if (getPresenter().userMayQuote(post)) {
            c.enableQuoting();
        }
        if (getPresenter().userMayVote()) {
            try {
                c.enableUpDownVoting(getPresenter().getPostVote(post));
            } catch (final DataSourceException e) {
                // NOP - everything's logged.
            }
        }

        // context menu permissions

        try {
            if (getPresenter().userCanFollowThread()) {
                c.enableThreadFollowing();
            }
            if (getPresenter().userCanUnFollowThread()) {
                c.enableThreadUnFollowing();
            }
        } catch (final DataSourceException e) {
            // NOP - everything's logged. If you can't follow, you can't
            // unfollow either.
        }

        if (getPresenter().userMayBan()) {
            if (!post.getAuthor().isBanned()) {
                c.enableBanning();
            } else {
                c.enableUnbanning();
            }
        }

        if (getPresenter().userMayDelete(post)) {
            c.enableDeleting();
        }
        return c;
    }

    private PostComponent newPostSummaryComponent(final Post post) {
        final PostComponent c = new PostComponent(post, getPresenter(), true);
        c.addStyleName("summary");
        return c;
    }

    private Component getThreadSummary(final Post firstPost) {
        final DiscussionThread thread = getCurrentThread();
        if (thread == null) {
            return new Label("No thread selected");
        }

        final VerticalLayout summaryLayout = new VerticalLayout();

        summaryLayout.setWidth(100.0f, Unit.PERCENTAGE);
        summaryLayout.addStyleName("threadSummary");

        final PostComponent postSummary = newPostSummaryComponent(firstPost);
        postSummary.setVisible(false);

        final String topicXhtml = String
                .format("Thread: <strong>%s</strong> started by <strong>%s</strong> %s",
                        thread.getTopic(), firstPost.getAuthor()
                                .getDisplayedName(), new PrettyTime()
                                .format(firstPost.getTime()));
        final Label topicLabel = new Label(topicXhtml, ContentMode.HTML);
        topicLabel.setWidth(null);
        topicLabel.setStyleName("topiclabel");

        final String showPostContentCaption = "Show post content";
        final String hidePostContentCaption = "Hide post content";
        final String collapsedStyle = "collapsed";
        final Label showOrHideLabel = new Label(showPostContentCaption);
        showOrHideLabel.setStyleName("show-or-hide");
        showOrHideLabel.addStyleName(collapsedStyle);
        showOrHideLabel.setWidth(null);
        summaryLayout.addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(final LayoutClickEvent event) {
                if (!event.isDoubleClick()) {
                    postSummary.setVisible(!postSummary.isVisible());
                    showOrHideLabel.setValue(postSummary.isVisible() ? hidePostContentCaption
                            : showPostContentCaption);
                    if (postSummary.isVisible()) {
                        showOrHideLabel.removeStyleName(collapsedStyle);
                    } else {
                        showOrHideLabel.addStyleName(collapsedStyle);
                    }
                }
            }
        });

        final CssLayout topRow = new CssLayout();
        topRow.addStyleName("topRow");
        topRow.setWidth("100%");
        topRow.addComponent(topicLabel);
        topRow.addComponent(showOrHideLabel);

        summaryLayout.addComponent(topRow);
        summaryLayout.addComponent(postSummary);

        return summaryLayout;
    }

    private FloatingBar getSummaryBar(final Post post,
            final PostComponent originalPost) {
        final FloatingBar bar = new FloatingBar();
        bar.addStyleName("threadSummaryBar");
        bar.addStyleName(UI.getCurrent().getTheme());
        bar.setContent(getThreadSummary(post));
        return bar;
    }

    private FloatingBar getQuickReplyBar(
            final AuthoringComponent mirroredAuthoringComponent) {
        quickReply = new AuthoringComponent(replyListener, "Quick Reply", false);

        // Using the TextArea of the AuthoringComponent as the property data
        // source
        // to keep the two editors in sync.
        quickReply.setUserMayAddFiles(false);
        quickReply.getInput().setPropertyDataSource(
                mirroredAuthoringComponent.getInput());
        quickReply.setWidth(100.0f, Unit.PERCENTAGE);
        quickReply.getInput().setHeight(140.0f, Unit.PIXELS);

        quickReplyLayout = new CssLayout();
        quickReplyLayout.setStyleName("quickreplylayout");
        quickReplyLayout.setWidth(100.0f, Unit.PERCENTAGE);

        showOrHideLabel = new Label();
        showOrHideLabel.addStyleName("show-or-hide");
        showOrHideLabel.setSizeUndefined();

        CssLayout showOrHideLayout = new CssLayout(showOrHideLabel);
        showOrHideLayout.setStyleName("showorhidelayout");
        showOrHideLayout.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (!event.isDoubleClick()) {
                    setQuickReplyVisible(!quickReply.isVisible());
                }
            }
        });
        showOrHideLayout.setWidth(100.0f, Unit.PERCENTAGE);

        final FloatingBar bar = new FloatingBar();
        bar.addStyleName("quickReply");
        bar.addStyleName(UI.getCurrent().getTheme());
        bar.setAlignment(FloatingAlignment.BOTTOM);
        bar.setScrollComponent(mirroredAuthoringComponent);

        quickReplyLayout.addComponent(quickReply);
        quickReplyLayout.addComponent(showOrHideLayout);
        bar.setContent(quickReplyLayout);
        return bar;
    }

    protected void setQuickReplyVisible(boolean visible) {
        quickReply.setVisible(visible);
        showOrHideLabel.setValue((visible ? "Hide" : "Show") + " quick reply");
        if (visible) {
            quickReplyLayout.removeStyleName(COLLAPSED);
        } else {
            quickReplyLayout.addStyleName(COLLAPSED);
        }
    }

    @Override
    public void displayThreadNotFoundError(final String threadIdString) {
        layout.removeAllComponents();
        layout.addComponent(new HeadingLabel(
                "This thread does not exist. Maybe someone deleted it?",
                HeadingLevel.H1));
    }

    @Override
    protected void navigationTo(final String[] arguments) {
        try {
            super.getPresenter().handleArguments(arguments);
        } catch (final NoSuchThreadException e) {
            displayThreadNotFoundError(String.valueOf(e.getThreadId()));
        } catch (final DataSourceException e) {
            panic();
        }
    }

    @Override
    public void confirmPostReported() {
        Notification.show("Post is reported!");
    }

    @Override
    public void confirmBanned(final User user) {
        for (final Entry<Post, PostComponent> entry : postsToComponents
                .entrySet()) {
            final Post post = entry.getKey();
            if (post.getAuthor().equals(user)) {
                final PostComponent postComponent = entry.getValue();
                postComponent.setUserIsBanned();
                postComponent.swapBannedMenu();
            }
        }
    }

    @Override
    public void confirmUnbanned(final User user) {
        for (final Entry<Post, PostComponent> entry : postsToComponents
                .entrySet()) {
            final Post post = entry.getKey();
            if (post.getAuthor().equals(user)) {
                final PostComponent postComponent = entry.getValue();
                postComponent.setUserIsUnbanned();
                postComponent.swapBannedMenu();
            }
        }
    }

    @Override
    public void confirmFollowingThread() {
        swapFollowingMenus();
    }

    @Override
    public void confirmUnFollowingThread() {
        swapFollowingMenus();
    }

    private void swapFollowingMenus() {
        for (final PostComponent c : postsToComponents.values()) {
            c.swapFollowingMenu();
        }
    }

    @Override
    public void confirmPostDeleted() {
        Notification.show("Post deleted");
        reloadPage();
    }

    /**
     * Resets and redraws the view.
     * 
     * @deprecated Now with the {@link LazyLayout} this <em>really really</em>
     *             should be avoided. Use incremental changes instead whenever
     *             humanly possible, please!
     */
    @Deprecated
    private void reloadPage() {
        try {
            getPresenter().resetView();
        } catch (final DataSourceException e) {
            panic();
        }
    }

    @Override
    public void refreshScores(final Post post, final long newScore) {
        postsToComponents.get(post).refreshScores(newScore);
    }

    @Override
    public void confirmReplyPostedAndShowIt(final Post newPost) {
        postsLayout.addComponent(newPostComponent(newPost));
    }

    @Override
    public void displayUserCanNotReply() {
        Notification.show("Unfortunately, you are not "
                + "allowed to reply to this thread.");
    }

    @Override
    public void displayUserCanNotEdit() {
        Notification.show("Unfortunately, you are not "
                + "allowed to edit this post.");
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
    public void displayNewThreadFormFor(final Category category) {
        layout.removeAllComponents();
        final HeadingLabel heading = new HeadingLabel("Start a New Thread",
                HeadingLevel.H2);
        layout.addComponent(heading);
        getUI().scrollIntoView(heading);

        final HorizontalLayout topicLayout = new HorizontalLayout();
        topicLayout.setWidth(70.0f, Unit.PERCENTAGE);
        topicLayout.setSpacing(true);
        topicLayout.setMargin(new MarginInfo(true, false, false, false));
        topicLayout.setStyleName("newthread");
        layout.addComponent(topicLayout);

        final HeadingLabel topicLabel = new HeadingLabel("Topic",
                HeadingLevel.H3);
        topicLabel.addStyleName("topiclabel");
        topicLabel.setWidth("153px");
        topicLayout.addComponent(topicLabel);

        final TextField topicField = new TextField();
        topicField.setStyleName("topicfield");
        topicField.setWidth("100%");
        topicLayout.addComponent(topicField);
        topicLayout.setExpandRatio(topicField, 1.0f);
        topicField.focus();

        AuthoringListener listener = new AuthoringListener() {

            @Override
            public void submit(final String rawBody) {
                String errorMessages = "";

                final String topic = topicField.getValue();
                if (topic.isEmpty()) {
                    errorMessages += "You need a topic<br/>";
                }
                if (rawBody.isEmpty()) {
                    errorMessages += "You need a thread body<br/>";
                }

                if (errorMessages.isEmpty()) {
                    try {
                        final DiscussionThread createdThread = getPresenter()
                                .createNewThread(category, topic, rawBody);
                        ToriUI.getCurrent().trackAction(null, "new-thread");
                        UI.getCurrent()
                                .getNavigator()
                                .navigateTo(
                                        ToriNavigator.ApplicationView.THREADS
                                                .getNavigatorUrl()
                                                + "/"
                                                + createdThread.getId());
                    } catch (final DataSourceException e) {
                        Notification
                                .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                    }
                } else {
                    final Notification n = new Notification(errorMessages,
                            Type.HUMANIZED_MESSAGE);
                    n.show(getUI().getPage());
                }
            }

            @Override
            public void addAttachment(final String attachmentFileName,
                    final byte[] data) {
                getPresenter().addAttachment(attachmentFileName, data);
            }

            @Override
            public void resetInput() {
                getPresenter().resetInput();
            }

            @Override
            public void removeAttachment(final String fileName) {
                getPresenter().removeAttachment(fileName);
            }
        };

        newThreadComponent = new AuthoringComponent(listener, "Post body", true);
        newThreadComponent.setUserMayAddFiles(getPresenter().userMayAddFiles());
        newThreadComponent.setMaxFileSize(getPresenter().getMaxFileSize());
        layout.addComponent(newThreadComponent);
    }

    @Override
    public void panic() {
        layout.removeAllComponents();
        layout.addComponent(new PanicComponent());
    }

    @Override
    public void appendToReply(final String textToAppend) {
        reply.insertIntoMessage(textToAppend);
        setQuickReplyVisible(true);
    }

    @Override
    public void refresh(final Post post) {
        final PostComponent postComponent = postsToComponents.get(post);
        postComponent.refreshBody(post);
    }

    @Override
    public void updateAttachmentList(
            final LinkedHashMap<String, byte[]> attachments) {
        if (reply != null) {
            reply.updateAttachmentList(attachments);
        }
        if (newThreadComponent != null) {
            newThreadComponent.updateAttachmentList(attachments);
        }
    }

    @Override
    public String getTitle() {
        return getPresenter().getThreadTopic();
    }

    @Override
    public void otherUserAuthored(final Post post) {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                postsLayout.addComponent(newPostComponent(post));
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
}
