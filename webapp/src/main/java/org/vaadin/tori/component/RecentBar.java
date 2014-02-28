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

package org.vaadin.tori.component;

import org.ocpsoft.prettytime.PrettyTime;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUI;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.util.ComponentUtil;
import org.vaadin.tori.util.ComponentUtil.HeadingLevel;
import org.vaadin.tori.util.ToriActivityMessaging.UserAuthoredListener;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.listing.SpecialCategory;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.UIDetachedException;

@SuppressWarnings("serial")
public class RecentBar extends CustomComponent implements UserAuthoredListener {

    private PostNotification previous;
    private PostNotification current;
    private final FloatingNotification floatingNotification = new FloatingNotification();
    private final FloatingComponent floatingComponent = new FloatingComponent();

    private final DataSource dataSource = ToriApiLoader.getCurrent()
            .getDataSource();
    private CssLayout notificationsLayout;

    public RecentBar() {
        ToriApiLoader.getCurrent().getToriActivityMessaging()
                .addUserAuthoredListener(this);
        addStyleName("recentbar");
        setWidth(100.0f, Unit.PERCENTAGE);
        setHeight(35.0f, Unit.PIXELS);

        CssLayout layout = new CssLayout();
        layout.setSizeFull();
        setCompositionRoot(layout);

        HorizontalLayout barLayout = new HorizontalLayout();
        barLayout.setMargin(new MarginInfo(false, true, false, true));
        barLayout.setSizeFull();
        addTitleLabel(barLayout);
        addNotificationsLayout(barLayout);
        addRecentLink(barLayout);
        layout.addComponent(barLayout);

        floatingComponent.extend(floatingNotification);
        layout.addComponent(floatingNotification);
        floatingNotification.setId("floatingnotification");

        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                refresh();
            }
        });
    }

    private void addRecentLink(final HorizontalLayout barLayout) {
        Link link = new Link("Recent Posts", new ExternalResource("#"
                + ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                + SpecialCategory.RECENT_POSTS.getId().toLowerCase()));
        link.addStyleName("recentlink");
        barLayout.addComponent(link);
        barLayout.setComponentAlignment(link, Alignment.MIDDLE_RIGHT);
    }

    private void addNotificationsLayout(final HorizontalLayout barLayout) {
        notificationsLayout = new CssLayout();
        notificationsLayout.setSizeFull();
        notificationsLayout.addStyleName("notificationslayout");
        barLayout.addComponent(notificationsLayout);
        barLayout.setExpandRatio(notificationsLayout, 1.0f);
        barLayout.setComponentAlignment(notificationsLayout,
                Alignment.MIDDLE_LEFT);
    }

    private void addTitleLabel(final HorizontalLayout barLayout) {
        Label headingLabel = ComponentUtil.getHeadingLabel("MOST RECENT",
                HeadingLevel.H4);
        headingLabel.setWidth(110.0f, Unit.PIXELS);
        barLayout.addComponent(headingLabel);
        barLayout.setComponentAlignment(headingLabel, Alignment.MIDDLE_LEFT);
    }

    @Override
    public void userAuthored(final long postId, final long threadId) {
        try {
            getUI().access(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        } catch (UIDetachedException e) {
            // Ignore
        }
    }

    private void newPostAdded(final Post post) {
        if (previous != null) {
            notificationsLayout.removeComponent(previous);
        }
        previous = current;
        current = new PostNotification(post);
        notificationsLayout.addComponent(current);
        floatingNotification.setLink(post.getThread().getTopic(),
                PostNotification.getPermaLinkUrl(post));
        floatingComponent.flash();

        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                previous.setState(PostNotificationState.PREVIOUS);
                current.setState(PostNotificationState.CURRENT);
            }
        });
    }

    private enum PostNotificationState {
        PREVIOUS, CURRENT, NEXT
    }

    public static class PostNotification extends HorizontalLayout {
        private final PrettyTime prettyTime = new PrettyTime();
        private final Post post;

        public PostNotification(final Post post) {
            this.post = post;
            setSpacing(true);
            Link link = new Link(post.getThread().getTopic(),
                    new ExternalResource(getPermaLinkUrl(post)));
            StringBuilder infoText = new StringBuilder("by ");
            infoText.append(post.getAuthor().getDisplayedName()).append(", ")
                    .append(prettyTime.format(post.getTime()));
            Label info = new Label(infoText.toString());
            addComponents(link, info);
            setState(PostNotificationState.NEXT);
        }

        public void setState(final PostNotificationState state) {
            setStyleName("postnotification " + state.name().toLowerCase());
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

        public Post getPost() {
            return post;
        }
    }

    public static class FloatingNotification extends CssLayout {

        private final Link link;

        public FloatingNotification() {
            addStyleName("floatingnotification");
            Label label = new Label("New post in");
            label.setSizeUndefined();
            addComponent(label);

            link = new Link();
            addComponent(link);
        }

        public void setLink(final String caption, final String url) {
            link.setCaption(caption);
            link.setResource(new ExternalResource(url));
        }
    }

    public static RecentBar getCurrent() {
        return ToriUI.getCurrent().getRecentBar();
    }

    public void refresh() {
        try {
            DiscussionThread recentThread = null;
            int count = dataSource.getThreadCountRecursively(null);

            for (int i = 0; i < count; i++) {
                DiscussionThread thread = dataSource.getRecentPosts(i, i)
                        .get(0);
                boolean newerThread = recentThread == null
                        || thread.getLatestPost().getTime()
                                .after(recentThread.getLatestPost().getTime());
                recentThread = newerThread ? thread : recentThread;
                if (!thread.isSticky()) {
                    break;
                }
            }

            if (recentThread != null) {
                Post latestPost = recentThread.getLatestPost();
                if (latestPost != null) {
                    latestPost.setThread(recentThread);
                    if (current == null) {
                        current = new PostNotification(latestPost);
                        current.setState(PostNotificationState.CURRENT);
                        notificationsLayout.addComponent(current);
                    } else if (latestPost != null
                            && current.getPost().getId() != latestPost.getId()) {
                        newPostAdded(latestPost);
                    }
                }
            }
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
    }
}
