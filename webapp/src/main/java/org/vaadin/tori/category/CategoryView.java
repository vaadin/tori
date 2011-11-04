package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;
import org.vaadin.tori.mvp.View;

public interface CategoryView extends View {
    void displaySubCategories(List<Category> subCategories);

    void displayThreads(List<Thread> threadsInCategory);
}
