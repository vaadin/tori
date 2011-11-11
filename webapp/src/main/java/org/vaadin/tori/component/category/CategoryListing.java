package org.vaadin.tori.component.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.data.entity.Category;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
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

    public enum Mode {

        /**
         * Displays only a single column with all details inside it.
         */
        SINGLE_COLUMN,

        /**
         * Displays normally with a separate column for each detail.
         */
        NORMAL;

    }

    private final CategoryTreeTable categoryTree;
    private final Mode mode;
    private final CssLayout layout;
    private final Component adminControls;

    public CategoryListing(final Mode listingMode) {
        this.mode = listingMode;
        layout = new CssLayout();

        categoryTree = new CategoryTreeTable();
        layout.addComponent(categoryTree);

        adminControls = createAdminControls();
        layout.addComponent(adminControls);
        setCompositionRoot(layout);
    }

    public void setCategories(final List<Category> categories) {
        categoryTree.removeAllItems();
        for (final Category category : categories) {
            categoryTree.addCategory(category, null);
        }
    }

    public void setAdminControlsVisible(final boolean visible) {
        adminControls.setVisible(visible);
    }

    private Component createAdminControls() {
        final HorizontalLayout adminControls = new HorizontalLayout();
        adminControls.addComponent(new Button("Create a new category"));
        adminControls.addComponent(new Button("Rearrange categories"));
        return adminControls;
    }

    private static long getUnreadPostCount(final Category category) {
        // TODO get the number of unread posts
        return 0;
    }

    private static long getThreadCount(final Category category) {
        return ToriApplication.getCurrent().getDataSource()
                .getThreadCount(category);
    }

    private class CategoryTreeTable extends TreeTable implements
            CollapseListener, ExpandListener {
        private static final String PROPERTY_ID_THREADS = "Threads";
        private static final String PROPERTY_ID_UNREAD = "Unread Threads";
        private static final String PROPERTY_ID_CATEGORY = "Category";

        public CategoryTreeTable() {
            setStyleName("categoryTree");

            // set container properties
            addContainerProperty(PROPERTY_ID_CATEGORY, Component.class, null);
            if (mode == Mode.NORMAL) {
                addContainerProperty(PROPERTY_ID_UNREAD, Integer.class, 0);
                addContainerProperty(PROPERTY_ID_THREADS, Integer.class, 0);
            }

            // set visual properties
            setWidth("100%");
            setSortDisabled(true);
            if (mode == Mode.NORMAL) {
                // icons
                setColumnIcon(PROPERTY_ID_UNREAD, new ThemeResource(
                        "images/icon-unread.png"));
                setColumnIcon(PROPERTY_ID_THREADS, new ThemeResource(
                        "images/icon-threads.png"));
                setColumnHeaderMode(COLUMN_HEADER_MODE_EXPLICIT);
            } else {
                setColumnHeaderMode(COLUMN_HEADER_MODE_HIDDEN);
            }
            addListener((CollapseListener) this);
            addListener((ExpandListener) this);
        }

        public void addCategory(final Category category, final Category parent) {
            final CategoryLayout categoryLayout = new CategoryLayout(category);

            final Item item = addItem(category);
            item.getItemProperty(PROPERTY_ID_CATEGORY).setValue(categoryLayout);
            if (mode == Mode.NORMAL) {
                item.getItemProperty(PROPERTY_ID_UNREAD).setValue(
                        getUnreadPostCount(category));
                item.getItemProperty(PROPERTY_ID_THREADS).setValue(
                        getThreadCount(category));
            }
            if (parent != null) {
                setParent(category, parent);
            }

            if (category.getSubCategories().isEmpty()) {
                setChildrenAllowed(category, false);
            } else {
                // all categories are collapsed by default
                setCollapsed(category, true);

                // recursively add all sub categories
                final List<Category> subCategories = ToriApplication
                        .getCurrent().getDataSource()
                        .getSubCategories(category);
                for (final Category subCategory : subCategories) {
                    addCategory(subCategory, category);
                }
            }
            setPageLength(this.size());
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
     * description. If the {@code CategoryListing} mode is
     * {@link Mode#SINGLE_COLUMN}, this layout also includes additional details.
     */
    private class CategoryLayout extends CssLayout {

        private final String CATEGORY_URL = ToriNavigator.ApplicationView.CATEGORIES
                .getUrl();

        public CategoryLayout(final Category category) {
            final long id = category.getId();
            final String name = category.getName();
            final String description = category.getDescription();

            setData(category);
            setStyleName("category");
            if (mode == Mode.SINGLE_COLUMN) {
                addComponent(createThreadCountLabel(getThreadCount(category),
                        getUnreadPostCount(category)));
            }
            addComponent(createCategoryLink(id, name));
            addComponent(createDescriptionLabel(description));
        }

        private Component createThreadCountLabel(final long threadCount,
                final long unreadPostCount) {
            final Label threadCountLabel = new Label(String.format(
                    "%d threads<br />%d new posts", threadCount,
                    unreadPostCount), Label.CONTENT_XHTML);
            threadCountLabel.setStyleName("threadCount");
            threadCountLabel.setWidth(null);
            return threadCountLabel;
        }

        private Component createDescriptionLabel(
                final String categoryDescription) {
            final Label description = new Label(categoryDescription);
            description.setStyleName("description");
            description.setWidth(null);
            return description;
        }

        private Component createCategoryLink(final long id, final String name) {
            final Link categoryLink = new Link();
            categoryLink.setCaption(name);
            categoryLink.setResource(new ExternalResource("#" + CATEGORY_URL
                    + "/" + id));
            categoryLink.setStyleName("categoryLink");
            categoryLink.setWidth(null);
            return categoryLink;
        }
    }
}
