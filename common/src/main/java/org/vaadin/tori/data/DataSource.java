package org.vaadin.tori.data;

import java.util.List;

import org.vaadin.tori.data.entity.Category;

public interface DataSource {

    /**
     * Returns a list of all {@link Category} instances.
     * 
     * @return all {@link Category} instances.
     */
    List<Category> getAllCategories();

}
