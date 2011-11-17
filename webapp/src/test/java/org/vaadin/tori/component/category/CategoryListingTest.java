package org.vaadin.tori.component.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.service.AuthorizationService;

public class CategoryListingTest {

    private CategoryListingPresenter presenter;

    private CategoryListingView mockView;
    private DataSource mockDataSource;
    private AuthorizationService mockAuthorizationService;

    @Before
    public void setup() {
        // create mocks
        mockView = mock(CategoryListingView.class);
        mockDataSource = mock(DataSource.class);
        mockAuthorizationService = mock(AuthorizationService.class);

        // create the presenter to test
        presenter = new CategoryListingPresenter(mockDataSource,
                mockAuthorizationService);
        presenter.setView(mockView);
    }

    @Test
    public void nonAdminUser() {
        when(mockAuthorizationService.isCategoryAdministrator()).thenReturn(
                false);

        presenter.init();
        verify(mockView).setAdminControlsVisible(false);
    }

    @Test
    public void adminUser() {
        when(mockAuthorizationService.isCategoryAdministrator()).thenReturn(
                true);

        presenter.init();
        verify(mockView).setAdminControlsVisible(true);
    }

    @Test(expected = IllegalStateException.class)
    public void categoryTreeTableWithoutPresenter() {
        final CategoryTreeTable treeTable = new CategoryTreeTable(Mode.NORMAL);
        treeTable.addCategory(new Category(), null);
    }

    @Test
    public void categoryTreeTableNormalMode() {
        final CategoryTreeTable treeTable = new CategoryTreeTable(Mode.NORMAL);
        assertEquals(3, treeTable.getContainerPropertyIds().size());
    }

    @Test
    public void categoryTreeTableSingleColumnMode() {
        final CategoryTreeTable treeTable = new CategoryTreeTable(
                Mode.SINGLE_COLUMN);
        assertEquals(1, treeTable.getContainerPropertyIds().size());
    }

    @Test
    public void applyRearrangementEmpty() {
        final Set<Category> modified = new HashSet<Category>(0);
        when(mockView.getModifiedCategories()).thenReturn(modified);

        // verify that the saveCategories method is not called when no
        // categories modified
        presenter.applyRearrangement();
        verify(mockDataSource, never()).saveCategories(modified);
    }

    @Test
    public void applyRearrangement() {
        final Set<Category> modified = new HashSet<Category>(1);
        modified.add(new Category());
        when(mockView.getModifiedCategories()).thenReturn(modified);

        presenter.applyRearrangement();
        verify(mockDataSource).saveCategories(modified);
    }

    @Test
    public void maxDisplayOrder() {
        final List<Category> categories = new ArrayList<Category>();
        final Category min = new Category();
        min.setDisplayOrder(10);
        final Category mid = new Category();
        mid.setDisplayOrder(50);
        final Category max = new Category();
        max.setDisplayOrder(100);
        categories.add(min);
        categories.add(max);
        categories.add(mid);

        presenter.setCategories(categories);
        assertEquals(100, presenter.getMaxDisplayOrder());
    }

    @Test
    public void createNewCategory() throws Exception {
        final Category c = new Category();
        c.setName("New");
        c.setDescription("New Category");

        presenter.createNewCategory(c.getName(), c.getDescription());
        verify(mockDataSource).saveCategory(c);
    }

    @Test
    public void contextMenuItems() {
        final Category category = new Category();

        when(mockAuthorizationService.mayEditCategory(category)).thenReturn(
                true);
        when(mockAuthorizationService.mayDeleteCategory(category)).thenReturn(
                true);
        when(mockAuthorizationService.mayFollowCategory(category)).thenReturn(
                true);
        assertEquals(3, presenter.getContextMenuItems(category).size());
        assertTrue(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.EDIT_CATEGORY));
        assertTrue(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.DELETE_CATEGORY));
        assertTrue(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.FOLLOW_CATEGORY));

        when(mockAuthorizationService.mayEditCategory(category)).thenReturn(
                false);
        when(mockAuthorizationService.mayDeleteCategory(category)).thenReturn(
                true);
        when(mockAuthorizationService.mayFollowCategory(category)).thenReturn(
                true);
        assertEquals(2, presenter.getContextMenuItems(category).size());
        assertFalse(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.EDIT_CATEGORY));
        assertTrue(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.DELETE_CATEGORY));
        assertTrue(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.FOLLOW_CATEGORY));

        when(mockAuthorizationService.mayEditCategory(category)).thenReturn(
                false);
        when(mockAuthorizationService.mayDeleteCategory(category)).thenReturn(
                false);
        when(mockAuthorizationService.mayFollowCategory(category)).thenReturn(
                true);
        assertEquals(1, presenter.getContextMenuItems(category).size());
        assertFalse(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.EDIT_CATEGORY));
        assertFalse(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.DELETE_CATEGORY));
        assertTrue(presenter.getContextMenuItems(category).contains(
                CategoryContextMenuItem.FOLLOW_CATEGORY));
    }
}
