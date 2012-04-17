package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

@SuppressWarnings("serial")
public class ThreadMoveComponent extends CustomComponent {

    private final CssLayout layout = new CssLayout();
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "We ignore serialization")
    private final DiscussionThread thread;
    private Tree categories;

    public ThreadMoveComponent(final DiscussionThread thread,
            final ContextMenu menu, final CategoryPresenter presenter) {
        this.thread = thread;

        try {
            setCompositionRoot(layout);
            setWidth("300px");

            final Panel panel = new Panel("Move Thread to Category...");
            panel.setWidth("100%");
            panel.setHeight("250px");

            categories = createCategories(presenter);
            panel.addComponent(categories);

            layout.addComponent(panel);
            layout.addComponent(new NativeButton("Move Thread",
                    new NativeButton.ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            final Category newCategory = (Category) categories
                                    .getValue();

                            try {
                                presenter.move(thread, newCategory);
                            } catch (final DataSourceException e) {
                                getRoot()
                                        .showNotification(
                                                DataSourceException.BORING_GENERIC_ERROR_MESSAGE);
                            }

                            menu.close();
                        }
                    }));
            layout.addComponent(new NativeButton("Cancel",
                    new NativeButton.ClickListener() {
                        @Override
                        public void buttonClick(final ClickEvent event) {
                            menu.close();
                        }
                    }));
        }

        catch (final DataSourceException e1) {
            setCompositionRoot(new Label(
                    DataSourceException.BORING_GENERIC_ERROR_MESSAGE));
        }
    }

    private Tree createCategories(final CategoryPresenter presenter)
            throws DataSourceException {
        final Tree tree = new Tree();

        for (final Category rootCategory : presenter.getRootCategories()) {
            addCategory(tree, rootCategory, presenter);
            tree.expandItemsRecursively(rootCategory);
        }

        tree.setValue(thread.getCategory());
        tree.setNullSelectionAllowed(false);
        return tree;
    }

    private void addCategory(final Tree tree, final Category category,
            final CategoryPresenter presenter) throws DataSourceException {
        tree.addItem(category);
        tree.setItemCaption(category, category.getName());

        final List<Category> subCategories = presenter
                .getSubCategories(category);
        if (!subCategories.isEmpty()) {
            for (final Category subCategory : subCategories) {
                addCategory(tree, subCategory, presenter);
                tree.setParent(subCategory, category);
            }
        } else {
            tree.setChildrenAllowed(category, false);
        }
    }
}
