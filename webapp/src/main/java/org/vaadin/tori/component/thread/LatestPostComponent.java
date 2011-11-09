package org.vaadin.tori.component.thread;

import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class LatestPostComponent extends CustomComponent {

    private Label root;

    public LatestPostComponent(final DiscussionThread thread) {
        setCompositionRoot(root = new Label("", Label.CONTENT_XHTML));
        setStyleName("latestPost");

        final Post latestPost = thread.getLatestPost();

        final String whenPostedXhtml = getWhenPostedXhtml(latestPost);
        final String byWhomXhtml = getByWhomXhtml(latestPost);
        root.setValue(whenPostedXhtml + byWhomXhtml);
    }

    private static String getWhenPostedXhtml(final Post latestPost) {
        final String prettifiedTime = new PrettyTime().format(latestPost
                .getTime());
        return String.format("<div class=\"time\">%s</div>", prettifiedTime);
    }

    private static String getByWhomXhtml(final Post latestPost) {
        final String latestAuthorName = latestPost.getAuthor()
                .getDisplayedName();
        return String.format("<div class=\"author\">By %s</div>",
                latestAuthorName);
    }
}
