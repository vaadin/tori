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

package org.vaadin.tori.thread;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.tori.TestUtil;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.view.thread.ThreadPresenter;

public class TestThreadPresenter {

    private ThreadPresenter presenter;

    @Before
    public void setup() {
        // create mocks
        final ToriApiLoader apiLoader = TestUtil.mockApiLoader();

        // create the presenter to test
        presenter = new ThreadPresenter(null) {
            @Override
            protected ToriApiLoader getApiLoader() {
                return apiLoader;
            }
        };
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
