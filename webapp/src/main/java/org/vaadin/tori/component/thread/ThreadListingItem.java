package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriUtil;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.Thread;
import org.vaadin.tori.data.entity.User;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class ThreadListingItem extends CustomComponent {

    public ThreadListingItem(final Thread thread) {

        final String topicXhtml = getTopicXtml(thread);
        final String startedByXhtml = getStartedByXtml(thread);
        final String postsXhtml = getPostsXtml(thread);
        final String latestPostXhtml = getLatestPostXhtml(thread);

        final String rowXhtml = "<div class=\"thread-listing-item\">"
                + topicXhtml + startedByXhtml + postsXhtml + latestPostXhtml
                + "</div>";

        setCompositionRoot(new Label(rowXhtml, Label.CONTENT_XHTML));
    }

    private static final String getTopicXtml(final Thread thread) {
        final String threadUrl = ToriNavigator.ApplicationView.THREADS.getUrl();
        final long id = thread.getId();
        final String topic = thread.getTopic();
        return String.format(
                "<div class=\"topic\"><a href=\"#%s/%s\">%s</a></div>",
                threadUrl, id, topic);
    }

    private static final String getStartedByXtml(final Thread thread) {
        final User op = thread.getOriginalPoster();
        final String url = ToriNavigator.ApplicationView.USERS.getUrl();
        final long posterId = op.getId();
        final String name = op.getDisplayedName();
        return String.format(
                "<div class=\"started-by\"><a href=\"#%s/%s\">%s</a></div>",
                url, posterId, name);
    }

    private static final String getPostsXtml(final Thread thread) {
        return String.format("<div class=\"posts\">%s</div>",
                thread.getPostCount());
    }

    private static final String getLatestPostXhtml(final Thread thread) {
        final Post latestPost = thread.getLatestPost();
        final String time = ToriUtil
                .getRelativeTimeString(latestPost.getTime());
        final String authorName = latestPost.getAuthor().getDisplayedName();
        return String.format("<div class=\"latest-post\">" //
                + "<div class=\"time\">%s</div>" //
                + "<div class=\"author\">%s</div>" + //
                "</div>", //
                time, authorName);
    }
}
