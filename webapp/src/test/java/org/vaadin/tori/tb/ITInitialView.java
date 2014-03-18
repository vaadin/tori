package org.vaadin.tori.tb;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ITInitialView {

    protected static WebDriver driver;

    @BeforeClass
    public static void setUp() {
        driver = TBUtils.getFirefoxDriver();
        driver.get(TBUtils.TARGET_URL);
    }

    @Test
    public void runParts() {
        checkMostRecent();
    }

    private void checkMostRecent() {
        WebElement mostRecentLink = driver.findElement(By
                .cssSelector(".recentbar .current .v-link span"));
        assertEquals("Discussion thread 298", mostRecentLink.getText());
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
    }
}
