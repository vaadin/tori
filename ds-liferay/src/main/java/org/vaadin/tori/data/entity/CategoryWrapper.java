package org.vaadin.tori.data.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.liferay.portlet.messageboards.model.MBCategory;

public class CategoryWrapper extends Category {

    private final MBCategory liferayCategory;

    private CategoryWrapper(final MBCategory liferayCategory) {
        this.liferayCategory = liferayCategory;
    }

    @Override
    public long getId() {
        return liferayCategory.getCategoryId();
    }

    @Override
    public String getName() {
        return liferayCategory.getName();
    }

    @Override
    public String getDescription() {
        return liferayCategory.getDescription();
    }

    @Override
    public List<Category> getSubCategories() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public Category getParentCategory() {
        // TODO
        return null;
    }

    @Override
    public int getDisplayOrder() {
        // TODO
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CategoryWrapper) {
            return liferayCategory
                    .equals(((CategoryWrapper) obj).liferayCategory);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return liferayCategory.hashCode();
    }

    public static Category wrap(final MBCategory categoryToWrap) {
        if (categoryToWrap != null) {
            return new CategoryWrapper(categoryToWrap);
        } else {
            return null;
        }
    }

    public static List<Category> wrap(final List<MBCategory> categoriesToWrap) {
        if (categoriesToWrap == null) {
            return Collections.emptyList();
        }

        final List<Category> categories = new ArrayList<Category>(
                categoriesToWrap.size());
        for (final MBCategory categoryToWrap : categoriesToWrap) {
            categories.add(wrap(categoryToWrap));
        }
        return categories;
    }
}
