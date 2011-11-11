package org.vaadin.tori.component.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * UI component for displaying a vertical hierarchical list of categories.
 */
@SuppressWarnings("serial")
public class CategoryListing extends
        AbstractView<CategoryListingView, CategoryListingPresenter> implements
        CategoryListingView {

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

    private final Mode mode;
    private CategoryTreeTable categoryTree;
    private CssLayout layout;
    private Component adminControls;
    private Button createCategoryButton;
    private Button rearrangeCategoriesButton;

    public CategoryListing(final Mode listingMode) {
        this.mode = listingMode;

        // Must call init here as this View is not
        // instantiated by ToriNavigator.
        init(null, ToriApplication.getCurrent());
    }

    @Override
    protected Component createCompositionRoot() {
        layout = new CssLayout();
        return layout;
    }

    @Override
    public void initView() {
        categoryTree = new CategoryTreeTable(mode);
        layout.addComponent(categoryTree);

        adminControls = createAdminControls();
        layout.addComponent(adminControls);
    }

    @Override
    public void setAdminControlsVisible(final boolean visible) {
        adminControls.setVisible(visible);
    }

    public void setCategories(final List<Category> categories) {
        categoryTree.removeAllItems();
        for (final Category category : categories) {
            categoryTree.addCategory(category, null);
        }
    }

    private Component createAdminControls() {
        rearrangeCategoriesButton = new Button("Rearrange categories",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        rearrangeCategories();
                    }
                });
        createCategoryButton = new Button("Create a new category",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(final ClickEvent event) {
                        createCategory();
                    }
                });

        final HorizontalLayout adminControls = new HorizontalLayout();
        adminControls.addComponent(createCategoryButton);
        adminControls.addComponent(rearrangeCategoriesButton);
        return adminControls;
    }

    private void rearrangeCategories() {
        categoryTree.setDraggingEnabled(true);
    }

    private void createCategory() {
        // TODO
    }

    @Override
    protected CategoryListingPresenter createPresenter() {
        final CategoryListingPresenter presenter = new CategoryListingPresenter(
                ToriApplication.getCurrent().getDataSource());
        categoryTree.setPresenter(presenter);
        return presenter;
    }

    @Override
    protected void navigationTo(final String requestedDataId) {
        // NOP
    }

}
