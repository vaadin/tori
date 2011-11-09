package org.vaadin.tori.component;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.data.entity.Category;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;

/**
 * UI component for displaying a vertical hierarchical list of categories.
 */
@SuppressWarnings("serial")
public class CategoryListing extends CustomComponent {

    private final CategoryTreeTable categoryTree;

    public CategoryListing() {
        categoryTree = new CategoryTreeTable();
        setCompositionRoot(categoryTree);
    }

    public void setCategories(final List<Category> categories) {
        categoryTree.removeAllItems();
        for (final Category category : categories) {
            categoryTree.addCategory(category, null);
        }
    }

    private static class CategoryTreeTable extends TreeTable implements
            CollapseListener, ExpandListener {
        private static final String PROPERTY_ID_THREADS = "Threads";
        private static final String PROPERTY_ID_UNREAD = "Unread Threads";
        private static final String PROPERTY_ID_CATEGORY = "Category";

        public CategoryTreeTable() {
            setStyleName("categoryTree");

            // set container properties
            addContainerProperty(PROPERTY_ID_CATEGORY, Component.class, null);
            addContainerProperty(PROPERTY_ID_UNREAD, Integer.class, 0);
            addContainerProperty(PROPERTY_ID_THREADS, Integer.class, 0);

            // set visual properties
            setWidth("100%");
            setColumnHeaderMode(COLUMN_HEADER_MODE_HIDDEN);
            addListener((CollapseListener) this);
            addListener((ExpandListener) this);
        }

        public void addCategory(final Category category, final Category parent) {
            final CategoryLayout categoryLayout = new CategoryLayout(category);
            final long threadCount = getThreadCount(category);
            final long unreadThreadCount = getUnreadThreadCount(category);

            final Item item = addItem(category);
            item.getItemProperty(PROPERTY_ID_CATEGORY).setValue(categoryLayout);
            item.getItemProperty(PROPERTY_ID_UNREAD).setValue(threadCount);
            item.getItemProperty(PROPERTY_ID_THREADS).setValue(
                    unreadThreadCount);
            if (parent != null) {
                setParent(category, parent);
            }

            if (category.getSubCategories().isEmpty()) {
                setChildrenAllowed(category, false);
            } else {
                // all categories are collapsed by default
                setCollapsed(category, true);

                // recursively add all sub categories
                for (final Category subCategory : category.getSubCategories()) {
                    addCategory(subCategory, category);
                }
            }
            setPageLength(this.size());
        }

        private long getUnreadThreadCount(final Category category) {
            // TODO get the number of unread threads
            return getThreadCount(category);
        }

        private long getThreadCount(final Category category) {
            return ToriApplication.getCurrent().getDataSource()
                    .getThreadCount(category);
        }

        @Override
        public void nodeExpand(final ExpandEvent event) {
            setPageLength(this.size());
        }

        @Override
        public void nodeCollapse(final CollapseEvent event) {
            setPageLength(this.size());
        }

    }

    /**
     * Simple layout displaying the category name as a link and the category
     * description.
     */
    private static class CategoryLayout extends CssLayout {

        private static final String CATEGORY_URL = ToriNavigator.ApplicationView.CATEGORIES
                .getUrl();

        public CategoryLayout(final Category category) {
            final long id = category.getId();
            final String name = category.getName();
            final String description = category.getDescription();

            setData(category);
            setStyleName("category");
            addComponent(createCategoryLink(id, name));
            addComponent(createDescriptionLabel(description));
        }

        private Component createDescriptionLabel(
                final String categoryDescription) {
            final Label description = new Label(categoryDescription);
            description.setStyleName("description");
            return description;
        }

        private Component createCategoryLink(final long id, final String name) {
            final Link categoryLink = new Link();
            categoryLink.setCaption(name);
            categoryLink.setResource(new ExternalResource("#" + CATEGORY_URL
                    + "/" + id));
            categoryLink.setStyleName("categoryLink");
            return categoryLink;
        }
    }
}
