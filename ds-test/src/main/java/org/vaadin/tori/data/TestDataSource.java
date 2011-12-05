package org.vaadin.tori.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.vaadin.tori.service.post.PostReport;

import edu.umd.cs.findbugs.annotations.NonNull;

public class TestDataSource implements DataSource {

    private static final long CURRENT_USER_ID = 3;
    private final User currentUser;

    public TestDataSource() {
        if (isEmptyDatabase()) {
            initDatabaseWithTestData();
        }

        currentUser = getUser(CURRENT_USER_ID);
    }

    private User getUser(final long userId) {
        return executeWithEntityManager(new Command<User>() {
            @Override
            public final User execute(final EntityManager em) {
                return em.find(User.class, userId);
            }
        });
    }

    private void initDatabaseWithTestData() {
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

    private boolean isEmptyDatabase() {
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
    public List<Category> getRootCategories() {
        return _getSubCategories(null);
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        return _getSubCategories(category);
    }

    @NonNull
    private List<Category> _getSubCategories(final Category category) {
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
    public List<DiscussionThread> getThreads(final Category category) {
        return executeWithEntityManager(new Command<List<DiscussionThread>>() {
            @Override
            public final List<DiscussionThread> execute(final EntityManager em) {
                final TypedQuery<DiscussionThread> query = em
                        .createQuery(
                                "select t from DiscussionThread t where t.category = :category",
                                DiscussionThread.class);
                query.setParameter("category", category);
                return query.getResultList();
            }
        });
    }

    @Override
    public Category getCategory(final long categoryId) {
        return executeWithEntityManager(new Command<Category>() {
            @Override
            public final Category execute(final EntityManager em) {
                return em.find(Category.class, categoryId);
            }
        });
    }

    @Override
    public long getThreadCount(final Category category) {
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
                final List<Category> subCategories = getSubCategories(category);
                for (final Category subCategory : subCategories) {
                    threadCount += getThreadCount(subCategory);
                }
                return threadCount;
            }
        });
    }

    @Override
    public DiscussionThread getThread(final long threadId) {
        return executeWithEntityManager(new Command<DiscussionThread>() {
            @Override
            public final DiscussionThread execute(final EntityManager em) {
                return em.find(DiscussionThread.class, threadId);
            }
        });
    }

    @Override
    public List<Post> getPosts(final DiscussionThread thread) {
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
    private static <T> T executeWithEntityManager(final Command<T> command) {
        final EntityManager em = PersistenceUtil.createEntityManager();
        try {
            return command.execute(em);
        } finally {
            em.close();
        }
    }

    private static interface Command<T> {
        T execute(EntityManager em);
    }

    @Override
    public void save(final Iterable<Category> categoriesToSave) {
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
    public void save(final Category categoryToSave) {
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
    public void delete(final Category categoryToDelete) {
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
    public void save(final Post post) {
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
    public void follow(final DiscussionThread thread) {
        if (!isFollowing(thread)) {
            final org.vaadin.tori.data.entity.Following following = new org.vaadin.tori.data.entity.Following();
            following.setFollower(currentUser);
            following.setThread(thread);
            save(following);
        }
    }

    @SuppressWarnings("deprecation")
    private void save(final org.vaadin.tori.data.entity.Following following) {
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
    public void unFollow(final DiscussionThread thread) {
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
    public boolean isFollowing(final DiscussionThread thread) {
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
    public void delete(final Post post) {
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
    public PostVote getPostVote(final Post post) {
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
    public void upvote(final Post post) {
        final PostVote vote = getPostVote(post);
        vote.setUpvote();
        save(vote);
    }

    @Override
    public void downvote(final Post post) {
        final PostVote vote = getPostVote(post);
        vote.setDownvote();
        save(vote);
    }

    public void save(final PostVote vote) {
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
    public void removeUserVote(final Post post) {
        delete(getPostVote(post));
    }

    private void delete(final PostVote postVote) {
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
    public long getScore(final Post post) {
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
    public void saveAsCurrentUser(final Post post) {
        post.setAuthor(currentUser);
        save(post);
    }

    @Override
    public void move(final DiscussionThread thread,
            final Category destinationCategory) {
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
    public void sticky(final DiscussionThread thread) {
        thread.setSticky(true);
        save(thread);
    }

    @Override
    public void unsticky(final DiscussionThread thread) {
        thread.setSticky(false);
        save(thread);
    }

    @Override
    public void lock(final DiscussionThread thread) {
        thread.setLocked(true);
        save(thread);
    }

    @Override
    public void unlock(final DiscussionThread thread) {
        thread.setLocked(false);
        save(thread);
    }

    private static void save(final DiscussionThread thread) {
        executeWithEntityManager(new Command<Void>() {
            @Override
            public Void execute(final EntityManager em) {
                final EntityTransaction t = em.getTransaction();
                t.begin();
                em.merge(thread);
                t.commit();
                return null;
            }
        });
    }

    @Override
    public void delete(final DiscussionThread thread) {
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

                // remove all votes for posts inside thread.
                for (final Post post : getPosts(mergedThread)) {
                    // "in" is not supported :(
                    final Query postDelete = em
                            .createQuery("delete from PostVote where post = :post");
                    postDelete.setParameter("post", post);
                    postDelete.executeUpdate();
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
            final Post firstPost) {
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
