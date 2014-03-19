package org.vaadin.tori.tb;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.testbench.By;
import com.vaadin.testbench.TestBenchTestCase;

public class ITListingViewPermissions extends TestBenchTestCase {

    private static WebDriver driver;
    private static String testCategoryUrl;
    private static String testCategoryTitle;

    private static String otherCategoryUrl;
    private static String otherCategoryTitle;

    @BeforeClass
    public static void setUp() {
        driver = TBUtils.getFirefoxDriver();
        driver.get(TBUtils.TARGET_URL);
        List<WebElement> categoryLinks = driver.findElements(By
                .cssSelector(".categoryLink a"));
        WebElement firstCategory = categoryLinks.get(0);
        testCategoryUrl = firstCategory.getAttribute("href").replaceAll("/#",
                "#");
        testCategoryTitle = firstCategory.getText();

        WebElement lastCategory = categoryLinks.get(categoryLinks.size() - 1);
        otherCategoryUrl = lastCategory.getAttribute("href").replaceAll("/#",
                "#");
        otherCategoryTitle = lastCategory.getText();

        navigateToTestCategory();
    }

    private static void navigateToTestCategory() {
        driver.get(testCategoryUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .titleContains(testCategoryTitle));
    }

    @Test
    public void testLockThread() {
        testThreadRowDropdownPermission("setMayLockThreadInCategory",
                "Lock Topic");
    }

    @Test
    public void testStickyThread() {
        testThreadRowDropdownPermission("setMayStickyThreadInCategory",
                "Pin Topic");
    }

    @Test
    public void testMoveThread() {
        testThreadRowDropdownPermission("setMayMoveThreadInCategory",
                "Move Topic");
    }

    private void testThreadRowDropdownPermission(final String permission,
            final String itemCaption) {
        openFirstThreadListingRowDropdown();
        Assert.assertFalse(getDropdownItem(itemCaption).isEmpty());

        togglePermission(permission);

        openFirstThreadListingRowDropdown();
        Assert.assertTrue(getDropdownItem(itemCaption).isEmpty());

        togglePermission(permission);
    }

    private List<WebElement> getDropdownItem(final String itemCaption) {
        return driver.findElements(By.xpath("//span[text()[contains(.,'"
                + itemCaption + "')]]"));
    }

    private void openFirstThreadListingRowDropdown() {
        openFirstDropdown(".threadlistingrow");
    }

    private void openFirstCategoryListingRowDropdown() {
        openFirstDropdown(".categoryTree");
    }

    private void openFirstDropdown(final String contextCssSelector) {
        showDropdowns();
        List<WebElement> dropdowns = driver.findElements(By
                .cssSelector(contextCssSelector
                        + " .dropdown .v-menubar-menuitem"));
        dropdowns.get(0).click();
    }

    private void showDropdowns() {
        for (String className : Arrays.asList("dropdown", "v-menubar-menuitem")) {
            ((JavascriptExecutor) driver)
                    .executeScript("[].slice.call(document.getElementsByClassName('"
                            + className
                            + "')).map(function(item){ item.style.visibility = 'visible'})");
        }
    }

    @Test
    public void testCreateThread() {
        String permission = "setMayCreateThreadInCategory";
        String newThreadButtonSelector = ".threadlistingview .headerlayout .v-button";

        List<WebElement> button = driver.findElements(By
                .cssSelector(newThreadButtonSelector));
        Assert.assertFalse(button.isEmpty());

        togglePermission(permission);

        button = driver.findElements(By.cssSelector(newThreadButtonSelector));
        Assert.assertTrue(button.isEmpty());

        togglePermission(permission);
    }

    @Test
    public void testCreateCategory() {
        String permission = "setMayEditCategories";
        String newCategoryButtonSelector = ".categorylistingview .headerlayout .v-button";

        List<WebElement> button = driver.findElements(By
                .cssSelector(newCategoryButtonSelector));
        Assert.assertFalse(button.isEmpty());

        togglePermission(permission);

        button = driver.findElements(By.cssSelector(newCategoryButtonSelector));
        Assert.assertTrue(button.isEmpty());

        togglePermission(permission);
    }

    @Test
    public void testDeleteCategory() {
        testCategoryDropdownPermission("setMayDeleteCategory",
                "Delete Category");
    }

    @Test
    public void testEditCategory() {
        testCategoryDropdownPermission("setMayEditCategory", "Edit Category");
    }

    private void testCategoryDropdownPermission(final String permission,
            final String itemCaption) {
        String checkUrl = TBUtils.TARGET_URL + "#!/dashboard";

        driver.get(checkUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions.titleIs("Tori"));
        openFirstCategoryListingRowDropdown();
        Assert.assertFalse(getDropdownItem(itemCaption).isEmpty());

        navigateToTestCategory();
        togglePermission(permission);

        driver.get(checkUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions.titleIs("Tori"));
        openFirstCategoryListingRowDropdown();
        Assert.assertTrue(getDropdownItem(itemCaption).isEmpty());

        navigateToTestCategory();
        togglePermission(permission);
    }

    @Test
    public void testViewCategory() {
        driver.get(otherCategoryUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .titleContains(otherCategoryTitle));

        togglePermission("setMayViewCategory");
        driver.findElement(By.cssSelector(".v-Notification-error")).click();
        navigateToTestCategory();
    }

    private void togglePermission(final String permission) {
        WebElement debugPopupButton = driver.findElement(By
                .cssSelector(".debugcontrolpanel .v-button"));
        debugPopupButton.click();
        driver.findElement(
                By.xpath("//label[text()[contains(.,'" + permission + "')]]"))
                .click();
        debugPopupButton.click();
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
    }
}
