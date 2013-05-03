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

package org.vaadin.tori.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.vaadin.tori.Configuration;
import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.AttachmentData;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.data.util.PersistenceUtil;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.service.post.PostReport;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class TestDataSource implements DataSource, DebugDataSource {

    private static final long CURRENT_USER_ID = 3;
    private final User currentUser;

    public TestDataSource() throws DataSourceException {
        if (isEmptyDatabase()) {
            initDatabaseWithTestData();
        }

        currentUser = getUser(CURRENT_USER_ID);
    }

    private User getUser(final long userId) throws DataSourceException {
        return executeWithEntityManager(new Command<User>() {
            @Override
            public final User execute(final EntityManager em) {
                return em.find(User.class, userId);
            }
        });
    }

    private void initDatabaseWithTestData() throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public final Void execute(final EntityManager em) {
                // read the "test-data.sql" file
                final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getClass().getClassLoader()
                                .getResourceAsStream("test-data.sql")));

                // Insert all test data in a single transaction
                // i.e. all data will be in the database or nothing will.
                em.getTransaction().begin();
                try {
                    String sqlLine;
                    while ((sqlLine = reader.readLine()) != null) {
                        sqlLine = sqlLine.trim();
                        if (sqlLine.length() > 0 && !sqlLine.startsWith("#")) {
                            // not a comment line -> run as native query
                            em.createNativeQuery(sqlLine).executeUpdate();
                        }
                    }
                    em.getTransaction().commit();
                } catch (final Exception e) {
                    em.getTransaction().rollback();
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    private boolean isEmptyDatabase() throws DataSourceException {
        return executeWithEntityManager(new Command<Boolean>() {
            @Override
            public final Boolean execute(final EntityManager em) {
                final TypedQuery<Long> q = em.createQuery(
                        "select count(c) from Category c", Long.class);
                final Long categoryCount = q.getSingleResult();
                return categoryCount == 0;
            }
        });
    }

    @Override
    public List<Category> getRootCategories() throws DataSourceException {
        return _getSubCategories(null);
    }

    @Override
    public List<Category> getSubCategories(final Category category)
            throws DataSourceException {
        return _getSubCategories(category);
    }

    @NonNull
    private List<Category> _getSubCategories(final Category category)
            throws DataSourceException {
        return executeWithEntityManager(new Command<List<Category>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Category> execute(final EntityManager em) {
                final Query q = em
                        .createQuery("select c from Category c where c.parentCategory "
                                + (category != null ? "= :parent" : "is null")
                                + " order by c.displayOrder");
                if (category != null) {
                    q.setParameter("parent", category);
                }
                return q.getResultList();
            }
        });
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category,
            final int startIndex, final int endIndex)
            throws DataSourceException {
        return executeWithEntityManager(new Command<List<DiscussionThread>>() {
            @Override
            public final List<DiscussionThread> execute(final EntityManager em) {
                final TypedQuery<DiscussionThread> threadQuery = em
                        .createQuery(
                                "select t from DiscussionThread t "
                                        + "where t.category = :category order by t.sticky desc, t.id desc",
                                DiscussionThread.class);
                if (startIndex >= 0 && endIndex >= 0) {
                    threadQuery.setFirstResult(startIndex);
                    threadQuery.setMaxResults(endIndex - startIndex + 1);
                    System.out.println("Querying threads from " + startIndex
                            + " to " + endIndex + ", max results "
                            + (endIndex - startIndex + 1) + ".");
                }
                threadQuery.setParameter("category", category);

                return threadQuery.getResultList();
            }
        });
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category)
            throws DataSourceException {
        return getThreads(category, -1, -1);
    }

    @Override
    public Category getCategory(final long categoryId)
            throws DataSourceException {
        return executeWithEntityManager(new Command<Category>() {
            @Override
            public final Category execute(final EntityManager em) {
                return em.find(Category.class, categoryId);
            }
        });
    }

    @Override
    public long getThreadCountRecursively(final Category category)
            throws DataSourceException {
        return executeWithEntityManager(new Command<Long>() {
            @Override
            public Long execute(final EntityManager em) {
                final TypedQuery<Long> query = em
                        .createQuery(
                                "select count(t) from DiscussionThread t where t.category = :category",
                                Long.class);
                query.setParameter("category", category);

                long threadCount = query.getSingleResult();

                // recursively add thread count of all sub categories
                try {
                    final List<Category> subCategories = getSubCategories(category);
                    for (final Category subCategory : subCategories) {
                        threadCount += getThreadCountRecursively(subCategory);
                    }
                } catch (final DataSourceException e) {
                    throw new RuntimeException(e);
                }
                return threadCount;
            }
        });
    }

    @Override
    public long getThreadCount(final Category category)
            throws DataSourceException {
        return executeWithEntityManager(new Command<Long>() {
            @Override
            public Long execute(final EntityManager em) {
                final TypedQuery<Long> q = em
                        .createQuery(
                                "select count(t) from DiscussionThread t where t.category = :category",
                                Long.class);
                q.setParameter("category", category);
                return q.getSingleResult();
            }
        });
    }

    @Override
    public DiscussionThread getThread(final long threadId)
            throws DataSourceException {
        return executeWithEntityManager(new Command<DiscussionThread>() {
            @Override
            public final DiscussionThread execute(final EntityManager em) {
                return em.find(DiscussionThread.class, threadId);
            }
        });
    }

    @Override
    public List<Post> getPosts(final DiscussionThread thread)
            throws DataSourceException {
        return executeWithEntityManager(new Command<List<Post>>() {
            @Override
            public List<Post> execute(final EntityManager em) {
                final TypedQuery<Post> query = em.createQuery(
                        "select p from Post p where p.thread = :thread "
                                + "order by p.time asc", Post.class);
                query.setParameter("thread", thread);
                return query.getResultList();
            }
        });
    }

    /**
     * Convenience method to execute a {@link Command} with an EntityManager
     * instance that will always be closed after the execution.
     * 
     * @param command
     * @return
     */
    private static <T> T executeWithEntityManager(final Command<T> command)
            throws DataSourceException {
        final EntityManager em = PersistenceUtil.createEntityManager();
        try {
            return command.execute(em);
        } catch (final Throwable e) {
            throw new DataSourceException(e);
        } finally {
            em.close();
        }
    }

    private static interface Command<T> {
        T execute(EntityManager em);
    }

    @Override
    public void save(final Iterable<Category> categoriesToSave)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    for (final Category categoryToSave : categoriesToSave) {
                        em.merge(categoryToSave);
                    }
                    transaction.commit();
                } catch (final Exception e) {
                    transaction.rollback();
                }
                return null;
            }
        });
    }

    @Override
    public Category save(final Category categoryToSave)
            throws DataSourceException {
        executeWithEntityManager(new Command<Category>() {
            @Override
            public Category execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    if (categoryToSave.getId() == 0) {
                        em.persist(categoryToSave);
                    } else {
                        em.merge(categoryToSave);
                    }
                    transaction.commit();
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                }
                return null;
            }
        });

        return null;
    }

    @Override
    public void delete(final Category categoryToDelete)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    // must merge detached entity before removal
                    final Category mergedCategory = em.merge(categoryToDelete);
                    em.remove(mergedCategory);
                    transaction.commit();
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                }
                return null;
            }
        });
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL", justification = "System.out will never be null, afaik")
    public void reportPost(final PostReport report) {
        System.out.println("TestDataSource.reportPost()");
        System.out.println("Post: " + report.getPost());
        System.out.println("Reason: " + report.getReason());
        System.out.println("Info: " + report.getAdditionalInfo());
    }

    @Override
    public long getUnreadThreadCount(final Category category) {
        // TODO implement actual unread thread logic
        return 0;
    }

    @Override
    public void save(final Post post) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {

                final DiscussionThread thread = post.getThread();
                thread.getPosts().add(post);

                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                // TODO: Handle attachments!
                em.merge(thread);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void ban(final User user) throws DataSourceException {
        user.setBanned(true);
        save(user);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void unban(@NonNull final User user) throws DataSourceException {
        user.setBanned(false);
        save(user);
    }

    private void save(final User user) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                em.merge(user);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void follow(final DiscussionThread thread)
            throws DataSourceException {
        if (!isFollowing(thread)) {
            final org.vaadin.tori.data.entity.Following following = new org.vaadin.tori.data.entity.Following();
            following.setFollower(currentUser);
            following.setThread(thread);
            save(following);
        }
    }

    @SuppressWarnings("deprecation")
    private void save(final org.vaadin.tori.data.entity.Following following)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                em.merge(following);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    public void unFollow(final DiscussionThread thread)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    final Query query = em
                            .createQuery("delete from Following f where f.thread = :thread and f.follower = :follower");
                    query.setParameter("thread", thread);
                    query.setParameter("follower", currentUser);
                    query.executeUpdate();
                } finally {
                    transaction.commit();
                }
                return null;
            }
        });
    }

    @Override
    public boolean isFollowing(final DiscussionThread thread)
            throws DataSourceException {
        return executeWithEntityManager(new Command<Boolean>() {
            @Override
            public Boolean execute(final EntityManager em) {
                final TypedQuery<Long> query = em
                        .createQuery(
                                "select count(f) from Following f where f.follower = :follower AND f.thread = :thread",
                                Long.class);
                query.setParameter("follower", currentUser);
                query.setParameter("thread", thread);
                final Long result = query.getSingleResult();
                if (result != null) {
                    return result > 0;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void delete(final Post post) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    final DiscussionThread thread = post.getThread();
                    thread.getPosts().remove(post);

                    // must merge detached entity before removal
                    final DiscussionThread mergedThread = em.merge(thread);
                    em.merge(mergedThread);
                    transaction.commit();
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                }
                return null;
            }
        });
    }

    @Override
    public PostVote getPostVote(final Post post) throws DataSourceException {
        return executeWithEntityManager(new Command<PostVote>() {
            @Override
            public PostVote execute(final EntityManager em) {
                try {
                    final TypedQuery<PostVote> query = em.createQuery(
                            "select v from PostVote v where v.voter = :voter "
                                    + "and v.post = :post", PostVote.class);
                    query.setParameter("voter", currentUser);
                    query.setParameter("post", post);
                    return query.getSingleResult();
                } catch (final NoResultException e) {
                    final PostVote vote = new PostVote();
                    vote.setPost(post);
                    vote.setVoter(currentUser);
                    return vote;
                }
            }
        });
    }

    @Override
    public void upvote(final Post post) throws DataSourceException {
        final PostVote vote = getPostVote(post);
        vote.setUpvote();
        save(vote);
    }

    @Override
    public void downvote(final Post post) throws DataSourceException {
        final PostVote vote = getPostVote(post);
        vote.setDownvote();
        save(vote);
    }

    public void save(final PostVote vote) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                em.merge(vote);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    public void removeUserVote(final Post post) throws DataSourceException {
        delete(getPostVote(post));
    }

    private void delete(final PostVote postVote) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    // must merge detached entity before removal
                    final PostVote mergedVote = em.merge(postVote);
                    em.remove(mergedVote);
                    transaction.commit();
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                }
                return null;
            }
        });
    }

    @Override
    public long getScore(final Post post) throws DataSourceException {
        return executeWithEntityManager(new Command<Long>() {
            @Override
            public Long execute(final EntityManager em) {
                final TypedQuery<Long> query = em
                        .createQuery(
                                "select SUM(v.vote) from PostVote v where v.post = :post",
                                Long.class);
                query.setParameter("post", post);
                final Long singleResult = query.getSingleResult();
                if (singleResult != null) {
                    return singleResult;
                } else {
                    return 0L;
                }
            }
        });
    }

    @Override
    public Post saveAsCurrentUser(final Post post,
            final Map<String, byte[]> files) throws DataSourceException {
        post.setAuthor(currentUser);

        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                em.getTransaction().begin();

                final DiscussionThread thread = em.find(DiscussionThread.class,
                        post.getThread().getId());
                thread.getPosts().add(post);
                em.persist(post);

                persistPostAttachments(post, files, em);

                em.getTransaction().commit();
                return null;
            }
        });

        return post;
    }

    private void persistPostAttachments(final Post post,
            final Map<String, byte[]> files, final EntityManager em) {
        if (files == null) {
            return;
        }

        if (!em.getTransaction().isActive()) {
            throw new IllegalArgumentException("Transaction inactive");
        }
        if (!em.contains(post)) {
            throw new IllegalArgumentException("Post instance not managed");
        }

        if (post.getAttachments() == null) {
            post.setAttachments(new ArrayList<Attachment>());
        }

        for (final Entry<String, byte[]> entry : files.entrySet()) {
            final Attachment attachment = new Attachment();
            attachment.setFilename(entry.getKey());
            attachment.setPost(post);
            attachment.setFileSize(entry.getValue().length);
            em.persist(attachment);
            post.getAttachments().add(attachment);

            final AttachmentData attachmentData = new AttachmentData();
            attachmentData.setAttachment(attachment);
            attachmentData.setData(entry.getValue());
            em.persist(attachmentData);

            em.flush();

            attachment.setDownloadUrl(ATTACHMENT_PREFIX
                    + attachmentData.getId() + "/" + attachment.getFilename());
        }
    }

    @Override
    public void move(final DiscussionThread thread,
            final Category destinationCategory) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {

            @Override
            public Void execute(final EntityManager em) {
                thread.setCategory(destinationCategory);
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                em.merge(thread);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    public DiscussionThread sticky(final DiscussionThread thread)
            throws DataSourceException {
        thread.setSticky(true);
        return save(thread);
    }

    @Override
    public DiscussionThread unsticky(final DiscussionThread thread)
            throws DataSourceException {
        thread.setSticky(false);
        return save(thread);
    }

    @Override
    public DiscussionThread lock(final DiscussionThread thread)
            throws DataSourceException {
        thread.setLocked(true);
        return save(thread);
    }

    @Override
    public DiscussionThread unlock(final DiscussionThread thread)
            throws DataSourceException {
        thread.setLocked(false);
        return save(thread);
    }

    private static DiscussionThread save(final DiscussionThread thread)
            throws DataSourceException {
        return executeWithEntityManager(new Command<DiscussionThread>() {
            @Override
            public DiscussionThread execute(final EntityManager em) {
                final EntityTransaction t = em.getTransaction();
                t.begin();
                final DiscussionThread mergedThread = em.merge(thread);
                t.commit();
                return mergedThread;
            }
        });
    }

    @Override
    public void delete(final DiscussionThread thread)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction t = em.getTransaction();
                t.begin();
                final DiscussionThread mergedThread = em.merge(thread);

                // remove all Following references
                final Query followDelete = em
                        .createQuery("delete from Following f where f.thread = :thread");
                followDelete.setParameter("thread", thread);
                followDelete.executeUpdate();

                try {
                    // remove all votes for posts inside thread.
                    for (final Post post : getPosts(mergedThread)) {
                        // "in" is not supported :(
                        final Query postDelete = em
                                .createQuery("delete from PostVote where post = :post");
                        postDelete.setParameter("post", post);
                        postDelete.executeUpdate();
                    }
                } catch (final DataSourceException e) {
                    throw new RuntimeException(e);
                }

                em.remove(mergedThread);
                t.commit();
                return null;
            }
        });
    }

    @Override
    public DiscussionThread saveNewThread(final DiscussionThread newThread,
            final Map<String, byte[]> files, final Post firstPost)
            throws DataSourceException {
        return executeWithEntityManager(new Command<DiscussionThread>() {
            @Override
            public DiscussionThread execute(final EntityManager em) {
                firstPost.setAuthor(currentUser);

                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                final DiscussionThread mergedThread = em.merge(newThread);
                firstPost.setThread(mergedThread);
                final Post post = em.merge(firstPost);

                persistPostAttachments(post, files, em);

                transaction.commit();

                return mergedThread;
            }
        });
    }

    @Override
    public void incrementViewCount(final DiscussionThread thread)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();

                transaction.begin();
                final Query q = em
                        .createQuery("update DiscussionThread t set t.viewCount = t.viewCount + 1 where t = :thread");
                q.setParameter("thread", thread);
                q.executeUpdate();
                transaction.commit();

                return null;
            }
        });
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DMI_RANDOM_USED_ONLY_ONCE", justification = "just a test implementation")
    public boolean isRead(final DiscussionThread thread) {
        // TODO actual implementation
        return new Random().nextBoolean();
    }

    @Override
    public void markRead(final DiscussionThread thread)
            throws DataSourceException {
        System.out.println(String.format(
                "Marking thread %d as read. Not actually implemented in "
                        + getClass().getSimpleName() + ".", thread.getId()));
    }

    @Override
    public List<DiscussionThread> getRecentPosts(final int from, final int to)
            throws DataSourceException {
        final List<Post> posts = executeWithEntityManager(new Command<List<Post>>() {
            @Override
            public List<Post> execute(final EntityManager em) {
                final EntityTransaction tx = em.getTransaction();
                tx.begin();
                final TypedQuery<Post> q = em
                        .createQuery(
                                "select p from Post p order by p.time desc",
                                Post.class);
                q.setFirstResult(from);
                q.setMaxResults(to - from + 1);
                final List<Post> result = q.getResultList();
                tx.commit();
                return result;
            }
        });

        final List<DiscussionThread> threads = new ArrayList<DiscussionThread>();

        for (final Post post : posts) {
            threads.add(post.getThread());
        }

        return threads;
    }

    @Override
    public int getRecentPostsAmount() throws DataSourceException {
        final Number number = executeWithEntityManager(new Command<Number>() {
            @Override
            public Number execute(final EntityManager em) {
                final EntityTransaction tx = em.getTransaction();
                tx.begin();
                final TypedQuery<Number> q = em.createQuery(
                        "select count(p) from Post p", Number.class);
                final Number result = q.getSingleResult();
                tx.commit();
                return result;
            }
        });
        return number.intValue();
    }

    @Override
    public List<DiscussionThread> getMyPosts(final int from, final int to)
            throws DataSourceException {
        System.out.println("TestDataSource.getMyPosts(): "
                + "My posts not implemented in " + getClass().getSimpleName()
                + ".");
        return Collections.emptyList();
    }

    @Override
    public int getMyPostsAmount() throws DataSourceException {
        System.out.println("TestDataSource.getMyPostsAmount(): "
                + "My posts not implemented in " + getClass().getSimpleName()
                + ".");
        return 0;
    }

    @Override
    public byte[] getAttachmentData(final long id) {
        try {
            return executeWithEntityManager(new Command<byte[]>() {
                @Override
                public byte[] execute(final EntityManager em) {
                    final AttachmentData data = em.find(AttachmentData.class,
                            id);
                    return data.getData();
                }
            });
        } catch (final DataSourceException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getAttachmentMaxFileSize() {
        return 307200;
    }

    @Override
    public boolean isLoggedInUser() {
        return true;
    }

    @Override
    public Map<String, String> getPostReplacements() {
        return new HashMap<String, String>();
    }

    @Override
    public void save(final Configuration conf) {
    }

    @Override
    public boolean getReplaceMessageBoardsLinks() {
        return false;
    }

    @Override
    public String getGoogleAnalyticsTrackerId() {
        return null;
    }

    @Override
    @Deprecated
    @CheckForNull
    public UrlInfo getToriFragment(@NonNull final String queryUrl,
            final String queryPart) throws Exception {
        return null;
    }

    @Override
    @Deprecated
    public String getPathRoot() {
        return null;
    }

    @Override
    @CheckForNull
    public UrlInfo getUrlInfoFromBackendNativeRequest(
            final HttpServletRequest servletRequest)
            throws NoSuchThreadException, DataSourceException {
        return null;
    }
}
