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

import java.util.List;

import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriScheduler;
import org.vaadin.tori.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.component.ComponentUtil.HeadingLevel;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.util.ToriActivityMessaging.UserAuthoredListener;
import org.vaadin.tori.view.listing.SpecialCategory;

import com.ocpsoft.pretty.time.PrettyTime;
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

    private final HorizontalLayout layout = new HorizontalLayout();
    private PostNotification previous;
    private PostNotification current;

    private final DataSource dataSource = ToriApiLoader.getCurrent()
            .getDataSource();
    private final CssLayout notificationsLayout;

    public RecentBar() {
        ToriApiLoader.getCurrent().getToriActivityMessaging()
                .addUserAuthoredListener(this);
        addStyleName("recentbar");
        setWidth(100.0f, Unit.PERCENTAGE);
        setHeight(27.0f, Unit.PIXELS);

        setCompositionRoot(layout);
        layout.setMargin(new MarginInfo(false, true, false, true));
        layout.setSizeFull();
        Label headingLabel = ComponentUtil.getHeadingLabel("MOST RECENT",
                HeadingLevel.H4);
        headingLabel.setWidth(110.0f, Unit.PIXELS);
        layout.addComponent(headingLabel);
        layout.setComponentAlignment(headingLabel, Alignment.MIDDLE_LEFT);
        notificationsLayout = new CssLayout();
        notificationsLayout.setWidth(100.0f, Unit.PERCENTAGE);
        notificationsLayout.setHeight(18.0f, Unit.PIXELS);
        notificationsLayout.addStyleName("notificationslayout");
        layout.addComponent(notificationsLayout);
        layout.setExpandRatio(notificationsLayout, 1.0f);
        layout.setComponentAlignment(notificationsLayout, Alignment.MIDDLE_LEFT);

        Link link = new Link("Recent Posts", new ExternalResource("#"
                + ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                + SpecialCategory.RECENT_POSTS.getId().toLowerCase()));
        link.addStyleName("recentlink");
        layout.addComponent(link);
        layout.setComponentAlignment(link, Alignment.MIDDLE_RIGHT);

        try {
            List<DiscussionThread> recentPosts = dataSource
                    .getRecentPosts(0, 1);
            if (!recentPosts.isEmpty()) {
                current = new PostNotification(recentPosts.get(0)
                        .getLatestPost());
                current.setState(PostNotificationState.CURRENT);
                notificationsLayout.addComponent(current);
            }
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userAuthored(final long postId, long threadId) {
        try {
            getUI().access(new Runnable() {
                @Override
                public void run() {
                    try {
                        newPostAdded(dataSource.getPost(postId));
                    } catch (DataSourceException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (UIDetachedException e) {
            // Ignore
        }
    }

    private void newPostAdded(Post post) {
        if (previous != null) {
            notificationsLayout.removeComponent(previous);
        }
        previous = current;
        current = new PostNotification(post);
        notificationsLayout.addComponent(current);
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

        public PostNotification(Post post) {
            setSpacing(true);
            addStyleName("postnotification");
            Link link = new Link(post.getThread().getTopic(),
                    new ExternalResource(getPermaLinkUrl(post)));
            StringBuilder infoText = new StringBuilder("by ");
            infoText.append(post.getAuthor().getDisplayedName()).append(", ")
                    .append(prettyTime.format(post.getTime()));
            Label info = new Label(infoText.toString());
            addComponents(link, info);
            setState(PostNotificationState.NEXT);
        }

        public void setState(PostNotificationState state) {
            for (PostNotificationState ps : PostNotificationState.values()) {
                removeStyleName(ps.name().toLowerCase());
            }
            addStyleName(state.name().toLowerCase());
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
    }

}
