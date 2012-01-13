package org.vaadin.tori.category;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.entity.Category;

public enum SpecialCategories {

    RECENT_POSTS("Recent Posts"), MY_POSTS("My Posts");

    private Category categoryInstance;

    private SpecialCategories(final String name) {
        categoryInstance = new Category();
        categoryInstance.setName(name);
    }

    public Category getInstance() {
        return categoryInstance;
    }

    public static boolean isSpecialCategory(final Category category) {
        for (final SpecialCategories specialCategory : values()) {
            if (specialCategory.getInstance() == category) {
                return true;
            }
        }
        return false;
    }

    public static List<Category> getCategories() {
        final List<Category> specialCategories = new ArrayList<Category>();
        for (final SpecialCategories specialCategory : values()) {
            specialCategories.add(specialCategory.getInstance());
        }
        return specialCategories;
    }
}
