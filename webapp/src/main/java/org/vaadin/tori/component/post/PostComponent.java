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

package org.vaadin.tori.component.post;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.component.ConfirmationDialog;
import org.vaadin.tori.component.ConfirmationDialog.ConfirmationListener;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.MenuPopup.ContextAction;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.component.post.EditComponent.EditListener;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.thread.ThreadPresenter;
import org.vaadin.tori.util.UserBadgeProvider;
import org.vaadin.tori.widgetset.client.ui.post.PostComponentRpc;
import org.vaadin.tori.widgetset.client.ui.post.PostComponentState;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;

import edu.umd.cs.findbugs.annotations.CheckForNull;

@SuppressWarnings("serial")
public class PostComponent extends AbstractComponentContainer implements
        PostComponentRpc {

    private static final String DELETE_CAPTION = "Delete Post";
    private static final String DELETE_ICON = "icon-delete";

    private static final String BAN_CAPTION = "Ban Author";
    private static final String BAN_ICON = "icon-ban";

    private static final String UNFOLLOW_CAPTION = "Unfollow Thread";
    private static final String UNFOLLOW_ICON = "icon-unfollow";

    private static final String FOLLOW_CAPTION = "Follow Thread";
    private static final String FOLLOW_ICON = "icon-follow";

    private static final String UNBAN_CAPTION = "Unban Author";
    private static final String UNBAN_ICON = "icon-unban";

    private static final String STYLE_BANNED = "banned-author";

    private final PrettyTime prettyTime = new PrettyTime();
    private final DateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yyyy kk:mm");

    private final Post post;

    private ReportComponent reportComponent;
    private final ContextMenu contextMenu = new ContextMenu();
    private final ThreadPresenter presenter;

    private boolean followingEnabled = false;
    private boolean unfollowingEnabled = false;
    private EditComponent editComponent;
    private final boolean allowHtml;
    private boolean banEnabled;
    private boolean unbanEnabled;

    @Override
    protected PostComponentState getState() {
        return (PostComponentState) super.getState();
    }

    // trying a new pattern here by grouping auxiliary methods in an inner class
    private static class Util {
        private static Component newConfirmBanComponent(
                final ThreadPresenter presenter, final User user,
                final ContextMenu menu) {
            final String title = String.format("Ban %s?",
                    user.getDisplayedName());
            final String confirmCaption = "Yes, Ban";
            final String cancelCaption = "No, Cancel!";
            final ConfirmationListener listener = new ConfirmationListener() {

                @Override
                public void onConfirmed() throws DataSourceException {
                    presenter.ban(user);
                    menu.close();
                }

                @Override
                public void onCancel() {
                    menu.close();
                }
            };
            return new ConfirmationDialog(title, confirmCaption, cancelCaption,
                    listener);
        }

        public static Component newConfirmDeleteComponent(
                final ThreadPresenter presenter, final Post post,
                final ContextMenu menu) {
            final String title = String.format("Delete Post?");
            final String confirmCaption = "Yes, Delete";
            final String cancelCaption = "No, Cancel!";
            final ConfirmationListener listener = new ConfirmationListener() {

                @Override
                public void onConfirmed() throws DataSourceException {
                    presenter.delete(post);
                    menu.close();
                }

                @Override
                public void onCancel() {
                    menu.close();
                }
            };
            return new ConfirmationDialog(title, confirmCaption, cancelCaption,
                    listener);
        }
    }

    private final EditListener editListener = new EditListener() {
        @Override
        public void postEdited(final String newPostBody) {
            try {
                presenter.saveEdited(post, newPostBody);
                ToriUI.getCurrent().trackAction(null, "edit-post");
                // this component will be replaced with a new one. So no need to
                // change the state.
            } catch (final DataSourceException e) {
                final Notification n = new Notification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE,
                        Notification.Type.ERROR_MESSAGE);
                n.show(getUI().getPage());
            }
        }
    };

    private final ContextAction followAction = new ContextAction() {
        @Override
        public void contextClicked() {
            try {
                presenter.followThread();
            } catch (final DataSourceException e) {
                Notification
                        .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }
        }
    };

    private final ContextAction unfollowAction = new ContextAction() {
        @Override
        public void contextClicked() {
            try {
                presenter.unFollowThread();
            } catch (final DataSourceException e) {
                Notification
                        .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }

        }
    };

    private final ContextComponentSwapper banActionSwapper = new ContextComponentSwapper() {
        @Override
        public Component swapContextComponent() {
            return Util.newConfirmBanComponent(presenter, post.getAuthor(),
                    contextMenu);
        }
    };

    private final ContextAction unbanAction = new ContextAction() {
        @Override
        public void contextClicked() {
            try {
                presenter.unban(post.getAuthor());
            } catch (final DataSourceException e) {
                Notification
                        .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }
        }
    };

    /**
     * @throws IllegalArgumentException
     *             if any argument is <code>null</code>.
     */
    public PostComponent(final Post post, final ThreadPresenter presenter,
            final boolean allowHtml) {
        registerRpc(this);

        ToriUtil.checkForNull(post, "post may not be null");
        ToriUtil.checkForNull(presenter, "presenter may not be null");

        this.presenter = presenter;
        this.post = post;
        this.allowHtml = allowHtml;

        setStyleName("post");

        getState().setAuthorName(post.getAuthor().getDisplayedName());
        getState().setAllowHTML(allowHtml);
        getState().setPrettyTime(prettyTime.format(post.getTime()));
        getState().setTimeStamp(dateFormat.format(post.getTime()));
        getState().setPermaLink(getPermaLinkUrl(post));
        setAvatarImageResource(post);

        try {
            refreshScores(presenter.getScore(post));
        } catch (final DataSourceException e) {
            // NOP - logged, just showing a score of OVER 9000!!!!
            refreshScores(9001);
        }

        addBadgePossibly(post.getAuthor());

        final String rawSignature = post.getAuthor().getSignatureRaw();
        if (rawSignature != null && !rawSignature.isEmpty()) {
            final String formattedSignature = ToriApiLoader.getCurrent()
                    .getSignatureFormatter().format(rawSignature);
            getState().setSignature(formattedSignature);
        }

        refreshBody(post);

        getState().setAttachments(getAttachments(post));
    }

    /**
     * Adds a badge to the customlayout if there is a {@link UserBadgeProvider}
     * set up.
     */
    private void addBadgePossibly(final User user) {
        final UserBadgeProvider badgeProvider = ToriApiLoader.getCurrent()
                .getUserBadgeProvider();
        if (badgeProvider == null) {
            return;
        }

        final String badgeHtml = badgeProvider.getHtmlBadgeFor(user);
        getState().setBadgeHTML(badgeHtml);
    }

    public void enableReporting() {
        getState().setReportingEnabled(true);
    }

    public void enableEditing() {
        getState().setEditingEnabled(true);
    }

    public void enableQuoting() {
        getState().setQuotingEnabled(true);
    }

    public void enableThreadFollowing() {
        contextMenu.add(FOLLOW_ICON, FOLLOW_CAPTION, followAction);
        followingEnabled = true;
        unfollowingEnabled = false;
        getState().setSettingsEnabled(true);
    }

    public void enableThreadUnFollowing() {
        contextMenu.add(UNFOLLOW_ICON, UNFOLLOW_CAPTION, unfollowAction);
        followingEnabled = false;
        unfollowingEnabled = true;
        getState().setSettingsEnabled(true);
    }

    public void enableBanning() {
        contextMenu.add(BAN_ICON, BAN_CAPTION, banActionSwapper);
        banEnabled = true;
        unbanEnabled = false;
        getState().setSettingsEnabled(true);
    }

    public void enableUnbanning() {
        contextMenu.add(UNBAN_ICON, UNBAN_CAPTION, unbanAction);
        banEnabled = false;
        unbanEnabled = true;
        getState().setSettingsEnabled(true);
    }

    public void enableDeleting() {
        contextMenu.add(DELETE_ICON, DELETE_CAPTION,
                new ContextComponentSwapper() {
                    @Override
                    public Component swapContextComponent() {
                        return Util.newConfirmDeleteComponent(presenter, post,
                                contextMenu);
                    }
                });
        getState().setSettingsEnabled(true);
    }

    public void enableUpDownVoting(final PostVote postVote) {
        getState().setVotingEnabled(true);
    }

    public void setUserIsBanned() {
        addStyleName(STYLE_BANNED);
        setDescription(post.getAuthor().getDisplayedName() + " is banned.");
    }

    public void setUserIsUnbanned() {
        removeStyleName(STYLE_BANNED);
        setDescription(null);
    }

    @CheckForNull
    private Map<String, String> getAttachments(final Post post) {
        Map<String, String> attachments = new HashMap<String, String>();
        if (post.hasAttachments()) {
            // create a Link for each attachment
            for (final Attachment attachment : post.getAttachments()) {
                final String linkCaption = String.format("%s (%s KB)",
                        attachment.getFilename(),
                        attachment.getFileSize() / 1024);
                attachments.put(attachment.getDownloadUrl(), linkCaption);
            }
        }
        return attachments;
    }

    private static String getPermaLinkUrl(final Post post) {
        // @formatter:off
        final String linkUrl = String.format(
                "#%s/%s/%s",
                ToriNavigator.ApplicationView.THREADS.getUrl(), 
                post.getThread().getId(),
                post.getId()
                );
        // @formatter:on

        return linkUrl;
    }

    private void setAvatarImageResource(final Post post) {
        String avatarUrl = post.getAuthor().getAvatarUrl();

        final Resource imageResource;
        if (avatarUrl != null) {
            imageResource = new ExternalResource(avatarUrl);
        } else {
            imageResource = new ThemeResource(
                    "images/icon-placeholder-avatar.gif");
        }
        setResource("avatar", imageResource);
    }

    public void refreshScores(final long newScore) {
        getState().setScore(newScore);
        try {
            PostVote postVote = presenter.getPostVote(post);
            Boolean upVoted = null;
            if (postVote.isUpvote()) {
                upVoted = true;
            } else if (postVote.isDownvote()) {
                upVoted = false;
            }
            getState().setUpVoted(upVoted);

        } catch (final DataSourceException e) {
            Notification.show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
        }
    }

    public void swapFollowingMenu() {
        if (followingEnabled || unfollowingEnabled) {

            if (followingEnabled) {
                contextMenu.swap(followAction, UNFOLLOW_ICON, UNFOLLOW_CAPTION,
                        unfollowAction);
            } else {
                contextMenu.swap(unfollowAction, FOLLOW_ICON, FOLLOW_CAPTION,
                        followAction);
            }

            followingEnabled = !followingEnabled;
            unfollowingEnabled = !unfollowingEnabled;
        }
    }

    public void swapBannedMenu() {
        if (banEnabled || unbanEnabled) {
            if (banEnabled) {
                contextMenu.swap(banActionSwapper, UNBAN_ICON, UNBAN_CAPTION,
                        unbanAction);
            } else {
                contextMenu.swap(unbanAction, BAN_ICON, BAN_CAPTION,
                        banActionSwapper);
            }

            banEnabled = !banEnabled;
            unbanEnabled = !unbanEnabled;
        }
    }

    public final void refreshBody(final Post post) {
        String formattedPost = ToriApiLoader.getCurrent().getPostFormatter()
                .format(post.getBodyRaw());
        if (!allowHtml) {
            formattedPost = presenter.stripTags(formattedPost);
        }
        getState().setPostBody(formattedPost);
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {

    }

    @Override
    public int getComponentCount() {
        int count = 0;
        Iterator<Component> i = iterator();
        while (i.hasNext()) {
            i.next();
            count++;
        }
        return count;
    }

    @Override
    public Iterator<Component> iterator() {
        List<Component> components = new ArrayList<Component>(Arrays.asList(
                editComponent, contextMenu, reportComponent));
        components.removeAll(Collections.singleton(null));
        if (contextMenu.getParent() == null) {
            components.remove(contextMenu);
        }
        return components.iterator();
    }

    @Override
    public void postVoted(boolean up) {
        if (getState().isVotingEnabled()) {
            try {
                if (up) {
                    presenter.upvote(post);
                } else {
                    presenter.downvote(post);
                }
            } catch (final DataSourceException e) {
                Notification
                        .show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void quoteForReply() {
        presenter.quotePost(post);
    }

    @Override
    public void settingsClicked() {
        if (contextMenu.getParent() == null) {
            addComponent(contextMenu);
            contextMenu.setSettingsIconVisible(false);
            getState().setSettings(contextMenu);
        }
        contextMenu.open();
    }

    @Override
    public void editClicked() {
        if (getState().isEditingEnabled()) {
            if (editComponent == null) {
                editComponent = new EditComponent(post.getBodyRaw(),
                        editListener);
                addComponent(editComponent);
                getState().setEdit(editComponent);
            }
            editComponent.open();
        }
    }

    @Override
    public void reportClicked() {
        if (getState().isReportingEnabled()) {
            if (reportComponent == null) {
                reportComponent = new ReportComponent(post, presenter,
                        getPermaLinkUrl(post));
                addComponent(reportComponent);
                getState().setReport(reportComponent);
            }
            reportComponent.open();
        }
    }

}
