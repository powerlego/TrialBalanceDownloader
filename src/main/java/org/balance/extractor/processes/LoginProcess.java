package org.balance.extractor.processes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.extractor.driver.Driver;
import org.balance.extractor.utils.Waits;
import org.balance.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * @author Nicholas Curl
 */
public class LoginProcess {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(LoginProcess.class);

    public static void login(Driver driver, String company) throws InterruptedException {
        String companyEncoded = Utils.encodeCompany(company);
        driver.get("https://nav2017-amer.navonazure.com/MTE-PRD12/WebClient/Default.aspx?company=" +
                   companyEncoded +
                   "&page=408&dc=0");
        loginEntry(driver);
        Waits.waitUntilVisible(driver, By.xpath("//h1[@title='G/L Balance by Dimension']"));
        Waits.waitUntilClickable(driver, By.xpath("//span[text()='Options']"));
        Waits.waitUntilClickable(driver, By.xpath("//span[text()='Matrix Options']"));
        Waits.waitUntilVisible(driver, By.xpath("//a[text()='View as']"));
    }

    public static void loginGLEntry(Driver driver, String company) throws InterruptedException {
        String companyEncoded = Utils.encodeCompany(company);
        driver.get("https://nav2017-amer.navonazure.com/MTE-PRD12/WebClient/Default.aspx?company=" +
                   companyEncoded +
                   "&page=20&dc=0");
        loginEntry(driver);
        Waits.waitUntilVisible(driver, By.xpath("//h1[@title='General Ledger Entries']"));
    }

    private static void loginEntry(Driver driver) throws InterruptedException {
        Waits.waitUntilVisible(driver, By.id("ctl00_PHM_UserName"));
        setValue(driver,By.id("ctl00_PHM_UserName"),"amer\\cherie_mte");
        setValue(driver,By.id("ctl00_PHM_Password"),"Dolphin1971!");
        Waits.waitUntilClickable(driver, By.id("ctl00_PHM_LoginButton"));
    }

    private static void setValue(Driver driver, By by, String value) throws InterruptedException {
        WebElement input = driver.findElement(by);
        new Actions(driver).click(input).perform();
        Waits.waitForLoad(driver);
        input.sendKeys(value);
        Waits.waitForLoad(driver);
        Waits.waitUntilAttributeToBe(driver, input, "value", value);
    }
}
