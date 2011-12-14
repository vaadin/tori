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
