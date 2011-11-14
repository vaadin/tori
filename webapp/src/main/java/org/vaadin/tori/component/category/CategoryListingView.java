package org.vaadin.tori.component.category;

import java.util.List;
import java.util.Set;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.View;

interface CategoryListingView extends View {

    /**
     * Displays or hides the administrative controls.
     * 
     * @param visible
     */
    void setAdminControlsVisible(boolean visible);

    /**
     * Displays all given categories in the hierarchical CategoryListing.
     * 
     * @param categories
     *            {@link Category Categories} to display.
     */
    void displayCategories(List<Category> categories);

    /**
     * Returns a {@link Set} of modified {@link Category Categories}.
     * 
     * @return a {@link Set} of modified {@link Category Categories}.
     */
    Set<Category> getModifiedCategories();
}
