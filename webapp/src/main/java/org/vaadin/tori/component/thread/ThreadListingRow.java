package org.vaadin.tori.component.thread;

import java.util.Date;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;

@SuppressWarnings("serial")
public class ThreadListingRow extends PopupView {

    private class RowContent implements Content {

        private String startedBy;
        private int postCount;
        private String topic;
        private boolean sticky;
        private boolean locked;
        private Date time;
        private String latestAuthor;
        private long threadId;

        @Override
        public String getMinimizedValueAsHTML() {
            final String locked = this.locked ? "<div class='locked'></div>"
                    : "";
            final String sticky = this.sticky ? "<div class='sticky'></div>"
                    : "";
            final String topic = "<div class='topic'>" + this.topic + "</div>";
            final String startedBy = "<div class='startedBy'>" + this.startedBy
                    + "</div>";
            final String postCount = "<div class='postcount'>" + this.postCount
                    + "</div>";
            final String latestAuthor = "<div class='latestauthor'>"
                    + this.latestAuthor + "</div>";
            final String time = String.format(
                    "<div class='time'>%1$td.%1$tm.%1$tY</div>", this.time);

            final String url = "#"
                    + ToriNavigator.ApplicationView.THREADS.getUrl() + "/"
                    + threadId;

            return String
                    .format("<a href='%s'>%s %s %s %s %s <div class='latestpost'>%s %s</div></a>",
                            url, locked, sticky, topic, startedBy, postCount,
                            time, latestAuthor);
        }

        @Override
        public Component getPopupComponent() {
            return contextMenu;
        }

        public void setStartedBy(final String displayedName) {
            this.startedBy = displayedName;
        }

        public void setPostAmount(final int postCount) {
            this.postCount = postCount;
        }

        public void setTopic(final String topic) {
            this.topic = topic;
        }

        public void isSticky(final boolean sticky) {
            this.sticky = sticky;
        }

        public void isLocked(final boolean locked) {
            this.locked = locked;
        }

        public void setLatestPostDate(final Date time) {
            this.time = time;
        }

        public void setLatestPostName(final String latestAuthor) {
            this.latestAuthor = latestAuthor;
        }

        public void setThreadId(final long threadId) {
            this.threadId = threadId;
        }

    }

    private static class ContextMenu extends CustomComponent {
        private final CategoryPresenter presenter;
        private final CssLayout layout = new CssLayout();

        public ContextMenu(final CategoryPresenter presenter) {
            this.presenter = presenter;
            setCompositionRoot(layout);
        }
    }

    private final DiscussionThread thread;
    private final ContextMenu contextMenu;

    public ThreadListingRow(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        super("", new Label());

        setStyleName("thread-listing-row");

        final RowContent content = new RowContent();
        this.thread = thread;

        initializePreview(thread, content);
        contextMenu = createContextMenu(thread, presenter);

        setContent(content);

        super.setHideOnMouseOut(false);
    }

    private static ContextMenu createContextMenu(final DiscussionThread thread,
            final CategoryPresenter presenter) {
        final ContextMenu menu = new ContextMenu(presenter);

        return menu;
    }

    private static void initializePreview(final DiscussionThread thread,
            final RowContent content) {
        content.setStartedBy(thread.getOriginalPoster().getDisplayedName());
        content.setLatestPostDate(thread.getLatestPost().getTime());
        content.setLatestPostName(thread.getLatestPost().getAuthor()
                .getDisplayedName());
        content.setPostAmount(thread.getPostCount());
        content.setTopic(thread.getTopic());
        content.isSticky(thread.isSticky());
        content.isLocked(thread.isLocked());
        content.setThreadId(thread.getId());
    }

    public DiscussionThread getThread() {
        return thread;
    }

    @Override
    public void setHideOnMouseOut(final boolean hideOnMouseOut) {
        // ignore calls.
    }
}
