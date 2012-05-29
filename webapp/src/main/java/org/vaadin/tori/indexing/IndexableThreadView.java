package org.vaadin.tori.indexing;

import java.util.List;

import org.vaadin.tori.ToriNavigator.ApplicationView;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;

public class IndexableThreadView extends IndexableView {

    public IndexableThreadView(final List<String> arguments,
            final ToriIndexableApplication application) {
        super(arguments, application);
    }

    @Override
    public String getHtml() {

        if (arguments.isEmpty()) {
            return "No thread given";
        }

        try {
            final long threadId = Long.parseLong(arguments.get(0));
            final DiscussionThread thread = application.getDataSource()
                    .getThread(threadId);

            if (thread == null) {
                return "No such thread";
            }

            final List<Post> posts = application.getDataSource().getPosts(
                    thread);

            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("<a href='#%s'>Back to Category</a>",
                    getCategoryLink(thread)));
            sb.append("<h2>" + escapeXhtml(thread.getTopic()) + "</h2>");
            if (!posts.isEmpty()) {
                for (final Post post : posts) {
                    sb.append("<article>");

                    sb.append("<header>");
                    sb.append("<span class='author'>"
                            + escapeXhtml(post.getAuthor().getDisplayedName())
                            + "</span>");
                    sb.append(String.format(
                            "<time pubdate>%1$tY-%1$tm-%1$td</time>",
                            post.getTime()));
                    sb.append("</header>");

                    sb.append("<section>");
                    sb.append(application.getPostFormatter().format(
                            post.getBodyRaw()));
                    sb.append("</section>");

                    sb.append("</article>");
                }
            } else {
                sb.append("No posts in this thread. Sounds like a problem to me.");
            }

            return sb.toString();

        } catch (final NumberFormatException e) {
            e.printStackTrace();
            return "Invalid thread argument format";
        } catch (final DataSourceException e) {
            e.printStackTrace();
            return "There's a problem with the database";
        }
    }

    private String getCategoryLink(final DiscussionThread thread) {
        return ApplicationView.CATEGORIES.getUrl() + "/"
                + thread.getCategory().getId();
    }
}
