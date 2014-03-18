package org.vaadin.tori.tb;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.vaadin.testbench.TestBench;

public class TBUtils {

    public static final String TARGET_URL = "http://localhost:8081/webapp";

    public static WebDriver getFirefoxDriver() {
        return TestBench.createDriver(new FirefoxDriver());
    }
}
