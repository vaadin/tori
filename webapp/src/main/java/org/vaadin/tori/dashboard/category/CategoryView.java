package org.vaadin.tori.dashboard.category;

import java.util.List;

import org.vaadin.tori.data.entity.Category;

import com.github.peholmst.mvp4vaadin.View;

public interface CategoryView extends View {

    void displayCategories(List<Category> categories);

}
