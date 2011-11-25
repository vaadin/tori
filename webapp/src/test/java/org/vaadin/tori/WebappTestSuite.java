package org.vaadin.tori;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.vaadin.tori.category.CategoryPresenterTest;
import org.vaadin.tori.component.category.CategoryListingTest;
import org.vaadin.tori.thread.ThreadPresenterTest;

@RunWith(Suite.class)
@SuiteClasses({ CategoryPresenterTest.class, CategoryListingTest.class,
        ThreadPresenterTest.class })
public class WebappTestSuite {

}
