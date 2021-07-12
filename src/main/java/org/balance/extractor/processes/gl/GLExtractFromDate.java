package org.balance.extractor.processes.gl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.data.mapping.gl.GLMapper;
import org.balance.data.processes.ExcelFormatter;
import org.balance.extractor.driver.Driver;
import org.balance.extractor.gui.ui.components.ProgressGL;
import org.balance.extractor.gui.ui.gl.GLFromDate;
import org.balance.extractor.processes.Extractor;
import org.balance.extractor.processes.LoginProcess;
import org.balance.extractor.utils.ExtractorUtils;
import org.balance.extractor.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Nicholas Curl
 */
public class GLExtractFromDate extends Extractor {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(GLExtractFromDate.class);

    private final GLFromDate       ui;
    private final List<String>     dates;
    private final List<ProgressGL> progressList;

    private final AtomicInteger selector;

    public GLExtractFromDate(GLFromDate ui, List<String> dates, List<ProgressGL> progressList) {
        super(new HashMap<>());
        this.ui = ui;
        this.dates = dates;
        this.progressList = progressList;
        this.selector = new AtomicInteger();
    }

    @Override
    public void extract() {
        for (int i = 0; i < 3; i++) {
            TaskFromDate task = new TaskFromDate(this.progressList.get(i),
                                                 this.dates.get(selector.getAndIncrement()),
                                                 (String) this.ui.getCompanySelector().getSelectedItem(),
                                                 this.ui.getOverallProgressBar(),
                                                 this.ui.getFileChooser().getSelectedFile().toPath()
            );
            this.addTask(task);
            task.execute();
        }
    }

    public class TaskFromDate extends Task<Void> {

        private final String     date;
        private final String     company;
        private final Path       downloadDir;
        private final ProgressGL progressContainer;
        private       Driver     driver;

        public TaskFromDate(ProgressGL progressContainer,
                            String date,
                            String company,
                            JProgressBar overallProgress,
                            Path downloadDir
        ) {
            super(progressContainer, overallProgress,date);
            this.progressContainer = progressContainer;
            this.date = date;
            this.company = company;
            this.downloadDir = downloadDir;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * <p>
         * Note that this method is executed only once.
         *
         * <p>
         * Note: this method is executed in a background thread.
         *
         * @return the computed result
         *
         * @throws Exception if unable to compute a result
         */
        @Override
        protected Void doInBackground() throws Exception {
            driver = Driver.createDriver(downloadDir);
            this.progressContainer.getCompany().setText(company);
            this.progressContainer.getPeriod().setText(date);
            this.progressContainer.getStatus().setText("Logging In");
            LoginProcess.loginGLEntry(driver, company);
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
            List<String> accountNums = ExtractorUtils.getAccountNums(driver);
            List<String> accountNumbers = accountNums.stream()
                                                     .parallel()
                                                     .map(s -> s.split(" ", 2)[0])
                                                     .collect(Collectors.toList());
            new Actions(driver).sendKeys(Keys.ESCAPE).pause(1000).sendKeys(Keys.ESCAPE).perform();
            List<String> header = driver.findElements(By.xpath(
                    "//table[@summary='General Ledger Entries']/thead/tr/th[not(@abbr='null') and @abbr]"))
                                        .stream()
                                        .map(element1 -> element1.getAttribute("abbr"))
                                        .collect(
                                                Collectors.toList());
            List<List<Object>> data = new ArrayList<>();
            for (String accountNum : accountNumbers) {
                if (isCancelled()) {
                    return null;
                }
                this.progressContainer.getStatus().setText("Filtering");
                this.progressContainer.getAccount().setText(accountNum);
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
                this.progressContainer.getProgressBar().setValue(0);
                element = driver.findElement(By.xpath(
                        "//th[@abbr='G/L Account No.']//a[@title='Open Menu']/ancestor::th"));

                new Actions(driver).contextClick(element).perform();
                Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
                Waits.waitUntilVisible(driver,
                                       By.xpath(
                                               "//p[text()='Only show lines where \"G/L Account No.\" is']/" +
                                               "ancestor::div[@class='ms-nav-band-container']//input")
                );
                ExtractorUtils.clearAndSetValue(driver,
                                                By.xpath(
                                                        "//p[text()='Only show lines where \"G/L Account No.\" is']/" +
                                                        "ancestor::div[@class='ms-nav-band-container']//input"),
                                                accountNum
                );
                Waits.waitUntilClickable(driver, By.xpath("//button[@title='OK']"));
                Waits.waitForLoad(driver);
                this.progressContainer.getStatus().setText("Scrolling");
                ExtractorUtils.scrollTable(driver, "General Ledger Entries");
                List<WebElement> rows = driver.findElements(By.xpath(
                        "//table[@summary = 'General Ledger Entries']/tbody/tr"));
                this.progressContainer.getProgressBar().setMaximum(Math.max(1, rows.size() * header.size()));
                this.progressContainer.getStatus().setText("Extracting");
                int progress = 0;
                for (WebElement row : rows) {
                    if (isCancelled()) {
                        return null;
                    }
                    List<WebElement> cells = row.findElements(By.xpath("./td[@aria-readonly]/*"));
                    List<Object> values = new ArrayList<>();
                    for (int i = 0; i < cells.size(); i++) {
                        if (isCancelled()) {
                            return null;
                        }
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
                        this.progressContainer.getProgressBar().setValue(progress++);
                    }
                    data.add(values);
                }
                driver.navigate().refresh();
                Waits.waitUntilVisible(driver, By.xpath("//h1[@title='General Ledger Entries']"));
                this.progressContainer.getProgressBar().setValue(0);
            }

            Map<String, List<List<Object>>> map = new GLMapper(header, data, deptCodes, this).map();
            this.progressContainer.getProgressBar().setValue(0);
            Path workbook = glEntryFolder.resolve("GLEntries-" + date.replace("/", "_").replace("..", "-") + ".xlsx");
            ExcelFormatter.makeGLWorkbook(workbook, map, this);
            this.progressContainer.getProgressBar().setValue(0);
            return null;
        }


        @Override
        protected void done() {
            driver.quit();
            if (isCancelled()) {
                this.progressContainer.getStatus().setText("Cancelled");
                enableUI();
            }
            else {
                this.getOverallProgress().setValue(this.getOverallProgress().getValue() + 1);
                this.progressContainer.getCompany().setText("");
                this.progressContainer.getPeriod().setText("");
                this.progressContainer.getStatus().setText("");
                this.progressContainer.getAccount().setText("");
                this.progressContainer.getProgressBar().setValue(0);
                if (this.getOverallProgress().getValue() == dates.size()) {
                    enableUI();
                }
                else {
                    if (!(selector.get() > dates.size() - 1)) {
                        TaskFromDate taskFromDate = new TaskFromDate(this.progressContainer,
                                                                     dates.get(selector.getAndIncrement()),
                                                                     company,
                                                                     this.getOverallProgress(),
                                                                     downloadDir
                        );
                        removeTask(this);
                        addTask(taskFromDate);
                        taskFromDate.execute();

                    }
                }
            }
            super.done();
        }

        private void enableUI() {
            ui.getBrowse().setEnabled(true);
            ui.getFileLineInputFromDate().setEnabled(true);
            ui.getExtract().setEnabled(true);
            ui.getMonthPanel1().setEnabled(true);
            ui.getCompanySelector().setEnabled(true);
            ui.getCancel().setEnabled(false);
        }
    }
}
