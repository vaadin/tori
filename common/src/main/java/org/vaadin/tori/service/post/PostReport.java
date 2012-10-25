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

package org.vaadin.tori.service.post;

import org.vaadin.tori.data.entity.Post;

import edu.umd.cs.findbugs.annotations.NonNull;

public class PostReport {
    public static enum Reason {
        /** The post was deemed as spam */
        SPAM,

        /** The post was deemed offensive, abusive or hateful */
        OFFENSIVE,

        /** The post seems to be in the wrong category */
        WRONG_CATEGORY,

        /** A moderator needs to take a look at the post */
        MODERATOR_ALERT
    };

    private final Post post;
    private final Reason reason;
    private final String additionalInfo;
    private final String postUrl;

    public PostReport(final Post post, final Reason reason,
            final String additionalInfo, final String postUrl) {
        this.post = post;
        this.reason = reason;
        this.additionalInfo = additionalInfo;
        this.postUrl = postUrl;
    }

    public Post getPost() {
        return post;
    }

    public Reason getReason() {
        return reason;
    }

    public String getPostUrl() {
        return postUrl;
    }

    /**
     * Additional info given by the user. Populated only when
     * {@link #getReason()} returns {@link Reason#MODERATOR_ALERT}
     * 
     * @return The written message the user gave. Never returns
     *         <code>null</code>.
     */
    @NonNull
    public String getAdditionalInfo() {
        if (additionalInfo != null) {
            return additionalInfo;
        } else {
            return "";
        }
    }
}
