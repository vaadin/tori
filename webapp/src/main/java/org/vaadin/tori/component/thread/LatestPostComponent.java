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

package org.vaadin.tori.component.thread;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class LatestPostComponent extends Label {

    public LatestPostComponent(final DiscussionThread thread) {
        super("", ContentMode.HTML);
        setWidth(null);
        setStyleName("latest-post");

        final Post latestPost = thread.getLatestPost();

        final String whenPostedXhtml = getWhenPostedXhtml(latestPost);
        final String byWhomXhtml = getByWhomXhtml(latestPost);
        setValue(whenPostedXhtml + byWhomXhtml);
    }

    private static String getWhenPostedXhtml(final Post latestPost) {
        if (latestPost == null) {
            return "";
        }
        final String prettifiedTime = new PrettyTime().format(latestPost
                .getTime());
        return String.format("<div class=\"time\">%s</div>", prettifiedTime);
    }

    private static String getByWhomXhtml(final Post latestPost) {
        if (latestPost == null) {
            return "";
        }
        final String latestAuthorName = latestPost.getAuthor()
                .getDisplayedName();
        return String.format("<div class=\"author\">By %s</div>",
                latestAuthorName);
    }
}
