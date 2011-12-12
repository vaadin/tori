package org.vaadin.tori.dashboard;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.View;

public interface DashboardView extends View {

    void displayCategories(List<Category> categories);

    /**
     * An unrecoverable error occurred, which can't be shown in any other way
     * than utter chaos, mayhem and pandemonium
     */
    void panic();

}
