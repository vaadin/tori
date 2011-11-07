package org.vaadin.tori.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;
import org.vaadin.tori.data.util.PersistenceUtil;

import com.google.common.collect.Lists;

public class TestDataSource implements DataSource {

    public TestDataSource() {
        if (isEmptyDatabase()) {
            initDatabaseWithTestData();
        }
    }

    private void initDatabaseWithTestData() {
        // read the "test-data.sql" file
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader()
                        .getResourceAsStream("test-data.sql")));
        final EntityManager entityManager = PersistenceUtil.getEntityManager();
        try {
            String sqlLine;
            while ((sqlLine = reader.readLine()) != null) {
                if (!sqlLine.startsWith("#")) {
                    // not a comment line -> run as native query
                    runNativeSql(entityManager, sqlLine);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            entityManager.close();
        }
    }

    private void runNativeSql(final EntityManager entityManager,
            final String sql) {
        entityManager.getTransaction().begin();
        try {
            entityManager.createNativeQuery(sql).executeUpdate();
            entityManager.getTransaction().commit();
        } catch (final Exception e) {
            entityManager.getTransaction().rollback();
        }
    }

    private boolean isEmptyDatabase() {
        final TypedQuery<Long> q = PersistenceUtil.getEntityManager()
                .createQuery("select count(c) from Category c", Long.class);
        final Long categoryCount = q.getSingleResult();
        return categoryCount == 0;
    }

    @Override
    public List<Category> getRootCategories() {
        return PersistenceUtil.getEntityManager()
                .createQuery("select c from Category c", Category.class)
                .getResultList();
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        final Category sub1 = new Category();
        sub1.setId(101);
        sub1.setName("SubCategory 1");
        sub1.setDescription("A sub-category that pops up everywhere");

        final Category sub2 = new Category();
        sub2.setId(102);
        sub2.setName("SubCategory 2");
        sub2.setDescription("Another sub-category that pops up everywhere");

        return Lists.newArrayList(sub1, sub2);
    }

    @Override
    public List<Thread> getThreads(final Category category) {
        return PersistenceUtil.getEntityManager()
                .createQuery("select t from Thread t", Thread.class)
                .getResultList();
    }

    @Override
    public Category getCategory(final String categoryId) {
        return PersistenceUtil.getEntityManager().find(Category.class,
                Long.parseLong(categoryId));
    }
}
