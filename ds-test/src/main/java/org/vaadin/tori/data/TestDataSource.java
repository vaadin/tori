package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;

import com.google.common.collect.Lists;

public class TestDataSource implements DataSource {

    private static final List<Category> categories = new ArrayList<Category>();
    private static final List<Thread> threads = new ArrayList<Thread>();

    static {
        initCategoryTestData();
        initThreadTestData();
    }

    @Override
    public List<Category> getRootCategories() {
        return categories;
    }

    private static void initThreadTestData() {
        threads.add(new Thread("First Thread"));
        threads.add(new Thread("Second Thread"));
        threads.add(new Thread("Third Thread"));
        threads.add(new Thread("Fourth Thread"));
        threads.add(new Thread("Fifth Thread"));
        threads.add(new Thread("Sixth Thread"));
        threads.add(new Thread("Seventh Thread"));
        threads.add(new Thread("Last Thread"));
    }

    private static void initCategoryTestData() {
        Category category = new Category();
        category.setId(1);
        category.setName("News & Announcements");
        category.setDescription("Vaadin related news and announcements.");
        categories.add(category);

        category = new Category();
        category.setId(2);
        category.setName("Using the Forums");
        category.setDescription("Guidelines and tips for using the forums.");
        categories.add(category);

        category = new Category();
        category.setId(3);
        category.setName("Miscellaneous Discussion");
        category.setDescription("Any Vaadin related discussion.");
        categories.add(category);

        category = new Category();
        category.setId(4);
        category.setName("General Help");
        category.setDescription("Discussions related to general Vaadin questions about installation and usage.");
        categories.add(category);

        category = new Category();
        category.setId(5);
        category.setName("UI Components");
        category.setDescription("Discussion about Vaadin UI components and their usage.");
        categories.add(category);

        category = new Category();
        category.setId(6);
        category.setName("Data Bindings");
        category.setDescription("Discussion about Vaadin data binding APIs.");
        categories.add(category);

        category = new Category();
        category.setId(7);
        category.setName("Themes");
        category.setDescription("How to change looks of Vaadin widgets and create new themes.");
        categories.add(category);

        category = new Category();
        category.setId(8);
        category.setName("Vaadin Testbench");
        category.setDescription("Discussion related to the Vaadin Testbench.");
        categories.add(category);

        category = new Category();
        category.setId(9);
        category.setName("Add-ons");
        category.setDescription("Discussion about Vaadin add-ons.");
        categories.add(category);
    }

    @Override
    public List<Category> getSubCategories(final Category category) {
        final Category sub1 = new Category();
        sub1.setId(101);
        sub1.setName("SubCategory 1");
        sub1.setDescription("A sub-category that pops up everywhere");

        final Category sub2 = new Category();
        sub2.setId(102);
        sub2.setName("SubCategory 2");
        sub2.setDescription("Another sub-category that pops up everywhere");

        return Lists.newArrayList(sub1, sub2);
    }

    @Override
    public List<Thread> getThreads(final Category category) {
        return threads;
    }

    @Override
    public Category getCategory(final String categoryId) {
        final Category convertedCategory = new Category();
        convertedCategory.setId(Integer.parseInt(categoryId));
        convertedCategory.setName("Category Converted from ID");
        convertedCategory
                .setDescription("This category has been created with the id "
                        + categoryId);
        return convertedCategory;
    }
}
