package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;

public class LiferayDataSource implements DataSource {

    @Override
    public List<Category> getRootCategories() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<Thread> getThreads(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Category getCategory(final String categoryId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
