package org.vaadin.tori.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.data.util.PersistenceUtil;
import org.vaadin.tori.service.post.PostReport;

public class TestDataSource implements DataSource {

    public TestDataSource() {
        if (isEmptyDatabase()) {
            initDatabaseWithTestData();
        }
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
        return getSubCategories(null);
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
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
    public List<Post> getPosts(final Thread thread) {
        return executeWithEntityManager(new Command<List<Post>>() {
            @Override
            public List<Post> execute(final EntityManager em) {
                final TypedQuery<Post> query = em.createQuery(
                        "select p from Post p where p.thread = :thread "
                                + "orderby asc time", Post.class);
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
    public void ban(final User user) {
        // TODO Auto-generated method stub
        System.err.println("TestDataSource.ban()");
        System.err.println("Banning, it does nothing!");
    }

}
