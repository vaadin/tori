package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;

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
    public List<DiscussionThread> getThreads(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Category getCategory(final long categoryId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public long getThreadCount(final Category category) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
