package org.vaadin.tori.dashboard;

import java.util.List;

import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.View;

public interface DashboardView extends View {

    void displayCategories(List<Category> categories);

}
