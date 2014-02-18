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

package org.vaadin.tori.view.thread;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.tori.ToriScheduler;
import org.vaadin.tori.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.data.entity.User;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
class ThreadUpdatesComponent extends CustomComponent {

    private final Label newPostsLabel = new Label();
    private final Label showAllLabel = new Label();
    private final Label pendingRepliesLabel = new Label();
    private Map<User, Date> pendingReplies = new HashMap<User, Date>();
    private int newPostsCount = 0;

    public ThreadUpdatesComponent(final ThreadPresenter presenter) {
        setStyleName("threadupdates");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.addStyleName("threadupdateslayout");
        setCompositionRoot(layout);

        setWidth(100.0f, Unit.PERCENTAGE);
        layout.setMargin(true);
        layout.setSpacing(true);
        setVisible(false);

        newPostsLabel.setSizeUndefined();
        newPostsLabel.addStyleName("newposts");
        layout.addComponent(newPostsLabel);

        showAllLabel.addStyleName("showall");
        layout.addComponent(showAllLabel);
        layout.setExpandRatio(showAllLabel, 1.0f);

        pendingRepliesLabel.setWidth(150.0f, Unit.PIXELS);
        layout.addComponent(pendingRepliesLabel);
        layout.setComponentAlignment(pendingRepliesLabel,
                Alignment.MIDDLE_RIGHT);

        layout.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getChildComponent() == showAllLabel) {
                    presenter.showNewPostsRequested();
                }
            }
        });
    }

    public void setNewPostsCount(int count) {
        newPostsCount = count;
        newPostsLabel.setVisible(count > 0);
        showAllLabel.setVisible(count > 0);
        newPostsLabel.setValue(count + " new " + (count > 1 ? "posts" : "post")
                + " –");
        showAllLabel.setValue("Show" + (count > 1 ? " all" : ""));

        updateVisibility();
    }

    public void setPendingReplies(Map<User, Date> pendingReplies) {
        this.pendingReplies = pendingReplies;

        pendingRepliesLabel.setVisible(!pendingReplies.isEmpty());
        pendingRepliesLabel.setValue(pendingReplies.size() + " pending "
                + (pendingReplies.size() > 1 ? "replies" : "reply") + "...");
        pendingRepliesLabel.setDescription("test");
        StringBuilder description = new StringBuilder();

        for (Entry<User, Date> entry : pendingReplies.entrySet()) {
            double millis = new Date().getTime() - entry.getValue().getTime();
            double minutes = millis / 60000;
            int minutesInt = (int) Math.floor(minutes);
            String pretty = "just now";
            if (minutesInt > 0) {
                pretty = minutesInt + (minutesInt > 1 ? " minutes" : " minute")
                        + " ago";
            }
            description.append("<div><a href='" + entry.getKey().getUserLink()
                    + "'>");
            description.append(entry.getKey().getDisplayedName());
            description.append("</a><span> started writing a reply ");
            description.append(pretty);
            description.append("</span></div>");
        }
        pendingRepliesLabel.setDescription(description.toString());

        updateVisibility();
    }

    public void updateVisibility() {
        setVisible(!pendingReplies.isEmpty() || newPostsCount > 0);
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                if (isVisible()) {
                    addStyleName("visible");
                } else {
                    removeStyleName("visible");
                }
            }
        });
    }

}
