/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.component.category;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.component.ConfirmationDialog;
import org.vaadin.tori.component.ConfirmationDialog.ConfirmationListener;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.component.MenuPopup.ContextAction;
import org.vaadin.tori.component.MenuPopup.ContextComponentSwapper;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.component.category.CategoryListingPresenter.ContextMenuOperation;
import org.vaadin.tori.component.category.EditCategoryForm.EditCategoryListener;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TreeTable;

@SuppressWarnings("serial")
class CategoryTreeTable extends TreeTable {

	private static final Logger log = Logger.getLogger(CategoryTreeTable.class);

	private static final String PROPERTY_ID_THREADS = "Threads";
	private static final String PROPERTY_ID_UNREAD = "Unread Threads";
	private static final String PROPERTY_ID_CATEGORY = "Category";

	private CategoryListingPresenter presenter;
	private final Mode mode;

	public CategoryTreeTable(final Mode mode) {
		this.mode = mode;
		setStyleName("categoryTree");
		addStyleName(mode.toString().toLowerCase());

		// set container properties
		addContainerProperty(PROPERTY_ID_CATEGORY, Component.class, null);
		setColumnExpandRatio(PROPERTY_ID_CATEGORY, 1.0f);
		if (mode == Mode.NORMAL) {
			addContainerProperty(PROPERTY_ID_UNREAD, Long.class, 0);
			addContainerProperty(PROPERTY_ID_THREADS, Long.class, 0);

			setColumnWidth(PROPERTY_ID_UNREAD, 120);
			setColumnWidth(PROPERTY_ID_THREADS, 100);

			setColumnAlignment(PROPERTY_ID_UNREAD, Align.RIGHT);
			setColumnAlignment(PROPERTY_ID_THREADS, Align.RIGHT);
		}

		// set visual properties
		setWidth("100%");
		setPageLength(0);
		setSortEnabled(false);
		setDropHandler(new CategoryTreeDropHandler());
		if (mode == Mode.SINGLE_COLUMN) {
			setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
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
		if (item == null) {
			log.warn("Cannot add category " + category.getName() + ", id "
					+ category.getId() + " to the "
					+ CategoryTreeTable.class.getSimpleName() + ".");
			return;
		}
		item.getItemProperty(PROPERTY_ID_CATEGORY).setValue(categoryLayout);
		if (mode == Mode.NORMAL) {
			try {
				item.getItemProperty(PROPERTY_ID_UNREAD).setValue(
						presenter.getUnreadThreadCount(category));
			} catch (final DataSourceException e) {
				item.getItemProperty(PROPERTY_ID_UNREAD).setValue(0L);
			}

			try {
				item.getItemProperty(PROPERTY_ID_THREADS).setValue(
						presenter.getThreadCount(category));
			} catch (final DataSourceException e) {
				item.getItemProperty(PROPERTY_ID_THREADS).setValue(0L);
			}

		}
		if (parent != null) {
			setParent(category, parent);
		}

		List<Category> subCategories = new ArrayList<Category>();
		try {
			subCategories = presenter.getSubCategories(category);
		} catch (final DataSourceException e) {
			// NOP - arraylist is already initialized as empty
		}

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

		private final String CATEGORY_URL = "#"
				+ ToriNavigator.ApplicationView.CATEGORIES.getUrl()+ "/";

		public CategoryLayout(final Category category) {
			final long id = category.getId();
			final String name = category.getName();
			final String description = category.getDescription();

			setData(category);
			setStyleName("category");
			if (mode == Mode.SINGLE_COLUMN) {
				try {
					addComponent(createThreadCountLabel(
							presenter.getThreadCount(category),
							presenter.getUnreadThreadCount(category)));
				} catch (final DataSourceException e) {
					addComponent(new Label(
							"Something went wrong with the database :("));
				}
			}
			addComponent(createCategoryLink(id, name));
			addComponent(createDescriptionLabel(description));
			addComponent(createSettingsMenu(category));
		}

		private Component createThreadCountLabel(final long threadCount,
				final long unreadPostCount) {
			final Label threadCountLabel = new Label(String.format(
					"%d threads<br />%d new posts", threadCount,
					unreadPostCount), ContentMode.HTML);
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
			categoryLink.setResource(new ExternalResource(CATEGORY_URL + id));
			categoryLink.setStyleName("categoryLink");
			categoryLink.setWidth(null);
			return categoryLink;
		}

		private Component createSettingsMenu(final Category category) {
			final List<ContextMenuOperation> contextMenuOperations = presenter
					.getContextMenuOperations(category);

			final ContextMenu contextMenu = new ContextMenu();
			for (final ContextMenuOperation menuItem : contextMenuOperations) {
				switch (menuItem) {
				case EDIT:
					contextMenu.add("icon-edit", "Edit category",
							new ContextComponentSwapper() {
								@Override
								public Component swapContextComponent() {
									final EditCategoryListener listener = new EditCategoryListener() {
										@Override
										public void commit(final String name,
												final String description) {
											try {
												presenter.edit(category, name,
														description);
											} catch (final DataSourceException e) {
												/*
												 * FIXME: make sure that edits
												 * are reverted.
												 */
												Notification
														.show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
											}
										}

                                        @Override
                                        public void cancel() {
                                            contextMenu.close();
                                        }
									};
									return new EditCategoryForm(listener,
											category);
								}
							});
					break;
				case DELETE:
					contextMenu.add("icon-delete", "Delete category",
							new ContextComponentSwapper() {
								@Override
								public Component swapContextComponent() {
									final String title = String.format(String
											.format("Really delete category \"%s\" and all of its contents?",
													category.getName()));
									final String confirmCaption = "Yes, Delete";
									final String cancelCaption = "No, Cancel!";
									final ConfirmationListener listener = new ConfirmationListener() {

										@Override
										public void onConfirmed() {
											try {
												presenter.delete(category);
											} catch (final DataSourceException e) {
												Notification
														.show(DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
											}
											contextMenu.close();
										}

										@Override
										public void onCancel() {
											contextMenu.close();
										}
									};
									return new ConfirmationDialog(title,
											confirmCaption, cancelCaption,
											listener);

								}
							});
					break;
				case FOLLOW:
					contextMenu.add("icon-follow", "Follow category",
							new ContextAction() {
								@Override
								public void contextClicked() {
									presenter.follow(category);
								}
							});
					break;
				}
			}
			return contextMenu;
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