package org.vaadin.tori.component.thread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.AbstractLazyLayout;
import org.vaadin.tori.component.GeneratedLazyLayout;
import org.vaadin.tori.component.GeneratedLazyLayout.ComponentGenerator;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.util.PoorMansProfiler;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;

/**
 * UI component for displaying a vertical hierarchical list of threads.
 */
@SuppressWarnings("serial")
@edu.umd.cs.findbugs.annotations.SuppressWarnings("SE_NO_SERIALVERSIONID")
public class ThreadListing extends CustomComponent {

    private static final String COLUMN_HEADER_TOPIC = "Topic";
    private static final String COLUMN_HEADER_STARTEDBY = "Started by";
    private static final String COLUMN_HEADER_POSTS = "Posts";
    private static final String COLUMN_HEADER_LATESTPOST = "Latest post";

    private static final String STYLE_FOLLOWING = "following";
    private static final String STYLE_COLUMN_HEADER = "column-header";
    private static final String STYLE_FIXED_COLUMNS = "fixed-column-headers";
    private static final String STYLE_COLUMN_HEADER_ROW = "column-header-row";

    protected static final String PLACEHOLDER_WIDTH = "100%";
    protected static final String PLACEHOLDER_HEIGHT = "40px";
    protected static final int RENDER_DELAY_MILLIS = 500;
    protected static final double RENDER_DISTANCE_MULTIPLIER = 1.5d;

    private static final long MAX_AMOUNT_OF_SHOWN_THREADS = 1000;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "ignoring serialization")
    protected final CategoryPresenter presenter;
    protected AbstractLazyLayout layout;

    private transient final ComponentGenerator componentGenerator = new ComponentGenerator() {
        @Override
        public List<Component> getComponentsAtIndexes(final int from,
                final int to) {

            final ArrayList<Component> components = new ArrayList<Component>();
            try {
                final PoorMansProfiler pmp = new PoorMansProfiler("liferya");
                pmp.mark("fetch");
                final List<DiscussionThread> threads = presenter
                        .getThreadsBetween(from, to);
                pmp.mark("fetched | added");
                for (final DiscussionThread thread : threads) {
                    components.add(new ThreadListingRow(thread, presenter));
                }
                pmp.stop();
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE,
                        Notification.TYPE_ERROR_MESSAGE);
            }
            return components;
        }

        @Override
        public int getAmountOfComponents() {
            try {
                return (int) Math.min(presenter.countThreads(),
                        MAX_AMOUNT_OF_SHOWN_THREADS);
            } catch (final DataSourceException e) {
                getRoot().showNotification(
                        DataSourceException.BORING_GENERIC_ERROR_MESSAGE,
                        Notification.TYPE_ERROR_MESSAGE);
                return 0;
            }
        }
    };

    protected final CssLayout root = new CssLayout();

    public ThreadListing(final CategoryPresenter presenter) {
        this.presenter = presenter;

        setStyleName("thread-listing");

        setCompositionRoot(root);

        layout = new GeneratedLazyLayout(componentGenerator);
        layout.setPlaceholderSize(PLACEHOLDER_HEIGHT, PLACEHOLDER_WIDTH);
        layout.setRenderDistanceMultiplier(RENDER_DISTANCE_MULTIPLIER);
        layout.setRenderDelay(RENDER_DELAY_MILLIS);
        layout.setStyleName("wrapper-layout");

        root.addComponent(getHeaderComponent());
        root.addComponent(layout);
        setCompositionRoot(root);
    }

    private Component getHeaderComponent() {
        final CssLayout headerRow = new CssLayout();
        headerRow.setStyleName(STYLE_COLUMN_HEADER_ROW);

        headerRow.addComponent(createColumnHeader(COLUMN_HEADER_TOPIC));
        final CssLayout detailsHeaders = new CssLayout();
        detailsHeaders.setStyleName(STYLE_FIXED_COLUMNS);
        detailsHeaders
                .addComponent(createColumnHeader(COLUMN_HEADER_STARTEDBY));
        detailsHeaders.addComponent(createColumnHeader(COLUMN_HEADER_POSTS));
        detailsHeaders
                .addComponent(createColumnHeader(COLUMN_HEADER_LATESTPOST));
        headerRow.addComponent(detailsHeaders);

        return headerRow;
    }

    private Label createColumnHeader(final String header) {
        final Label label = new Label(header);
        label.setSizeUndefined();
        label.setStyleName(header.toLowerCase().replaceAll(" ", "-"));
        label.addStyleName(STYLE_COLUMN_HEADER);
        return label;
    }

    private void assignStyles(final ThreadListingRow row) {
        if (presenter.userIsFollowing(row.getThread())) {
            row.addStyleName(STYLE_FOLLOWING);
        } else {
            row.removeStyleName(STYLE_FOLLOWING);
        }
    }

    private ThreadListingRow getRow(final DiscussionThread thread) {
        for (final Iterator<Component> it = layout.getComponentIterator(); it
                .hasNext();) {
            final Component row = it.next();
            if (row instanceof ThreadListingRow) {
                final ThreadListingRow rowInstance = (ThreadListingRow) row;
                if (rowInstance.getThread().equals(thread)) {
                    return rowInstance;
                }
            }
        }
        return null;
    }

    public void refresh(final DiscussionThread thread) {
        final ThreadListingRow row = getRow(thread);
        if (row != null) {
            final ThreadListingRow newRow = new ThreadListingRow(thread,
                    presenter);
            assignStyles(newRow);
            layout.replaceComponent(row, newRow);
        }
    }

    public void refreshStyles(final DiscussionThread thread) {
        final ThreadListingRow row = getRow(thread);
        if (row != null) {
            assignStyles(row);
        }
    }

}
