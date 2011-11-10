package org.vaadin.tori.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.util.PersistenceUtil;

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
        return executeWithEntityManager(new Command<List<Category>>() {
            @Override
            public final List<Category> execute(final EntityManager em) {
                return em.createQuery(
                        "select c from Category c "
                                + "where c.parentCategory is null",
                        Category.class).getResultList();
            }
        });
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        return executeWithEntityManager(new Command<List<Category>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Category> execute(final EntityManager em) {
                return em
                        .createQuery(
                                "select c from Category c where c.parentCategory = :parent")
                        .setParameter("parent", category).getResultList();
            }
        });
    }

    @Override
    public List<DiscussionThread> getThreads(final Category category) {
        return executeWithEntityManager(new Command<List<DiscussionThread>>() {
            @Override
            public final List<DiscussionThread> execute(final EntityManager em) {
                return em.createQuery("select t from DiscussionThread t",
                        DiscussionThread.class).getResultList();
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
                for (final Category subCategory : category.getSubCategories()) {
                    threadCount += TestDataSource.this
                            .getThreadCount(subCategory);
                }
                return threadCount;
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
}
