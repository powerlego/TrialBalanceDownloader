package org.balance.extractor.processes.tb;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.data.mapping.Mapper;
import org.balance.data.objects.NavData;
import org.balance.data.objects.NavTables;
import org.balance.data.processes.ExcelFormatter;
import org.balance.data.utils.DataUtils;
import org.balance.extractor.driver.Driver;
import org.balance.extractor.processes.LoginProcess;
import org.balance.extractor.utils.ExtractorUtils;
import org.balance.extractor.utils.Waits;
import org.balance.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Nicholas Curl
 */
public class TrialBalanceExtract {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(TrialBalanceExtract.class);

    public static NavData extract(Driver driver, String company) {
        Path companyFolder = driver.getDownloadDir().resolve(company);
        List<String> dates = generateDates();
        ProgressBar progressBar = new ProgressBarBuilder().setInitialMax(dates.size())
                                                          .setUpdateIntervalMillis(1)
                                                          .setTaskName("Extracting Trial Balances")
                                                          .setStyle(ProgressBarStyle.ASCII)
                                                          .setMaxRenderedLength(120)
                                                          .build();
        Utils.sleep(100);
        List<NavTables> navTables = new ArrayList<>();
        for (String date : dates) {
            NavTables tables = extractAtDate(driver, company, date,true);
            navTables.add(tables);
            progressBar.step();
            Utils.sleep(10);
            Path curDownloadDir = driver.getDownloadDir();
            driver.quit();
            driver = Driver.createDriver(curDownloadDir);
            LoginProcess.login(driver, Utils.encodeCompany(company));
        }
        progressBar.close();
        return new NavData(companyFolder, navTables);
    }
    
    public static void extractAtDate(Driver driver, String company, String date){
        extractAtDate(driver,company,date,false);
    }

    public static NavTables extractAtDate(Driver driver, String company, String date, boolean separateTables) {
        Path companyFolder = driver.getDownloadDir().resolve(company);
        if (!companyFolder.toFile().exists()) {
            try {
                Files.createDirectories(companyFolder);
            }
            catch (IOException e) {
                logger.fatal("Unable to create company folder {}", companyFolder, e);
                driver.quit();
                System.exit(1);
            }
        }
        Path downloadDir = companyFolder.resolve("downloads");
        if (!downloadDir.toFile().exists()) {
            try {
                Files.createDirectories(downloadDir);
            }
            catch (IOException e) {
                logger.fatal("Unable to create download folder {}", downloadDir, e);
                driver.quit();
                System.exit(1);
            }
        }
        Path download = downloadDir.resolve(company + "-" + date.replace("/", "-") + ".xlsx");
        if(separateTables) {
            List<List<Object>> debitTable = getBalances(driver, date, "Debit", company);
            List<List<Object>> creditTable = getBalances(driver, date, "Credit", company);
            Path debit = downloadDir.resolve(company + "-" + date.replace("/", "-") + "-debit.xlsx");
            Path credit = downloadDir.resolve(company + "-" + date.replace("/", "-") + "-credit.xlsx");
            try {
                DataUtils.writeWorkbook(debitTable, debit.toFile());
                DataUtils.writeWorkbook(creditTable, credit.toFile());
            }
            catch (IOException e) {
                logger.fatal("Unable to write files", e);
                driver.quit();
                System.exit(1);
            }
            ExcelFormatter.makeWorkbook(download, debitTable, creditTable);
            return new NavTables(debitTable, creditTable, date);
        }
        else {
            List<List<Object>> table = getBalances(driver,date,"Amount",company);
            try {
                DataUtils.writeWorkbook(table,download.toFile());
            } catch (IOException e){
                logger.fatal("Unable to write file", e);
                driver.quit();
                System.exit(1);
            }
            return NavTables.EMPTY;
        }
    }

    public static void extractGLEntries(Driver driver, String company, String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Path companyFolder = driver.getDownloadDir().resolve(company);
        if (!companyFolder.toFile().exists()) {
            try {
                Files.createDirectories(companyFolder);
            }
            catch (IOException e) {
                logger.fatal("Unable to create company folder {}", companyFolder, e);
                driver.quit();
                System.exit(1);
            }
        }
        Path glEntryFolder = companyFolder.resolve("GL_Entries");
        if (!glEntryFolder.toFile().exists()) {
            try {
                Files.createDirectories(glEntryFolder);
            }
            catch (IOException e) {
                logger.fatal("Unable to create G/L Entry folder", e);
                driver.quit();
                System.exit(1);
            }
        }
        List<String> deptCodes = ExtractorUtils.getDeptCodes(driver);
        new Actions(driver).sendKeys(Keys.ESCAPE).pause(1000).sendKeys(Keys.ESCAPE).perform();
        List<String> header = driver.findElements(By.xpath(
                "//table[@summary='General Ledger Entries']/thead/tr/th[not(@abbr='null') and @abbr]"))
                                    .stream()
                                    .map(element1 -> element1.getAttribute("abbr"))
                                    .collect(
                                            Collectors.toList());
        List<List<Object>> data = new ArrayList<>();
        WebElement element
                = driver.findElement(By.xpath("//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
        new Actions(driver).contextClick(element).perform();
        Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
        Waits.waitUntilVisible(driver,
                               By.xpath(
                                       "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                       "ancestor::div[@class='ms-nav-band-container']//input")
        );
        ExtractorUtils.clearAndSetValue(driver,
                                        By.xpath(
                                                "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                                "ancestor::div[@class='ms-nav-band-container']//input"),
                                        date
        );
        Waits.waitUntilClickable(driver, By.xpath("//button[@title='OK']"));
        Waits.waitForLoad(driver);
        ExtractorUtils.scrollTable(driver, "General Ledger Entries");
        List<WebElement> rows = driver.findElements(By.xpath(
                "//table[@summary = 'General Ledger Entries']/tbody/tr"));
        ProgressBar pb3 = Utils.createProgressBar("Extracting Data", rows.size() * header.size());
        pb3.setExtraMessage(company + " Period: " + date);
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td[@aria-readonly]/*"));
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < cells.size(); i++) {
                WebElement cell = cells.get(i);
                String text = cell.getAttribute("title").trim();
                if (text.contains("Open record")) {
                    Pattern pattern = Pattern.compile("\"(.*?)\"");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        text = matcher.group(1);
                    }
                    else {
                        text = "";
                    }
                }
                try {
                    Date extractedDate = simpleDateFormat.parse(text);
                    values.add(extractedDate);
                }
                catch (ParseException e) {
                    if (Pattern.compile("\\d\\.\\d{2}").matcher(text).find() && i == 4) {
                        try {
                            values.add(new BigDecimal(text.replace(",", "")));
                        }
                        catch (NumberFormatException formatException) {
                            logger.fatal("Unable to get amount {}", text, formatException);
                            driver.quit();
                            System.exit(1);
                        }
                    }
                    else {
                        values.add(text);
                    }
                }
                pb3.step();
            }
            data.add(values);
        }
        pb3.close();
        Map<String, List<List<Object>>> map = Mapper.mapGL(header, data, deptCodes);
        Path workbook = glEntryFolder.resolve("GLEntries-" + date.replace("/", "_").replace("..", "-") + ".xlsx");
        ExcelFormatter.makeGLWorkbook(workbook, map);
    }

    private static List<String> generateDates() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy");
        List<String> dates = new ArrayList<>();
        try {
            Date date = simpleDateFormat.parse("12/31/18");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Instant now = Instant.now();
            while (calendar.toInstant().isBefore(now)) {
                dates.add(simpleDateFormat.format(calendar.getTime()));
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DATE));
            }
        }
        catch (ParseException e) {
            logger.fatal("Unable to parse starting date", e);
        }
        return dates;
    }

    private static List<List<Object>> getBalances(Driver driver, String dateString, String showAmount, String company) {
        String newDateString = "''..C"+dateString;
        //region Property Selection
        //region Rows Selection
        ExtractorUtils.dimensionCheckAndSelect(driver, "Show as Lines", "G/L Account");
        //endregion
        Waits.waitForLoad(driver);
        //region Columns Selection
        ExtractorUtils.dimensionCheckAndSelect(driver, "Show as Columns", "DEPT");
        //endregion
        //region Closing Entries
        ExtractorUtils.checkSelect(driver, "Closing Entries", "Include");
        //endregion
        //region View As
        ExtractorUtils.checkSelect(driver, "View as", "Balance at Date");
        //endregion
        //region View By
        ExtractorUtils.checkSelect(driver, "View by", "Accounting Period");
        //endregion
        //region Date Filter
        ExtractorUtils.clearAndSetValue(driver, By.xpath("//a[text()='Date Filter']/..//input"), newDateString);
        //endregion
        //region Account Filter
        ExtractorUtils.checkAndSet(driver, "G/L Account Filter", "<>22000&<>23000&<>33000&<>50000&<>59000");
        //endregion
        //region Dept Filter
        ExtractorUtils.checkAndSet(driver, "Dept Filter", "<>1258&<>158&<>300&<>395&<>399&<>100");
        //endregion
        //region Show Selection
        ExtractorUtils.checkSelect(driver, "Show", "Actual Amounts");
        //endregion
        //region Show Amount Field
        if (showAmount.equalsIgnoreCase("Debit")) {
            ExtractorUtils.checkSelect(driver, "Show Amount Field", "Debit Amount");
        }
        else if (showAmount.equalsIgnoreCase("Credit")) {
            ExtractorUtils.checkSelect(driver, "Show Amount Field", "Credit Amount");
        }
        else {
            ExtractorUtils.checkSelect(driver, "Show Amount Field", "Amount");
        }
        //endregion
        //endregion
        //region Data Grab
        //region Show Matrix
        Waits.waitUntilClickable(driver, By.xpath("//span[contains(text(),'Show')]/.."));
        Waits.waitUntilVisible(driver, By.xpath("//table[@summary='G/L Balance by Dim. Matrix']"));
        //endregion
        //region Show Every Row
        do {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTo(0,arguments[0].scrollHeight)",
                                                        driver.findElement(By.xpath(
                                                                "//table[@summary='G/L Balance by Dim. Matrix']/.."))
            );
        }
        while (!driver.findElement(By.xpath("//table[@summary='G/L Balance by Dim. Matrix']/../div[2]"))
                      .getAttribute("class")
                      .toLowerCase(
                              Locale.ROOT)
                      .contains("ms-nav-hidden"));
        //endregion
        //region Get Table
        List<List<Object>> table = Collections.synchronizedList(new ArrayList<>());
        List<Object> header = new ArrayList<>();
        List<WebElement> headers = driver.findElements(By.xpath(
                "//table[@summary='G/L Balance by Dim. Matrix']/thead/tr/th[not(@abbr='null') and @abbr]"));
        for (WebElement headerElement : headers) {
            String value = headerElement.getAttribute("abbr");
            header.add(value);
        }
        table.add(header);
        List<WebElement> rows
                = driver.findElements(By.xpath("//table[@summary='G/L Balance by Dim. Matrix']/tbody/tr"));
        ProgressBar progressBar = new ProgressBarBuilder().setTaskName("Reading " + showAmount + " Table")
                                                          .setMaxRenderedLength(120)
                                                          .setUpdateIntervalMillis(1)
                                                          .setStyle(ProgressBarStyle.ASCII)
                                                          .setInitialMax(rows.size())
                                                          .build()
                                                          .setExtraMessage("Company: " +
                                                                           company +
                                                                           ", Period: " +
                                                                           dateString);

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td[not(@class='ms-nav-hidden') and @aria-readonly]"));
            List<Object> tableRow = new ArrayList<>();
            for (int i = 0; i < cells.size(); i++) {
                WebElement cell = cells.get(i);
                String text = cell.getText();
                if (text.isBlank() && !(i == 0 || i == 1)) {
                    tableRow.add(new BigDecimal("0.00"));
                }
                else if (text.contains(".") && !(i == 0 || i == 1)) {
                    text = text.replace(",", "");
                    BigDecimal value;
                    try {
                        value = new BigDecimal(text);
                        if (showAmount.equalsIgnoreCase("credit")) {
                            value = value.negate();
                        }
                    }
                    catch (NumberFormatException e) {
                        value = new BigDecimal("0.00");
                        logger.fatal("Unable to get value {}", text, e);
                        driver.quit();
                        System.exit(1);
                    }
                    tableRow.add(value);
                }
                else {
                    tableRow.add(text.trim());
                }
            }
            table.add(tableRow);
            progressBar.step();
        }
        //endregion
        progressBar.close();
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        return table;
    }

}
