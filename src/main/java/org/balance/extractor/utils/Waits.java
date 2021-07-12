package org.balance.extractor.utils;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.utils.Utils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.time.Duration;

/**
 * @author Nicholas Curl
 */
public class Waits {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Waits.class);

    private static final long DEFAULT_WAIT = 20;

    public static void waitForLoad(WebDriver driver) {
        waitForLoad(driver, DEFAULT_WAIT);
    }

    public static void waitForLoad(WebDriver driver, long timeOutInSeconds) throws TimeoutException {
        ExpectedCondition<Boolean> pageLoadCondition = driver1 -> ((JavascriptExecutor) driver1).executeScript(
                "return document.readyState").equals("complete");
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(pageLoadCondition);
        waitUntilJQueryReady(driver, timeOutInSeconds);
    }

    private static void waitUntilJQueryReady(WebDriver webDriver, long timeOutInSeconds) {
        JavascriptExecutor jsExec = (JavascriptExecutor) webDriver;
        Boolean jQueryDefined = (Boolean) jsExec.executeScript("return typeof jQuery != 'undefined'");
        if (jQueryDefined) {
            Utils.sleep(20);
            waitForJQueryLoad(webDriver, timeOutInSeconds);
            Utils.sleep(20);
        }
    }

    private static void waitForJQueryLoad(WebDriver webDriver, long timeOutInSeconds) {
        WebDriverWait jsWait = new WebDriverWait(webDriver, timeOutInSeconds);
        JavascriptExecutor jsExec = (JavascriptExecutor) webDriver;
        ExpectedCondition<Boolean> jQueryLoad = driver -> ((Long) ((JavascriptExecutor) driver)
                .executeScript("return jQuery.active") == 0
        );
        boolean jqueryReady = (Boolean) jsExec.executeScript("return jQuery.active===0");
        if (!jqueryReady) {
            jsWait.until(jQueryLoad);
        }
    }

    public static void waitUntilAttributeToBe(WebDriver driver, By by, String attribute, String value) {
        waitUntilAttributeToBe(driver, DEFAULT_WAIT, by, attribute, value);
    }

    public static void waitUntilAttributeToBe(WebDriver driver,
                                              long timeOutInSeconds,
                                              By by,
                                              String attribute,
                                              String value
    ) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(ExpectedConditions.attributeToBe(by, attribute, value));
    }

    public static void waitUntilAttributeToBe(WebDriver driver, WebElement element, String attribute, String value) {
        waitUntilAttributeToBe(driver, DEFAULT_WAIT, element, attribute, value);
    }

    public static void waitUntilAttributeToBe(WebDriver driver,
                                              long timeOutInSeconds,
                                              WebElement element,
                                              String attribute,
                                              String value
    ) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(ExpectedConditions.attributeToBe(element, attribute, value));
    }

    public static void waitUntilClickable(WebDriver driver, By by) throws TimeoutException {
        waitUntilClickable(driver, by, DEFAULT_WAIT);
    }

    public static void waitUntilClickable(WebDriver driver, By by, long timeOutInSeconds) throws TimeoutException {
        WebDriverWait driverWait = new WebDriverWait(driver, timeOutInSeconds);
        driverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
        waitForLoad(driver, timeOutInSeconds);
    }

    public static void waitUntilClickable(WebDriver driver, WebElement element) throws TimeoutException {
        waitUntilClickable(driver, element, DEFAULT_WAIT);
    }

    public static void waitUntilClickable(WebDriver driver, WebElement element, long timeOutInSeconds)
    throws TimeoutException {
        WebDriverWait driverWait = new WebDriverWait(driver, timeOutInSeconds);
        driverWait.until(ExpectedConditions.elementToBeClickable(element)).click();
        waitForLoad(driver, timeOutInSeconds);
    }

    public static void waitUntilFileDownloaded(WebDriver driver, Path downloadDir, long timeout, String fileName) {
        waitUntilFileDownloaded(driver, downloadDir.toFile(), timeout, fileName);
    }

    public static void waitUntilFileDownloaded(WebDriver driver, File downloadDir, long timeout, String fileName)
    throws TimeoutException {
        FluentWait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofMillis(timeout))
                                                             .pollingEvery(Duration.ofMillis(200L));
        FileFilter fileFilter = FileFilterUtils.nameFileFilter(fileName);
        wait.until(driver1 -> {
            File[] files = downloadDir.listFiles(fileFilter);
            return (files != null && files.length > 0);
        });
        Utils.sleep(1000);
    }

    public static void waitUntilVisible(WebDriver driver, By by) throws TimeoutException {
        waitUntilVisible(driver, by, DEFAULT_WAIT);
    }

    public static void waitUntilVisible(WebDriver driver, By by, long timeOutInSeconds) throws TimeoutException {
        WebDriverWait driverWait = new WebDriverWait(driver, timeOutInSeconds);
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
}
