package org.vaadin.tori.tb;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.vaadin.testbench.By;
import com.vaadin.testbench.TestBench;

public class TBUtils {

    public static final String TARGET_URL = "http://localhost:8081/webapp";

    public static WebDriver getFirefoxDriver() {
        return TestBench.createDriver(new FirefoxDriver());
    }

    public static void showDropdowns(final WebDriver driver) {
        for (String className : Arrays.asList("dropdown", "v-menubar-menuitem")) {
            ((JavascriptExecutor) driver)
                    .executeScript("[].slice.call(document.getElementsByClassName('"
                            + className
                            + "')).map(function(item){ item.style.visibility = 'visible'; item.style.opacity = 1; })");
        }
    }

    public static List<WebElement> getDropdownItem(final WebDriver driver,
            final String itemCaption) {
        return driver.findElements(By.xpath("//span[text()[contains(.,'"
                + itemCaption + "')]]"));
    }

    public static void openFirstDropdown(final WebDriver driver,
            final String contextCssSelector) {
        TBUtils.showDropdowns(driver);
        List<WebElement> dropdowns = driver.findElements(By
                .cssSelector(contextCssSelector
                        + " .dropdown .v-menubar-menuitem"));
        dropdowns.get(0).click();
    }
}
