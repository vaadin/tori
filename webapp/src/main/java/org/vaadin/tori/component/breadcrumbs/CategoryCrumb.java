package org.vaadin.tori.component.breadcrumbs;

import java.util.List;

import org.vaadin.hene.splitbutton.SplitButton;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickEvent;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickListener;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonPopupVisibilityEvent;
import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.entity.Category;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Tree;

@SuppressWarnings("serial")
abstract class CategoryCrumb extends CustomComponent {

    public static class Clickable extends CategoryCrumb {
        public Clickable(final Category category,
                final CategorySelectionListener listener) {
            super(category, listener);
            setButtonClickListener(new SplitButtonClickListener() {
                @Override
                public void splitButtonClick(final SplitButtonClickEvent event) {
                    listener.selectCategory(category);
                }
            });
        }
    }

    public static class UnClickable extends CategoryCrumb {
        public UnClickable(final Category category,
                final CategorySelectionListener listener) {
            super(category, listener);
            addStyleName("unclickable");
        }
    }

    public interface CategorySelectionListener {
        void selectCategory(Category selectedCategory);
    }

    private final CategorySelectionListener listener;
    private final SplitButton crumb;

    public CategoryCrumb(final Category category,
            final CategorySelectionListener listener) {
        this.listener = listener;

        if (category == null) {
            throw new IllegalArgumentException("Trying to render the category "
                    + "part of the breadcrumbs, "
                    + "but the given category was null");
        }

        crumb = new SplitButton(category.getName());
        crumb.addPopupVisibilityListener(new SplitButton.SplitButtonPopupVisibilityListener() {
            @Override
            public void splitButtonPopupVisibilityChange(
                    final SplitButtonPopupVisibilityEvent event) {
                if (event.isPopupVisible()) {
                    final SplitButton splitButton = event.getSplitButton();
                    splitButton.setComponent(getCategoryPopup(category));
                }
            }
        });

        setCompositionRoot(crumb);
    }

    protected void setButtonClickListener(
            final SplitButtonClickListener listener) {
        crumb.addClickListener(listener);
    }

    private Component getCategoryPopup(final Category currentCategory) {
        final Tree tree = new Tree();
        tree.setImmediate(true);

        final List<Category> rootCategories = ToriApplication.getCurrent()
                .getDataSource().getRootCategories();
        for (final Category category : rootCategories) {
            addCategory(tree, category, null);
        }

        tree.addListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(final ItemClickEvent event) {
                if (listener != null) {
                    listener.selectCategory((Category) event.getItemId());
                }
                crumb.setPopupVisible(false);
            }
        });

        highlight(tree, currentCategory);

        return tree;
    }

    private static void highlight(final Tree tree,
            final Category currentCategory) {
        for (final Object itemId : tree.getItemIds()) {
            if (itemId.equals(currentCategory)) {
                /* make sure all the parents to this item are expanded */
                Object parentId = tree.getParent(itemId);
                while (parentId != null) {
                    tree.expandItem(parentId);
                    parentId = tree.getParent(parentId);
                }

                final String itemCaption = tree.getItemCaption(itemId);
                tree.setItemCaption(itemId, "> " + itemCaption);
                return;
            }
        }
    }

    private static void addCategory(final Tree tree, final Category category,
            final Category parent) {
        tree.addItem(category);
        tree.setItemCaption(category, category.getName());

        if (parent != null) {
            tree.setParent(category, parent);
        }

        final List<Category> subCategories = ToriApplication.getCurrent()
                .getDataSource().getSubCategories(category);
        if (subCategories.isEmpty()) {
            tree.setChildrenAllowed(category, false);
        } else {
            for (final Category subCategory : subCategories) {
                addCategory(tree, subCategory, category);
            }
        }
    }
}
