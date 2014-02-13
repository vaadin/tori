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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import org.vaadin.tori.exception.NoSuchCategoryException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.service.post.PostReport.Reason;

public class TestDataSource implements DataSource {

    private static final String CONTEXT = "/webapp";
    private static final String ATTACHMENT_PREFIX = CONTEXT + "/attachments/";
    public static final long CURRENT_USER_ID = 3;
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
    public List<Category> getSubCategories(Long categoryId)
            throws DataSourceException {
        Category category = null;
        try {
            category = getCategory(categoryId != null ? categoryId : 0);
        } catch (NoSuchCategoryException e) {

        }
        return _getSubCategories(category);
    }

    private List<Category> _getSubCategories(final Category category)
            throws DataSourceException {
        return executeWithEntityManager(new Command<List<Category>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Category> execute(final EntityManager em) {
                final Query q = em
                        .createQuery("select c from Category c where c.parentCategory "
                                + (category != null ? "= :parent" : "is null"));
                if (category != null) {
                    q.setParameter("parent", category);
                }
                return q.getResultList();
            }
        });
    }

    @Override
    public List<DiscussionThread> getThreads(Long categoryId,
            final int startIndex, final int endIndex)
            throws DataSourceException {
        final Category category = getCategory(categoryId);
        return executeWithEntityManager(new Command<List<DiscussionThread>>() {
            @Override
            public final List<DiscussionThread> execute(final EntityManager em) {
                final TypedQuery<DiscussionThread> threadQuery = em
                        .createQuery(
                                "select t from DiscussionThread t "
                                        + "where t.category "
                                        + (category != null ? "= :category"
                                                : "is null")
                                        + " order by t.sticky desc, t.id desc",
                                DiscussionThread.class);
                if (startIndex >= 0 && endIndex >= 0) {
                    threadQuery.setFirstResult(startIndex);
                    threadQuery.setMaxResults(endIndex - startIndex + 1);
                    System.out.println("Querying threads from " + startIndex
                            + " to " + endIndex + ", max results "
                            + (endIndex - startIndex + 1) + ".");
                }
                if (category != null) {
                    threadQuery.setParameter("category", category);
                }

                return threadQuery.getResultList();
            }
        });
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category)
            throws DataSourceException {
        return getThreads(category.getId(), -1, -1);
    }

    @Override
    public Category getCategory(final Long categoryId)
            throws DataSourceException {
        Category category = null;
        if (categoryId != null) {
            category = executeWithEntityManager(new Command<Category>() {
                @Override
                public final Category execute(final EntityManager em) {
                    return em.find(Category.class, categoryId);
                }
            });
        }
        return category;
    }

    @Override
    public int getThreadCountRecursively(Long categoryId)
            throws DataSourceException {
        final Category category = getCategory(categoryId);
        final long threadCount = getThreadCount(categoryId);
        return executeWithEntityManager(new Command<Integer>() {
            @Override
            public Integer execute(final EntityManager em) {
                final TypedQuery<Long> query = em
                        .createQuery(
                                "select count(t) from DiscussionThread t where t.category = :category",
                                Long.class);
                query.setParameter("category", category);

                // recursively add thread count of all sub categories
                Long theThreadCount = threadCount;
                try {
                    final List<Category> subCategories = getSubCategories(category
                            .getId());
                    for (final Category subCategory : subCategories) {
                        theThreadCount += getThreadCountRecursively(subCategory
                                .getId());
                    }
                } catch (final DataSourceException e) {
                    throw new RuntimeException(e);
                }
                return new Long(theThreadCount).intValue();
            }
        });
    }

    @Override
    public int getThreadCount(Long categoryId) throws DataSourceException {
        Category category = null;
        if (categoryId != null) {
            category = getCategory(categoryId);
        }
        final Category theCategory = category;
        long result = executeWithEntityManager(new Command<Long>() {
            @Override
            public Long execute(final EntityManager em) {
                StringBuilder sb = new StringBuilder(
                        "select count(t) from DiscussionThread t where t.category");
                if (theCategory == null) {
                    sb.append(" IS NULL");
                } else {
                    sb.append(" = :category");
                }
                TypedQuery<Long> q = em.createQuery(sb.toString(), Long.class);

                if (theCategory != null) {
                    q.setParameter("category", theCategory);
                }
                return q.getSingleResult();
            }
        });
        return new Long(result).intValue();
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
    public List<Post> getPosts(long threadId) throws DataSourceException {
        final DiscussionThread thread = getThread(threadId);
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
        T execute(EntityManager em) throws DataSourceException;
    }

    @Override
    public void deleteCategory(final long categoryId)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    // must merge detached entity before removal
                    em.remove(em.find(Category.class, categoryId));
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
    public int getUnreadThreadCount(long categoryId) {
        return 0;
    }

    @Override
    public void savePost(final long postId, final String rawBody)
            throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                Post post = em.find(Post.class, postId);
                post.setBodyRaw(rawBody);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void banUser(long userId) throws DataSourceException {
        User user = getUser(userId);
        user.setBanned(true);
        save(user);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void unbanUser(long userId) throws DataSourceException {
        User user = getUser(userId);
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
    public void followThread(long threadId) throws DataSourceException {
        DiscussionThread thread = getThread(threadId);
        if (!isFollowingThread(threadId)) {
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
    public void unfollowThread(long threadId) throws DataSourceException {
        final DiscussionThread thread = getThread(threadId);
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
    public boolean isFollowingThread(long threadId) {
        try {
            final DiscussionThread thread = getThread(threadId);
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
        } catch (DataSourceException e) {
            return false;
        }
    }

    @Override
    public void deletePost(long postId) throws DataSourceException {
        final Post post = getPost(postId);
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em)
                    throws DataSourceException {
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
                    throw new DataSourceException(e);
                }
                return null;
            }
        });
    }

    @Override
    public Boolean getPostVote(long postId) throws DataSourceException {
        Boolean result = null;
        PostVote vote = getPostVoteInternal(postId);
        if (vote.isDownvote()) {
            result = false;
        } else if (vote.isUpvote()) {
            result = true;
        }
        return result;
    }

    private PostVote getPostVoteInternal(long postId)
            throws DataSourceException {
        final Post post = getPost(postId);
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
    public void upvote(long postId) throws DataSourceException {
        final PostVote vote = getPostVoteInternal(postId);
        vote.setUpvote();
        save(vote);
    }

    @Override
    public void downvote(long postId) throws DataSourceException {
        final PostVote vote = getPostVoteInternal(postId);
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

                Post post = em.find(Post.class, vote.getPost().getId());
                List<PostVote> postVotes = post.getPostVotes();
                if (postVotes == null) {
                    post.setPostVotes(new ArrayList<PostVote>());
                    postVotes = post.getPostVotes();
                }
                postVotes.add(vote);

                transaction.commit();
                return null;
            }
        });
    }

    @Override
    public void removeUserVote(long postId) throws DataSourceException {
        delete(getPostVoteInternal(postId));
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
    public long getPostScore(long postId) throws DataSourceException {
        final Post post = getPost(postId);
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
    public Post saveReply(final String rawBody,
            final Map<String, byte[]> attachments, final long threadId)
            throws DataSourceException {

        return executeWithEntityManager(new Command<Post>() {
            @Override
            public Post execute(final EntityManager em) {
                em.getTransaction().begin();

                final Post post = new Post();
                post.setBodyRaw(rawBody);
                post.setAuthor(currentUser);
                post.setTime(new Date());

                final DiscussionThread thread = em.find(DiscussionThread.class,
                        threadId);
                thread.getPosts().add(post);
                em.persist(post);
                post.setThread(thread);

                persistPostAttachments(post, attachments, em);

                em.getTransaction().commit();
                return post;
            }
        });

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
    public void moveThread(long threadId, final Long destinationCategoryId)
            throws DataSourceException {
        final DiscussionThread thread = getThread(threadId);
        final Category destinationCategory = getCategory(destinationCategoryId);
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
    public void stickyThread(long threadId) throws DataSourceException {
        DiscussionThread thread = getThread(threadId);
        thread.setSticky(true);
        save(thread);
    }

    @Override
    public void unstickyThread(long threadId) throws DataSourceException {
        DiscussionThread thread = getThread(threadId);
        thread.setSticky(false);
        save(thread);
    }

    @Override
    public void lockThread(long threadId) throws DataSourceException {
        DiscussionThread thread = getThread(threadId);
        thread.setLocked(true);
        save(thread);
    }

    @Override
    public void unlockThread(long threadId) throws DataSourceException {
        DiscussionThread thread = getThread(threadId);
        thread.setLocked(false);
        save(thread);
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
    public void deleteThread(final long threadId) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction t = em.getTransaction();
                t.begin();
                final DiscussionThread thread = em.find(DiscussionThread.class,
                        threadId);

                // remove all Following references
                final Query followDelete = em
                        .createQuery("delete from Following f where f.thread = :thread");
                followDelete.setParameter("thread", thread);
                followDelete.executeUpdate();

                try {
                    // remove all votes for posts inside thread.
                    for (final Post post : getPosts(thread.getId())) {
                        // "in" is not supported :(
                        final Query postDelete = em
                                .createQuery("delete from PostVote where post = :post");
                        postDelete.setParameter("post", post);
                        postDelete.executeUpdate();
                    }
                } catch (final DataSourceException e) {
                    throw new RuntimeException(e);
                }

                em.remove(thread);
                t.commit();
                return null;
            }
        });
    }

    @Override
    public Post saveNewThread(final String topic, final String rawBody,
            final Map<String, byte[]> attachments, Long categoryId)
            throws DataSourceException {
        final Category category = categoryId == null ? null
                : getCategory(categoryId);
        return executeWithEntityManager(new Command<Post>() {
            @Override
            public Post execute(final EntityManager em) {
                DiscussionThread newThread = new DiscussionThread();
                newThread.setCategory(category);
                newThread.setTopic(topic);

                Post firstPost = new Post();
                firstPost.setAuthor(currentUser);
                firstPost.setBodyRaw(rawBody);
                firstPost.setTime(new Date());

                newThread.setPosts(Arrays.asList(firstPost));

                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                final DiscussionThread mergedThread = em.merge(newThread);
                firstPost.setThread(mergedThread);
                final Post post = em.merge(firstPost);

                persistPostAttachments(post, attachments, em);

                transaction.commit();

                return post;
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
    public boolean isThreadRead(long threadId) {
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
    public int getRecentPostsCount() throws DataSourceException {
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
    public List<DiscussionThread> getMyPostThreads(final int from, final int to)
            throws DataSourceException {
        System.out.println("TestDataSource.getMyPostThreads(): "
                + "getMyPostThreads not implemented in "
                + getClass().getSimpleName() + ".");
        return Collections.emptyList();
    }

    @Override
    public int getMyPostThreadsCount() throws DataSourceException {
        System.out.println("TestDataSource.getMyPostThreadsCount(): "
                + "getMyPostThreads not implemented in "
                + getClass().getSimpleName() + ".");
        return 0;
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
    public String getPathRoot() {
        return null;
    }

    @Override
    public UrlInfo getUrlInfoFromBackendNativeRequest(
            final HttpServletRequest servletRequest)
            throws NoSuchThreadException, DataSourceException {
        return null;
    }

    @Override
    public User getToriUser(long userId) {
        User user = null;
        if (userId > 0) {
            try {
                user = getUser(userId);
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    @Override
    public Post getPost(final long postId) throws DataSourceException {
        return executeWithEntityManager(new Command<Post>() {
            @Override
            public final Post execute(final EntityManager em) {
                return em.find(Post.class, postId);
            }
        });
    }

    @Override
    public boolean getUpdatePageTitle() {
        return true;
    }

    @Override
    public String getPageTitlePrefix() {
        return "Tori";
    }

    @Override
    public void saveNewCategory(final Long parentCategoryId, final String name,
            final String description) throws DataSourceException {
        executeWithEntityManager(new Command<Category>() {
            @Override
            public Category execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    if (parentCategoryId != null) {
                        category.setParentCategory(getCategory(parentCategoryId));
                    }
                    em.persist(category);
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
    public void updateCategory(final long categoryId, final String name,
            final String description) throws DataSourceException {
        executeWithEntityManager(new Command<Category>() {
            @Override
            public Category execute(final EntityManager em) {
                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                try {
                    Category category = em.find(Category.class, categoryId);
                    category.setName(name);
                    category.setDescription(description);
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
    public void reportPost(long postId, Reason reason, String additionalInfo,
            String postUrl) {
        System.out.println("TestDataSource.reportPost()");
        System.out.println("Post: " + postId);
        System.out.println("Reason: " + reason);
        System.out.println("Info: " + additionalInfo);
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}
