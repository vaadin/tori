package org.vaadin.tori.category;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.exception.DataSourceException;

public class CategoryPresenterTest {

    private CategoryPresenter presenter;

    private CategoryView mockView;
    private DataSource mockDataSource;

    @Before
    public void setup() {
        // create mocks
        mockView = mock(CategoryView.class);
        mockDataSource = mock(DataSource.class);

        // create the presenter to test
        presenter = new CategoryPresenter(mockDataSource, null);
        presenter.setView(mockView);
    }

    @Test
    public void invalidCategoryIdFormat() {
        presenter.setCurrentCategoryById("qwerty");
        verify(mockView).displayCategoryNotFoundError("qwerty");
    }

    @Test
    public void nonExistingCategoryId() throws DataSourceException {
        when(mockDataSource.getCategory(-1)).thenReturn(null);

        presenter.setCurrentCategoryById("-1");
        verify(mockView).displayCategoryNotFoundError("-1");
    }

    @Test
    public void recentPostsCategory() throws DataSourceException {
        presenter.setCurrentCategoryById("recent");
        assertEquals(SpecialCategories.RECENT_POSTS.getInstance(),
                presenter.getCurrentCategory());
        assertFalse(presenter.userMayStartANewThread());

        verify(mockDataSource).getRecentPosts();
    }

    @Test
    public void myPostsCategory() throws DataSourceException {
        presenter.setCurrentCategoryById("myposts");
        assertEquals(SpecialCategories.MY_POSTS.getInstance(),
                presenter.getCurrentCategory());
        assertFalse(presenter.userMayStartANewThread());

        verify(mockDataSource).getMyPosts();
    }

    @Test
    public void existingCategoryId() throws DataSourceException {
        final Category category = new Category();
        final List<Category> subCategories = Collections.emptyList();
        final List<org.vaadin.tori.data.entity.DiscussionThread> threads = Collections
                .emptyList();

        when(mockDataSource.getCategory(1)).thenReturn(category);
        when(mockDataSource.getSubCategories(category)).thenReturn(
                subCategories);
        when(mockDataSource.getThreads(category)).thenReturn(threads);

        // assert that that category is actually set
        presenter.setCurrentCategoryById("1");
        assertEquals(category, presenter.getCurrentCategory());

        // verify that the category (and subcategories) is displayed)
        verify(mockView).displaySubCategories(subCategories);
        verify(mockView).displayThreads(threads);
    }

}
