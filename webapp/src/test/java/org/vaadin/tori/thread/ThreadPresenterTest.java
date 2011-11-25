package org.vaadin.tori.thread;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.service.AuthorizationService;

public class ThreadPresenterTest {

    private ThreadPresenter presenter;
    private DataSource mockDataSource;
    private AuthorizationService mockAuthorizationService;

    @Before
    public void setup() {
        // create mocks
        mockDataSource = mock(DataSource.class);
        mockAuthorizationService = mock(AuthorizationService.class);

        // create the presenter to test
        presenter = new ThreadPresenter(mockDataSource,
                mockAuthorizationService);
    }

    @Test
    public void stripTagsTest() {
        assertEquals("foo bar baz",
                presenter.stripTags("<strong>foo bar baz</strong>"));
        assertEquals("foo bar baz", presenter.stripTags("foo <b>bar</b> baz"));
        assertEquals("foo bar baz",
                presenter.stripTags("<img src=\"foo.jpg\"/>foo bar baz"));
        assertEquals("my code: &lt;code&gt;",
                presenter.stripTags("my code: <code>&lt;code&gt;</code>"));
    }

}
