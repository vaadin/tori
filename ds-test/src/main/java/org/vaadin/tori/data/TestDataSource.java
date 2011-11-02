package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.entity.Category;

public class TestDataSource implements DataSource {

    private static final List<Category> categories = new ArrayList<Category>();

    static {
        initCategoryTestData();
    }

    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    private static void initCategoryTestData() {
        Category category = new Category();
        category.setName("News & Announcements");
        category.setDescription("Vaadin related news and announcements.");
        categories.add(category);

        category = new Category();
        category.setName("Using the Forums");
        category.setDescription("Guidelines and tips for using the forums.");
        categories.add(category);

        category = new Category();
        category.setName("Miscellaneous Discussion");
        category.setDescription("Any Vaadin related discussion.");
        categories.add(category);

        category = new Category();
        category.setName("General Help");
        category.setDescription("Discussions related to general Vaadin questions about installation and usage.");
        categories.add(category);

        category = new Category();
        category.setName("UI Components");
        category.setDescription("Discussion about Vaadin UI components and their usage.");
        categories.add(category);

        category = new Category();
        category.setName("Data Bindings");
        category.setDescription("Discussion about Vaadin data binding APIs.");
        categories.add(category);

        category = new Category();
        category.setName("Themes");
        category.setDescription("How to change looks of Vaadin widgets and create new themes.");
        categories.add(category);

        category = new Category();
        category.setName("Vaadin Testbench");
        category.setDescription("Discussion related to the Vaadin Testbench.");
        categories.add(category);

        category = new Category();
        category.setName("Add-ons");
        category.setDescription("Discussion about Vaadin add-ons.");
        categories.add(category);
    }

}
