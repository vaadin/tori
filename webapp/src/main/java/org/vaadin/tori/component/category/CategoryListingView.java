package org.vaadin.tori.component.category;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.View;

interface CategoryListingView extends View {

    void setAdminControlsVisible(boolean visible);

    void displayCategories(List<Category> categories);
}
