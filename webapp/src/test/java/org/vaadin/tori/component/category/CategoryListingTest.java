package org.vaadin.tori.component.category;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.tori.component.category.CategoryListing.Mode;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.User;

public class CategoryListingTest {

    private CategoryListingPresenter presenter;

    private CategoryListingView mockView;
    private DataSource mockDataSource;

    @Before
    public void setup() {
        // create mocks
        mockView = mock(CategoryListingView.class);
        mockDataSource = mock(DataSource.class);

        // create the presenter to test
        presenter = new CategoryListingPresenter(mockDataSource);
        presenter.setView(mockView);
    }

    @Test
    public void nonAdminUser() {
        final User currentUser = new User();
        when(mockDataSource.isAdministrator(currentUser)).thenReturn(false);

        presenter.setCurrentUser(currentUser);
        presenter.init();
        verify(mockView).setAdminControlsVisible(false);
    }

    @Test
    public void adminUser() {
        final User currentUser = new User();
        when(mockDataSource.isAdministrator(currentUser)).thenReturn(true);

        presenter.setCurrentUser(currentUser);
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
}
