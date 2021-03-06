package org.balance.extractor.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.balance.extractor.driver.Driver;
import org.balance.utils.Utils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Nicholas Curl
 */
public class ExtractorUtils {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(ExtractorUtils.class);

    public static String getMonthlyPeriod(Date date){
        LocalDate localStartDate = Utils.dateToLocalDate(date);
        return getMonthlyPeriod(localStartDate);
    }

    public static String getMonthlyPeriod(LocalDate startDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        startDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        if (startDate.isAfter(LocalDate.now()) || startDate.isEqual(LocalDate.now())){
            return "";
        }
        else{
            return startDate.with(TemporalAdjusters.firstDayOfMonth()).format(formatter) +
                   "..C" +
                   startDate.with(TemporalAdjusters.lastDayOfMonth()).format(formatter);
        }
    }

    public static List<String> generatePeriods(Date startDate){
        LocalDate localStartDate = Utils.dateToLocalDate(startDate);
        return generatePeriods(localStartDate);
    }

    public static List<String> generatePeriods(LocalDate startDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        startDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        List<String> periods = new ArrayList<>();
        while (!startDate.isAfter(LocalDate.now()) && !startDate.isEqual(LocalDate.now())) {
            periods.add(formatter.format(startDate));
            startDate = startDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
        return periods;
    }

    public static void dimensionCheckAndSelect(Driver driver, String type, String value) throws InterruptedException {
        if (!driver.findElement(By.xpath("//a[text()='" + type + "']/..//input[@type='text']"))
                   .getAttribute("value")
                   .equalsIgnoreCase(value)) {
            Waits.waitUntilClickable(driver, By.xpath("//a[text()='" + type + "']/..//input[@type='button']"));
            Waits.waitUntilVisible(driver, By.xpath("//h2[@title='Dimension Selection']"));
            Waits.waitForLoad(driver);
            Waits.waitUntilClickable(driver, By.xpath("//a[text()='" + value + "']"));
            Waits.waitForLoad(driver);
            Waits.waitUntilAttributeToBe(driver,
                                         By.xpath("//a[text()='" + type + "']/..//input[@type='text']"),
                                         "value",
                                         value
            );
        }
    }

    public static void checkSelect(Driver driver, String type, String value) throws InterruptedException {
        if (!driver.findElement(By.xpath("//a[text()='" + type + "']/..//select"))
                   .getAttribute("title")
                   .equalsIgnoreCase(value)) {
            Waits.waitUntilClickable(driver, By.xpath("//a[text()='" + type + "']/..//select"));
            Waits.waitUntilClickable(driver,
                                     By.xpath("//a[text()='" + type + "']/..//select/option[@title='" + value + "']")
            );
        }
    }

    public static void clearAndSetValue(Driver driver, By by, String value) throws InterruptedException {
        WebElement input = driver.findElement(by);
        input.clear();
        Waits.waitForLoad(driver);
        Waits.waitUntilAttributeToBe(driver, input, "value", "");
        Waits.waitForLoad(driver);
        new Actions(driver).click(input).perform();
        Waits.waitForLoad(driver);
        input.sendKeys(value);
        Waits.waitForLoad(driver);
        Waits.waitUntilAttributeToBe(driver, input, "value", value);
    }

    public static void checkAndSet(Driver driver, String type, String value) throws InterruptedException {
        if (!driver.findElement(By.xpath("//a[text()='" + type + "']/..//input[@type='text']"))
                   .getAttribute("value")
                   .equalsIgnoreCase(value)) {
            clearAndSetValue(driver, By.xpath("//a[text()='" + type + "']/..//input[@type='text']"), value);
        }
    }

    public static List<String> getDeptCodes(Driver driver) throws InterruptedException {
        WebElement element = driver.findElement(By.xpath(
                "//th[@abbr='Dept Code']//a[@title='Open Menu']/ancestor::th"));

        new Actions(driver).contextClick(element).perform();
        Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
        Waits.waitUntilVisible(driver,
                               By.xpath(
                                       "//p[text()='Only show lines where \"Dept Code\" is']/" +
                                       "ancestor::div[@class='ms-nav-band-container']//input")
        );
        Waits.waitUntilClickable(driver, By.xpath("//input[@title='Look up value']"));
        scrollTable(driver, "Lookupform");
        return driver.findElements(By.xpath(
                "//table[@summary='Lookupform']/tbody//td[2]/a[" +
                "not(text()='1258') and " +
                "not(text()='158') and " +
                "not(text()='300') and " +
                "not(text()='395') and " +
                "not(text()='399') and " +
                "not(text()='100')" +
                "]"))
                     .stream()
                     .parallel()
                     .map(
                             WebElement::getText).filter(Strings::isNotBlank)
                     .collect(Collectors.toList());
    }

    public static List<String> getAccountNums(Driver driver) throws InterruptedException {
        List<String> accountNums = new ArrayList<>();
        WebElement element = driver.findElement(By.xpath(
                "//th[@abbr='G/L Account No.']//a[@title='Open Menu']/ancestor::th"));

        new Actions(driver).contextClick(element).perform();
        Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
        Waits.waitUntilVisible(driver,
                               By.xpath(
                                       "//p[text()='Only show lines where \"G/L Account No.\" is']/" +
                                       "ancestor::div[@class='ms-nav-band-container']//input")
        );
        Waits.waitUntilClickable(driver, By.xpath("//input[@title='Look up value']"));
        scrollTable(driver, "Lookupform");
        List<WebElement> rows = driver.findElements(By.xpath("//table[@summary='Lookupform']/tbody//td[2]/a[" +
                                                             "not(text()='22000') and " +
                                                             "not(text()='23000') and " +
                                                             "not(text()='33000') and " +
                                                             "not(text()='50000') and " +
                                                             "not(text()='59000')" +
                                                             "]/ancestor::tr"));
        for (WebElement row : rows) {
            try {
                String accountNumber = row.findElement(By.xpath("./td[2]/a")).getText();
                String accountName = row.findElement(By.xpath("./td[3]/span")).getText();
                accountNums.add(accountNumber + " " + accountName);
            } catch (WebDriverException e){
                if (e.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw (InterruptedException) e.getCause();
                }
            }
        }

        return accountNums.stream().parallel().filter(Strings::isNotBlank).collect(Collectors.toList());
    }

    public static Path initialize() {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please list the directory you want to save the trial balances: ");
        Path downloadDirPath = null;
        try {
            downloadDirPath = Paths.get(scanner.nextLine()).toFile().getCanonicalFile().toPath();
        }
        catch (IOException canonical) {
            logger.fatal("Unable to get canonical path", canonical);
            System.exit(1);
        }
        if (!downloadDirPath.toFile().exists()) {
            try {
                Files.createDirectories(downloadDirPath);
            }
            catch (IOException download) {
                logger.fatal("Unable to create download directory", download);
                System.exit(1);
            }
        }
        return downloadDirPath;
    }

    public static boolean scrollTable(Driver driver, String summary) {
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.MINUTES);
        try {
            ((JavascriptExecutor) driver).executeAsyncScript("var callback = arguments[arguments.length-1];" +
                                                             "function scrollDown(){" +
                                                             "    var main = document.evaluate(\"//table[@summary='" +
                                                             summary +
                                                             "']/..\",document,null,XPathResult.ANY_TYPE,null).iterateNext();" +
                                                             "    var test = document.evaluate(\"//table[@summary='" +
                                                             summary +
                                                             "']/../div[2]\",document,null,XPathResult.ANY_TYPE,null).iterateNext();" +
                                                             "    function loop(){" +
                                                             "        if(test.classList.length === 1){" +
                                                             "            main.scrollTo(0,main.scrollHeight);" +
                                                             "            setTimeout(loop,0);" +
                                                             "        }" +
                                                             "        else{" +
                                                             "           setTimeout(callback,0)" +
                                                             "        }" +
                                                             "    }" +
                                                             "    loop();" +
                                                             "}" +
                                                             "function scrollUp(){" +
                                                             "  var main = document.evaluate(\"//table[@summary='" +
                                                             summary +
                                                             "']/..\",document,null,XPathResult.ANY_TYPE,null).iterateNext();" +
                                                             "  var test = document.evaluate(\"//table[@summary='" +
                                                             summary +
                                                             "']/../div[1]\", document, null, XPathResult.ANY_TYPE, null).iterateNext();" +
                                                             "  function up(){" +
                                                             "      if(test.classList.length === 1){" +
                                                             "          main.scrollTo(0,0);" +
                                                             "          setTimeout(up,0);" +
                                                             "      }" +
                                                             "      else {" +
                                                             "          " +
                                                             "      }" +
                                                             "  }" +
                                                             "  up();" +
                                                             "}" +
                                                             "setTimeout(scrollUp,0);" +
                                                             "setTimeout(scrollDown,0);"
            );
            return true;
        } catch (TimeoutException | ScriptTimeoutException e){
            return false;
        }
    }
}
