package org.vaadin.tori.component.thread;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.User;

import com.ocpsoft.pretty.time.PrettyTime;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class ThreadListingItem extends CustomComponent {

    public ThreadListingItem(final DiscussionThread thread) {

        final String topicXhtml = getTopicXhtml(thread);
        final String startedByXhtml = getStartedByXhtml(thread);
        final String postsXhtml = getPostsXhtml(thread);
        final String latestPostXhtml = getLatestPostXhtml(thread);

        final String rowXhtml = "<div class=\"thread-listing-item\">"
                + topicXhtml + startedByXhtml + postsXhtml + latestPostXhtml
                + "</div>";

        setCompositionRoot(new Label(rowXhtml, Label.CONTENT_XHTML));
    }

    private static final String getTopicXhtml(final DiscussionThread thread) {
        final String threadUrl = ToriNavigator.ApplicationView.THREADS.getUrl();
        final long id = thread.getId();
        final String topic = thread.getTopic();
        return String.format(
                "<div class=\"topic\"><a href=\"#%s/%s\">%s</a></div>",
                threadUrl, id, topic);
    }

    private static final String getStartedByXhtml(final DiscussionThread thread) {
        final User op = thread.getOriginalPoster();
        final String url = ToriNavigator.ApplicationView.USERS.getUrl();
        final long posterId = op.getId();
        final String name = op.getDisplayedName();
        return String.format(
                "<div class=\"started-by\"><a href=\"#%s/%s\">%s</a></div>",
                url, posterId, name);
    }

    private static final String getPostsXhtml(final DiscussionThread thread) {
        return String.format("<div class=\"posts\">%s</div>",
                thread.getPostCount());
    }

    private static final String getLatestPostXhtml(final DiscussionThread thread) {
        final Post latestPost = thread.getLatestPost();
        final String time = new PrettyTime().format(latestPost.getTime());
        final String authorName = latestPost.getAuthor().getDisplayedName();
        return String.format("<div class=\"latest-post\">" //
                + "<div class=\"time\">%s</div>" //
                + "<div class=\"author\">%s</div>" + //
                "</div>", //
                time, authorName);
    }
}
