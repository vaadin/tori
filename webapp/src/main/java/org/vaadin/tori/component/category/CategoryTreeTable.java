package org.vaadin.tori.component.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.data.entity.Category;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;

@SuppressWarnings("serial")
class CategoryTreeTable extends TreeTable implements CollapseListener,
        ExpandListener {
    private static final String PROPERTY_ID_THREADS = "Threads";
    private static final String PROPERTY_ID_UNREAD = "Unread Threads";
    private static final String PROPERTY_ID_CATEGORY = "Category";

    private CategoryListingPresenter presenter;
    private final Mode mode;

    public CategoryTreeTable(final Mode mode) {
        this.mode = mode;
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

    public void setPresenter(final CategoryListingPresenter presenter) {
        this.presenter = presenter;
    }

    public void addCategory(final Category category, final Category parent) {
        if (presenter == null) {
            throw new IllegalStateException(
                    "Presenter must be set before adding any categories.");
        }

        final CategoryLayout categoryLayout = new CategoryLayout(category);

        final Item item = addItem(category);
        item.getItemProperty(PROPERTY_ID_CATEGORY).setValue(categoryLayout);
        if (mode == Mode.NORMAL) {
            item.getItemProperty(PROPERTY_ID_UNREAD).setValue(
                    presenter.getUnreadThreadCount(category));
            item.getItemProperty(PROPERTY_ID_THREADS).setValue(
                    presenter.getThreadCount(category));
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
            final List<Category> subCategories = ToriApplication.getCurrent()
                    .getDataSource().getSubCategories(category);
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
                addComponent(createThreadCountLabel(
                        presenter.getThreadCount(category),
                        presenter.getUnreadThreadCount(category)));
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