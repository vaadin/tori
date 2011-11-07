package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;
import org.vaadin.tori.data.util.PersistenceUtil;

import com.google.common.collect.Lists;

public class TestDataSource implements DataSource {

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
