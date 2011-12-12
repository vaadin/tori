package org.vaadin.tori.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.PostVote;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.data.util.PersistenceUtil;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.service.post.PostReport;

import edu.umd.cs.findbugs.annotations.NonNull;

public class TestDataSource implements DataSource {

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
    public List<DiscussionThread> getThreads(final Category category)
            throws DataSourceException {
        return executeWithEntityManager(new Command<List<DiscussionThread>>() {
            @Override
            public final List<DiscussionThread> execute(final EntityManager em) {
                final List<DiscussionThread> threads = new ArrayList<DiscussionThread>();

                final TypedQuery<DiscussionThread> stickyQuery = em
                        .createQuery("select t from DiscussionThread t "
                                + "where t.sticky = :sticky "
                                + "and t.category = :category",
                                DiscussionThread.class);
                stickyQuery.setParameter("sticky", true);
                stickyQuery.setParameter("category", category);
                threads.addAll(stickyQuery.getResultList());

                final TypedQuery<DiscussionThread> nonStickyQuery = em
                        .createQuery("select t from DiscussionThread t "
                                + "where t.sticky = :sticky "
                                + "and t.category = :category",
                                DiscussionThread.class);
                nonStickyQuery.setParameter("sticky", false);
                nonStickyQuery.setParameter("category", category);
                threads.addAll(nonStickyQuery.getResultList());

                return threads;
            }
        });
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
    public long getThreadCount(final Category category)
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
                        threadCount += getThreadCount(subCategory);
                    }
                } catch (final DataSourceException e) {
                    throw new RuntimeException(e);
                }
                return threadCount;
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
    public void save(final Category categoryToSave) throws DataSourceException {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
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
                em.merge(thread);
                transaction.commit();
                return null;
            }
        });
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL", justification = "System.out will never be null, afaik")
    public void ban(final User user) {
        // TODO Auto-generated method stub
        System.out.println("TestDataSource.ban()");
        System.out.println("Banning, it does nothing!");
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
        try {
            return executeWithEntityManager(new Command<PostVote>() {
                @Override
                public PostVote execute(final EntityManager em) {
                    final TypedQuery<PostVote> query = em.createQuery(
                            "select v from PostVote v where v.voter = :voter "
                                    + "and v.post = :post", PostVote.class);
                    query.setParameter("voter", currentUser);
                    query.setParameter("post", post);
                    return query.getSingleResult();
                }
            });
        } catch (final NoResultException e) {
            final PostVote vote = new PostVote();
            vote.setPost(post);
            vote.setVoter(currentUser);
            return vote;
        }
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
    public void saveAsCurrentUser(final Post post) throws DataSourceException {
        post.setAuthor(currentUser);
        save(post);
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
    public void setRequest(final Object request) {
        // NOP - this data source is not interested in the request
    }

    @Override
    public DiscussionThread saveNewThread(final DiscussionThread newThread,
            final Post firstPost) throws DataSourceException {
        return executeWithEntityManager(new Command<DiscussionThread>() {
            @Override
            public DiscussionThread execute(final EntityManager em) {
                firstPost.setAuthor(currentUser);

                final EntityTransaction transaction = em.getTransaction();
                transaction.begin();
                final DiscussionThread mergedThread = em.merge(newThread);
                em.merge(firstPost);
                transaction.commit();

                return mergedThread;
            }
        });
    }
}
