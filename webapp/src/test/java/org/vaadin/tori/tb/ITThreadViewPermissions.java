package org.vaadin.tori.tb;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.testbench.By;
import com.vaadin.testbench.TestBenchTestCase;

public class ITThreadViewPermissions extends TestBenchTestCase {

    private static WebDriver driver;
    private static String testCategoryUrl;
    private static String testCategoryTitle;
    private static String testThreadUrl;
    private static String testThreadTitle;

    private static String otherThreadUrl;
    private static String otherThreadTitle;

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

        navigateToTestCategory();

        List<WebElement> threadLinks = driver.findElements(By
                .cssSelector(".threadlistingrow a.topicname"));

        WebElement firstThread = threadLinks.get(0);

        testThreadUrl = firstThread.getAttribute("href").replaceAll("/#", "#");
        testThreadTitle = firstThread.getText();

        WebElement otherThread = threadLinks.get(4);
        otherThreadUrl = otherThread.getAttribute("href").replaceAll("/#", "#");
        otherThreadTitle = otherThread.getText();

        navigateToTestThread();
    }

    @Test
    public void testAddFiles() {
        String permission = "setMayAddFilesInCategory";

        Assert.assertFalse(findUpload().isEmpty());
        togglePermission(false, permission);

        Assert.assertTrue(findUpload().isEmpty());
        togglePermission(false, permission);
    }

    @Test
    public void testBan() {
        testPostDropdownPermission(false, "setMayBan", "Ban Author");
    }

    @Test
    public void testDeletePost() {
        testPostDropdownPermission(true, "setMayDeletePost", "Delete Post...");
    }

    @Test
    public void testEditPost() {
        testPostDropdownPermission(true, "setMayEditPost", "Edit Post");
    }

    @Test
    public void testFollow() {
        String permission = "setMayFollowThread";

        Assert.assertTrue(findFollowComponents().size() == 2);
        navigateToTestCategory();
        TBUtils.openFirstDropdown(driver, ".threadlistingrow");
        Assert.assertFalse(TBUtils.getDropdownItem(driver, "Follow Topic")
                .isEmpty());
        navigateToTestThread();

        togglePermission(false, permission);

        Assert.assertTrue(findFollowComponents().isEmpty());
        navigateToTestCategory();
        TBUtils.openFirstDropdown(driver, ".threadlistingrow");
        Assert.assertTrue(TBUtils.getDropdownItem(driver, "Follow Topic")
                .isEmpty());
        navigateToTestThread();

        togglePermission(false, permission);
    }

    @Test
    public void testReply() {
        String permission = "setMayReplyInThread";

        Assert.assertTrue(findReplyComponents().size() == 2);
        togglePermission(false, permission);

        Assert.assertTrue(findReplyComponents().isEmpty());
        togglePermission(false, permission);
    }

    @Test
    public void testReport() {
        String permission = "setMayReportPosts";

        Assert.assertTrue(findReportComponents().size() == 1);
        togglePermission(false, permission);

        Assert.assertTrue(findReportComponents().isEmpty());
        togglePermission(false, permission);
    }

    @Test
    public void testVote() {
        String permission = "setMayVote";

        Assert.assertTrue(findVoteComponents().size() == 2);
        togglePermission(false, permission);

        Assert.assertTrue(findVoteComponents().isEmpty());
        togglePermission(false, permission);
    }

    @Test
    public void testViewPost() {
        String permission = "setMayViewPost";

        Assert.assertTrue(driver.findElements(By.cssSelector(".post")).size() == 2);
        togglePermission(true, permission);

        Assert.assertTrue(driver.findElements(By.cssSelector(".post")).size() == 1);
        togglePermission(true, permission);
    }

    @Test
    public void testViewThread() {
        String permission = "setMayViewThread";

        navigateToOtherThread();
        navigateToTestCategory();
        Assert.assertTrue(driver
                .findElements(
                        By.xpath("//a[text()[contains(.,'" + otherThreadTitle
                                + "')]]")).size() == 1);

        navigateToOtherThread();
        togglePermission(false, permission);

        driver.findElement(By.cssSelector(".v-Notification-error")).click();
        navigateToTestCategory();

        Assert.assertTrue(driver
                .findElements(
                        By.xpath("//a[text()[contains(.,'" + otherThreadTitle
                                + "')]]")).isEmpty());

        navigateToTestThread();
    }

    private List<WebElement> findVoteComponents() {
        return driver.findElements(By.cssSelector(".footer .vote"));
    }

    private List<WebElement> findReportComponents() {
        return driver.findElements(By.cssSelector(".flagpost"));
    }

    private List<WebElement> findReplyComponents() {
        List<WebElement> result = new ArrayList<WebElement>();
        result.addAll(driver.findElements(By.cssSelector(".quoteforreply")));
        result.addAll(driver.findElements(By.cssSelector(".authoringcomponent")));
        return result;
    }

    private List<WebElement> findFollowComponents() {
        List<WebElement> result = new ArrayList<WebElement>();
        result.addAll(driver.findElements(By.cssSelector(".notfollowed")));
        result.addAll(driver.findElements(By
                .cssSelector(".buttonslayout .v-checkbox")));
        return result;
    }

    private List<WebElement> findUpload() {
        WebElement buttonsLayout = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.visibilityOfElementLocated(By
                        .cssSelector(".posteditor .buttonslayout")));
        List<WebElement> upload = buttonsLayout.findElements(By
                .cssSelector(".v-upload"));
        return upload;
    }

    private void togglePermission(final boolean popupPermission,
            final String permission) {
        WebElement debugPopupButton = driver.findElement(By
                .cssSelector(".debugcontrolpanel .v-button"));
        debugPopupButton.click();

        if (popupPermission) {
            driver.findElement(
                    By.xpath("//div[text()[contains(.,'" + permission + "')]]"))
                    .click();
            driver.findElement(By.xpath("//div[text() = '" + permission + "']"))
                    .click();
            driver.findElement(
                    By.cssSelector(".postselect-content." + permission
                            + " input")).click();
        } else {
            driver.findElement(
                    By.xpath("//label[text()[contains(.,'" + permission
                            + "')]]")).click();
            debugPopupButton.click();
        }

    }

    private void testPostDropdownPermission(final boolean popupPermission,
            final String permission, final String itemCaption) {
        openFirstPostDropdown();
        Assert.assertFalse(TBUtils.getDropdownItem(driver, itemCaption)
                .isEmpty());

        togglePermission(popupPermission, permission);

        openFirstPostDropdown();
        Assert.assertTrue(TBUtils.getDropdownItem(driver, itemCaption)
                .isEmpty());

        togglePermission(popupPermission, permission);
    }

    private void openFirstPostDropdown() {
        TBUtils.showDropdowns(driver);
        WebElement until = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.elementToBeClickable(By
                        .cssSelector(".post .dropdown .v-menubar-menuitem")));
        until.click();
    }

    private static void navigateToOtherThread() {
        driver.get(otherThreadUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .titleContains(otherThreadTitle));
    }

    private static void navigateToTestThread() {
        driver.get(testThreadUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .titleContains(testThreadTitle));
    }

    private static void navigateToTestCategory() {
        driver.get(testCategoryUrl);
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .titleContains(testCategoryTitle));
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
    }
}
