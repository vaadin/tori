package org.vaadin.tori.util;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.vaadin.tori.ToriUI;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.exception.DataSourceException;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class DatabaseFiller {

    private static final int WORDS_IN_POST_BASE = 100;
    private static final int WORDS_IN_POST_JITTER = 200;

    private static final int POSTS_IN_LONG_THREAD = 150;
    private static final int POSTS_IN_THREAD_BASE = 10;
    private static final int POSTS_IN_THREAD_JITTER = 20;

    private static final int THREAD_AMOUNT = 500;

    private static final String[] LIPSUM = new String[] { "Lorem", "ipsum",
            "dolor", "sit", "amet,", "consectetur", "adipiscing", "elit.",
            "Morbi", "accumsan", "sagittis", "nibh", "non", "tincidunt.",
            "Cras", "dignissim", "libero", "a", "libero", "porta", "vehicula.",
            "Etiam", "eleifend", "enim", "sit", "amet", "leo", "mattis", "et",
            "blandit", "purus", "congue.", "Lorem", "ipsum", "dolor", "sit",
            "amet,", "consectetur", "adipiscing", "elit.", "Mauris", "in",
            "neque", "est.", "Praesent", "a", "fermentum", "augue.", "Quisque",
            "congue", "faucibus", "libero", "a", "euismod.", "Nullam",
            "pharetra", "facilisis", "rutrum.", "Phasellus", "arcu", "tellus,",
            "laoreet", "a", "accumsan", "non,", "fringilla", "id", "libero.",
            "Curabitur", "sed", "est", "eros.", "Morbi", "condimentum", "urna",
            "vitae", "eros", "aliquam", "commodo.", "Pellentesque", "habitant",
            "morbi", "tristique", "senectus", "et", "netus", "et", "malesuada",
            "fames", "ac", "turpis", "egestas.", "Ut", "vel", "odio", "sed",
            "ipsum", "molestie", "bibendum.", "Quisque", "eget", "turpis",
            "est.", "Vestibulum", "vel", "neque", "quis", "urna",
            "sollicitudin", "commodo.", "Nulla", "luctus", "erat", "eget",
            "mi", "congue", "eget", "vestibulum", "nulla", "vestibulum.", "In",
            "quis", "quam", "a", "odio", "egestas", "pretium", "sed", "non",
            "orci.", "Nulla", "congue", "sem", "nec", "est", "imperdiet",
            "pulvinar.", "Praesent", "non", "congue", "nisi.", "Maecenas",
            "facilisis,", "odio", "eget", "convallis", "gravida,", "magna",
            "turpis", "vestibulum", "orci,", "a", "tempor", "neque", "turpis",
            "quis", "magna.", "Nullam", "imperdiet", "gravida", "posuere.",
            "Vestibulum", "at", "magna", "quis", "ipsum", "placerat",
            "accumsan.", "Donec", "ultricies,", "magna", "ut", "pellentesque",
            "iaculis,", "tortor", "tortor", "ultricies", "velit,", "at",
            "tristique", "est", "risus", "non", "orci.", "Donec", "non",
            "nibh", "nec", "erat", "dictum", "vulputate.", "Vestibulum",
            "ante", "ipsum", "primis", "in", "faucibus", "orci", "luctus",
            "et", "ultrices", "posuere", "cubilia", "Curae;", "Duis", "nec",
            "sapien", "non", "mi", "vehicula", "faucibus.", "Donec", "in",
            "diam", "id", "enim", "dignissim", "facilisis", "vel", "et",
            "metus.", "Aliquam", "erat", "volutpat.", "Duis", "eget", "massa",
            "diam.", "\n\n", "\n\n", "\n\n", "\n\n", "\n\n", "\n\n", "\n\n",
            "\n\n" };

    private DatabaseFiller() {
    }

    @SuppressWarnings("serial")
    public static Button getFillerButton() {
        return new Button("NUKE!", new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                try {
                    final DataSource ds = ToriUI.getCurrent().getDataSource();

                    final long startTime = System.currentTimeMillis();
                    Category category = null;
                    try {
                        category = new Category();
                        final String name = "Generated Category";
                        category.setName(name);
                        category.setDescription("");
                        category = ds.save(category);

                        for (final Category c : ds.getRootCategories()) {
                            if (c.getName().equals(name)) {
                                category = c;
                                System.out.println("FOUNDIT");
                                break;
                            }
                        }
                    } catch (final DataSourceException e) {
                        throw new RuntimeException(e);
                    }
                    try {

                        for (int threadNo = 0; threadNo < THREAD_AMOUNT; threadNo++) {
                            DiscussionThread thread = new DiscussionThread(
                                    "Generated Thread " + (threadNo + 1));
                            thread.setCategory(category);

                            final Post op = new Post();
                            op.setBodyRaw(getLipsum());
                            op.setTime(new Date());
                            thread = ds.saveNewThread(thread, null, op);

                            if (thread == null) {
                                throw new RuntimeException("thread was null");
                            }

                            final int cap = new Random()
                                    .nextInt(POSTS_IN_THREAD_JITTER)
                                    + POSTS_IN_THREAD_BASE;
                            System.out.println("saving " + (cap + 1)
                                    + " messages");
                            for (int postNo = 0; postNo < cap; postNo++) {
                                final Post post = new Post();
                                post.setBodyRaw(getLipsum());
                                post.setThread(thread);
                                post.setTime(new Date());
                                ds.saveAsCurrentUser(post, null);
                            }

                            System.out.println("Thread " + threadNo + ": "
                                    + (System.currentTimeMillis() - startTime));
                        }

                    } catch (final DataSourceException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        DiscussionThread thread = new DiscussionThread(
                                "Long Thrd");
                        thread.setCategory(category);

                        final Post op = new Post();
                        op.setBodyRaw(getLipsum());
                        op.setTime(new Date());
                        thread = ds.saveNewThread(thread, null, op);

                        for (int posts = 0; posts < POSTS_IN_LONG_THREAD; posts++) {
                            final Post post = new Post();
                            post.setBodyRaw(getLipsum());
                            post.setTime(new Date());
                            post.setThread(thread);
                            ds.saveAsCurrentUser(post, null);

                            if (posts % 10 == 0) {
                                System.out.println("LongThread "
                                        + posts
                                        + " "
                                        + (System.currentTimeMillis() - startTime));
                            }
                        }
                    } catch (final DataSourceException e) {
                        throw new RuntimeException(e);
                    }
                } catch (final RuntimeException e) {
                    throw e;
                }
            }

            private String getLipsum() {
                final StringBuilder sb = new StringBuilder();
                sb.append(UUID.randomUUID() + " ");
                final Random r = new Random();
                final int cap = r.nextInt(WORDS_IN_POST_JITTER)
                        + WORDS_IN_POST_BASE;
                for (int i = 0; i < cap; i++) {
                    sb.append(LIPSUM[r.nextInt(LIPSUM.length)] + " ");
                }
                return sb.toString();
            }
        });
    }
}
