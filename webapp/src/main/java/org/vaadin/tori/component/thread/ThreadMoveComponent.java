package org.vaadin.tori.component.thread;

import java.util.List;

import org.vaadin.tori.category.CategoryPresenter;
import org.vaadin.tori.component.ContextMenu;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

@SuppressWarnings("serial")
public class ThreadMoveComponent extends CustomComponent {

    private final CssLayout layout = new CssLayout();
    private final DiscussionThread thread;
    private final Tree categories;

    public ThreadMoveComponent(final DiscussionThread thread,
            final ContextMenu menu, final CategoryPresenter presenter) {
        this.thread = thread;
        setCompositionRoot(layout);
        setWidth("300px");

        final Panel panel = new Panel("Move Thread to Category...");
        panel.setWidth("100%");
        panel.setHeight("250px");
        panel.setScrollable(true);

        categories = createCategories(presenter);
        panel.addComponent(categories);

        layout.addComponent(panel);
        layout.addComponent(new NativeButton("Move Thread",
                new NativeButton.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        final Category newCategory = (Category) categories
                                .getValue();
                        presenter.move(thread, newCategory);
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

    private Tree createCategories(final CategoryPresenter presenter) {
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
            final CategoryPresenter presenter) {
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
