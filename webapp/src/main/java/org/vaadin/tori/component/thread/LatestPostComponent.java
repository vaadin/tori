package org.vaadin.tori.component.thread;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class LatestPostComponent extends Label {

    public LatestPostComponent(final DiscussionThread thread) {
        super("", ContentMode.XHTML);
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
