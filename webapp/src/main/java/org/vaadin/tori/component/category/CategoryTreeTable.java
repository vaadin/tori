package org.vaadin.tori.component.category;

import java.util.List;

import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.ContextMenu.Builder;
import org.vaadin.tori.component.ContextMenu.ContextAction;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.component.category.CategoryListingPresenter.ContextMenuOperation;
import org.vaadin.tori.data.entity.Category;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TreeTable;

@SuppressWarnings("serial")
class CategoryTreeTable extends TreeTable {

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
        setPageLength(0);
        setSortDisabled(true);
        setDropHandler(new CategoryTreeDropHandler());
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

        final List<Category> subCategories = presenter
                .getSubCategories(category);
        if (subCategories.isEmpty()) {
            setChildrenAllowed(category, false);
        } else {
            // all categories are collapsed by default
            setCollapsed(category, true);

            // recursively add all sub categories
            for (final Category subCategory : subCategories) {
                addCategory(subCategory, category);
            }
        }
    }

    public void setDraggingEnabled(final boolean enabled) {
        if (enabled) {
            setDragMode(TableDragMode.ROW);
            addStyleName("rearranging");
        } else {
            setDragMode(TableDragMode.NONE);
            removeStyleName("rearranging");
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
                addComponent(createThreadCountLabel(
                        presenter.getThreadCount(category),
                        presenter.getUnreadThreadCount(category)));
            }
            addComponent(createCategoryLink(id, name));
            addComponent(createDescriptionLabel(description));
            addComponent(createSettingsMenu(category));
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

        private Component createSettingsMenu(final Category category) {
            final List<ContextMenuOperation> contextMenuOperations = presenter
                    .getContextMenuOperations(category);

            final Builder builder = new ContextMenu.Builder();
            for (final ContextMenuOperation menuItem : contextMenuOperations) {
                switch (menuItem) {
                case EDIT:
                    builder.add(new ThemeResource("images/icon-edit.png"),
                            "Edit category", new ContextAction() {
                                @Override
                                public void contextClicked() {
                                    presenter.edit(category);
                                }
                            });
                    break;
                case DELETE:
                    builder.add(new ThemeResource("images/icon-delete.png"),
                            "Delete category" + '\u2026', new ContextAction() {
                                @Override
                                public void contextClicked() {
                                    presenter.confirmDelete(category);
                                }
                            });
                    break;
                case FOLLOW:
                    builder.add(new ThemeResource("images/icon-pin.png"),
                            "Follow category", new ContextAction() {
                                @Override
                                public void contextClicked() {
                                    presenter.follow(category);
                                }
                            });
                    break;
                }
            }
            return builder.build();
        }
    }

    private class CategoryTreeDropHandler implements DropHandler {

        @Override
        public void drop(final DragAndDropEvent event) {
            final Transferable t = event.getTransferable();

            // check that we're dragging within the same CategoryTreeTable
            if (t.getSourceComponent() != CategoryTreeTable.this) {
                return;
            }

            final AbstractSelectTargetDetails targetDetails = (AbstractSelectTargetDetails) event
                    .getTargetDetails();

            // get source and target itemIds
            final Object draggedItemId = t.getData("itemId");
            final Object targetItemId = targetDetails.getItemIdOver();

            final HierarchicalContainer container = (HierarchicalContainer) getContainerDataSource();
            final Object parentItemId = container.getParent(targetItemId);
            container.setParent(draggedItemId, parentItemId);

            // move the dragged item in the container according to the drop
            // location
            final VerticalDropLocation dropLocation = targetDetails
                    .getDropLocation();
            if (dropLocation == VerticalDropLocation.MIDDLE) {
                // middle -> make it child
                container.setChildrenAllowed(targetItemId, true);
                container.setParent(draggedItemId, targetItemId);
            } else if (dropLocation == VerticalDropLocation.TOP) {
                // top -> make it previous
                container.moveAfterSibling(targetItemId, draggedItemId);
            } else if (dropLocation == VerticalDropLocation.BOTTOM) {
                // bottom -> make it next
                container.moveAfterSibling(draggedItemId, targetItemId);
            }
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }

    }
}