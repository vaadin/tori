package org.vaadin.tori.category;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.entity.Category;

public enum SpecialCategory {

    RECENT_POSTS("Recent Posts", "recent"), MY_POSTS("My Posts", "myposts");

    private Category categoryInstance;
    private String id;

    private SpecialCategory(final String name, final String id) {
        categoryInstance = new Category();
        categoryInstance.setName(name);
        this.id = id;
    }

    public Category getInstance() {
        return categoryInstance;
    }

    public String getId() {
        return id;
    }

    public static boolean isSpecialCategory(final Category category) {
        for (final SpecialCategory specialCategory : values()) {
            if (specialCategory.getInstance() == category) {
                return true;
            }
        }
        return false;
    }

    public static List<Category> getCategories() {
        final List<Category> specialCategories = new ArrayList<Category>();
        for (final SpecialCategory specialCategory : values()) {
            specialCategories.add(specialCategory.getInstance());
        }
        return specialCategories;
    }
}
