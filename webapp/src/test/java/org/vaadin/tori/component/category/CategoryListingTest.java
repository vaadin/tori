package org.vaadin.tori.component.category;

import static org.junit.Assert.assertEquals;
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
import org.vaadin.tori.exception.DataSourceException;
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
        when(mockAuthorizationService.mayRearrangeCategories()).thenReturn(
                false);
        when(mockAuthorizationService.mayEditCategories()).thenReturn(false);

        presenter.init();
        verify(mockView).setRearrangeVisible(false);
        verify(mockView).setCreateVisible(false);
    }

    @Test
    public void adminUser() {
        when(mockAuthorizationService.mayRearrangeCategories())
                .thenReturn(true);
        when(mockAuthorizationService.mayEditCategories()).thenReturn(true);

        presenter.init();
        verify(mockView).setRearrangeVisible(true);
        verify(mockView).setCreateVisible(true);
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
    public void applyRearrangementEmpty() throws DataSourceException {
        final Set<Category> modified = new HashSet<Category>(0);
        when(mockView.getModifiedCategories()).thenReturn(modified);

        // verify that the saveCategories method is not called when no
        // categories modified
        presenter.applyRearrangement();
        verify(mockDataSource, never()).save(modified);
    }

    @Test
    public void applyRearrangement() throws DataSourceException {
        final Set<Category> modified = new HashSet<Category>(1);
        modified.add(new Category());
        when(mockView.getModifiedCategories()).thenReturn(modified);

        presenter.applyRearrangement();
        verify(mockDataSource).save(modified);
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
        verify(mockDataSource).save(c);
    }

    @Test
    public void contextMenuItems() {
        final Category category = new Category();

        when(mockAuthorizationService.mayEdit(category)).thenReturn(true);
        when(mockAuthorizationService.mayDelete(category)).thenReturn(true);
        when(mockAuthorizationService.mayFollow(category)).thenReturn(true);
        assertEquals(3, presenter.getContextMenuOperations(category).size());

        when(mockAuthorizationService.mayEdit(category)).thenReturn(true);
        when(mockAuthorizationService.mayDelete(category)).thenReturn(false);
        when(mockAuthorizationService.mayFollow(category)).thenReturn(true);
        assertEquals(2, presenter.getContextMenuOperations(category).size());

        when(mockAuthorizationService.mayEdit(category)).thenReturn(false);
        when(mockAuthorizationService.mayDelete(category)).thenReturn(false);
        when(mockAuthorizationService.mayFollow(category)).thenReturn(true);
        assertEquals(1, presenter.getContextMenuOperations(category).size());

        when(mockAuthorizationService.mayEdit(category)).thenReturn(false);
        when(mockAuthorizationService.mayDelete(category)).thenReturn(false);
        when(mockAuthorizationService.mayFollow(category)).thenReturn(false);
        assertTrue(presenter.getContextMenuOperations(category).isEmpty());
    }
}
