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
import java.util.Iterator;
import java.util.List;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriScheduler;
import org.vaadin.tori.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.component.ConfirmationDialog;
import org.vaadin.tori.component.ConfirmationDialog.ConfirmationListener;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.MenuPopup.ContextAction;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.component.post.EditComponent.EditListener;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.view.thread.ThreadPresenter;
import org.vaadin.tori.view.thread.ThreadView.PostData;
import org.vaadin.tori.widgetset.client.ui.post.PostComponentRpc;
import org.vaadin.tori.widgetset.client.ui.post.PostComponentState;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;

@SuppressWarnings("serial")
public class PostComponent extends AbstractComponentContainer implements
        PostComponentRpc {

    private static final String DELETE_CAPTION = "Delete Post";
    private static final String DELETE_ICON = "icon-delete";

    private static final String BAN_CAPTION = "Ban Author";
    private static final String BAN_ICON = "icon-ban";

    private static final String UNBAN_CAPTION = "Unban Author";
    private static final String UNBAN_ICON = "icon-unban";

    private static final String STYLE_BANNED = "banned-author";

    private final PrettyTime prettyTime = new PrettyTime();
    private final DateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yyyy kk:mm");

    private final PostData post;

    private ReportComponent reportComponent;
    private final ContextMenu contextMenu = new ContextMenu();
    private final ThreadPresenter presenter;

    private EditComponent editComponent;

    private boolean banEnabled;
    private boolean unbanEnabled;

    @Override
    protected PostComponentState getState() {
        return (PostComponentState) super.getState();
    }

    // TODO: REMOVE
    private static class Util {
        private static Component newConfirmBanComponent(
                final ThreadPresenter presenter, final PostData post,
                final ContextMenu menu) {
            final String title = String.format("Ban %s?", post.getAuthorName());
            final String confirmCaption = "Yes, Ban";
            final String cancelCaption = "No, Cancel!";
            final ConfirmationListener listener = new ConfirmationListener() {

                @Override
                public void onConfirmed() throws DataSourceException {
                    presenter.ban(post.getAuthorId());
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
                final ThreadPresenter presenter, final PostData post,
                final ContextMenu menu) {
            final String title = String.format("Delete Post?");
            final String confirmCaption = "Yes, Delete";
            final String cancelCaption = "No, Cancel!";
            final ConfirmationListener listener = new ConfirmationListener() {

                @Override
                public void onConfirmed() throws DataSourceException {
                    presenter.delete(post.getId());
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

    /**
     * @throws IllegalArgumentException
     *             if any argument is <code>null</code>.
     */
    public PostComponent(final PostData post, final ThreadPresenter presenter,
            final boolean allowHtml) {
        this.presenter = presenter;
        this.post = post;

        registerRpc(this);
        setStyleName("post");

        final PostComponentState state = getState();
        state.setAuthorName(post.getAuthorName());
        state.setAllowHTML(allowHtml);
        state.setPrettyTime(prettyTime.format(post.getTime()));
        state.setTimeStamp(dateFormat.format(post.getTime()));
        state.setPermaLink(getPermaLinkUrl(post));
        state.setPostBody(post.getFormattedBody(allowHtml));
        state.setAttachments(post.getAttachments());
        setAvatarImageResource(post);
        if (post.isAuthorBanned()) {
            setUserIsBanned();
        }

        ToriScheduler.get().scheduleManual(new ScheduledCommand() {
            @Override
            public void execute() {
                refreshScores();
                state.setBadgeHTML(post.getBadgeHTML());
                state.setReportingEnabled(post.userMayReportPosts());
                state.setEditingEnabled(post.userMayEdit());
                state.setQuotingEnabled(post.userMayQuote());
                state.setVotingEnabled(post.userMayVote());

                // context menu permissions

                if (post.userMayBanAuthor()) {
                    if (!post.isAuthorBanned()) {
                        enableBanning();
                    } else {
                        enableUnbanning();
                    }
                }

                if (post.userMayDelete()) {
                    enableDeleting();
                }
            }
        });

    }

    private final EditListener editListener = new EditListener() {
        @Override
        public void postEdited(final String newPostBody) {
            presenter.saveEdited(post.getId(), newPostBody);
            ToriUI.getCurrent().trackAction("edit-post");
        }
    };

    private final ContextComponentSwapper banActionSwapper = new ContextComponentSwapper() {
        @Override
        public Component swapContextComponent() {
            return Util.newConfirmBanComponent(presenter, post, contextMenu);
        }
    };

    private final ContextAction unbanAction = new ContextAction() {
        @Override
        public void contextClicked() {
            try {
                presenter.unban(post.getAuthorId());
            } catch (final DataSourceException e) {
                Notification.show(DataSourceException.GENERIC_ERROR_MESSAGE);
            }
        }
    };

    private void refreshScores() {
        getState().setScore(post.getScore());
        getState().setUpVoted(post.getUpVoted());
    }

    private void enableBanning() {
        contextMenu.add(BAN_ICON, BAN_CAPTION, banActionSwapper);
        getState().setSettingsEnabled(true);
    }

    private void enableUnbanning() {
        contextMenu.add(UNBAN_ICON, UNBAN_CAPTION, unbanAction);
        getState().setSettingsEnabled(true);
    }

    private void enableDeleting() {
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

    public void setUserIsBanned() {
        addStyleName(STYLE_BANNED);
        setDescription(post.getAuthorName() + " is banned.");
    }

    public void setUserIsUnbanned() {
        removeStyleName(STYLE_BANNED);
        setDescription(null);
    }

    private static String getPermaLinkUrl(final PostData post) {
        // @formatter:off
        final String linkUrl = String.format(
                "#%s/%s/%s",
                ToriNavigator.ApplicationView.THREADS.getUrl(), 
                post.getThreadId(),
                post.getId()
                );
        // @formatter:on

        return linkUrl;
    }

    private void setAvatarImageResource(final PostData post) {
        String avatarUrl = post.getAuthorAvatarUrl();

        final Resource imageResource;
        if (avatarUrl != null) {
            imageResource = new ExternalResource(avatarUrl);
        } else {
            imageResource = new ThemeResource(
                    "images/icon-placeholder-avatar.gif");
        }
        setResource("avatar", imageResource);
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
                    presenter.upvote(post.getId());
                } else {
                    presenter.downvote(post.getId());
                }
                refreshScores();
            } catch (final DataSourceException e) {
                Notification.show(DataSourceException.GENERIC_ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void quoteForReply() {
        presenter.quotePost(post.getId());
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
                editComponent = new EditComponent(post.getRawBody(),
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
