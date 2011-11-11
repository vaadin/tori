package org.vaadin.tori;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.vaadin.tori.category.CategoryPresenterTest;
import org.vaadin.tori.component.category.CategoryListingTest;

@RunWith(Suite.class)
@SuiteClasses({ CategoryPresenterTest.class, CategoryListingTest.class })
public class WebappTestSuite {

}
