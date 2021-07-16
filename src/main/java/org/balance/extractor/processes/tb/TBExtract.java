package org.balance.extractor.processes.tb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.data.mapping.tb.TBMapper;
import org.balance.data.mapping.tb.TrialBalanceGLMapper;
import org.balance.data.mapping.template.TemplateMapper;
import org.balance.data.objects.Balances;
import org.balance.data.objects.GLEntries;
import org.balance.data.writing.tb.TBWriter;
import org.balance.data.writing.template.TemplateWriter;
import org.balance.extractor.driver.Driver;
import org.balance.extractor.gui.ui.components.ProgressGL;
import org.balance.extractor.gui.ui.tb.TrialBalance;
import org.balance.extractor.processes.Extractor;
import org.balance.extractor.processes.LoginProcess;
import org.balance.extractor.utils.ExtractorUtils;
import org.balance.extractor.utils.Waits;
import org.balance.utils.Utils;
import org.balance.utils.concurrent.CustomExecutors;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Nicholas Curl
 */
public class TBExtract extends Extractor {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(TBExtract.class);

    private final TrialBalance       ui;
    private final List<String>       datesGL;
    private final AtomicInteger      dateSelector;
    private final AtomicInteger      glSelector;
    private final AtomicInteger      tbSelector;
    private final ProgressGL[]       progressGLList;
    private final ThreadPoolExecutor service;

    public TBExtract(TrialBalance ui, List<String> datesGL) {
        super(new HashMap<>());
        this.ui = ui;
        this.datesGL = datesGL;
        this.dateSelector = new AtomicInteger();
        this.glSelector = new AtomicInteger();
        this.tbSelector = new AtomicInteger();
        this.service = (ThreadPoolExecutor) CustomExecutors.newFixedThreadPool(3);
        this.progressGLList = new ProgressGL[]{
                ui.getProgress1(),
                ui.getProgress2(),
                ui.getProgress3()
        };
        this.ui.getOverallProgressBar().setMaximum((2 * datesGL.size()) + 1);
    }

    @Override
    public void extract() {
        TBInitial initial = new TBInitial(ui.getProgressTB1(),
                                          "10/31/17",
                                          (String) this.ui.getCompanySelector().getSelectedItem(),
                                          this.ui.getOverallProgressBar(),
                                          this.ui.getFileChooser().getSelectedFile().toPath()
        );
        addTask(initial);
        service.execute(initial);
        for (int i = 0; i < 3; i++) {
            GLFromDate task = new GLFromDate(this.progressGLList[i],
                                             this.datesGL.get(dateSelector.getAndIncrement()),
                                             (String) this.ui.getCompanySelector().getSelectedItem(),
                                             this.ui.getOverallProgressBar(),
                                             this.ui.getFileChooser().getSelectedFile().toPath()
            );
            this.addTask(task);
            glSelector.getAndIncrement();
            service.execute(task);
            Utils.sleep(100);
        }
    }

    private void enableUI() {
        ui.getBrowse().setEnabled(true);
        ui.getFileLineInput().setEnabled(true);
        ui.getExtract().setEnabled(true);
        ui.getCompanySelector().setEnabled(true);
        ui.getCancel().setEnabled(false);
    }


    public class GLFromDate extends Task<GLEntries> {

        private final String     date;
        private final String     company;
        private final Path       downloadDir;
        private final ProgressGL progressContainer;
        private       Driver     driver;

        public GLFromDate(ProgressGL progressContainer,
                          String date,
                          String company,
                          JProgressBar overallProgress,
                          Path downloadDir
        ) {
            super(progressContainer, overallProgress, date);
            this.progressContainer = progressContainer;
            this.date = date;
            this.company = company;
            this.downloadDir = downloadDir;
        }

        private void filter(String accountNum)
        throws TimeoutException, NoSuchElementException, StaleElementReferenceException, InterruptedException {
            WebElement element
                    = driver.findElement(By.xpath(
                    "//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
            new Actions(driver).contextClick(element).perform();
            Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
            Waits.waitForLoad(driver);
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
            Waits.waitForLoad(driver);
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
        protected GLEntries doInBackground() throws Exception {
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
                    driver.quit();
                    return null;
                }
                this.progressContainer.getStatus().setText("Filtering");
                this.progressContainer.getAccount().setText(accountNum);
                try{
                    filter(accountNum);
                }
                catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver, company);
                    filter(accountNum);
                }
                this.progressContainer.getProgressBar().setValue(0);
                this.progressContainer.getStatus().setText("Scrolling");
                try {
                    Waits.waitUntilClickable(driver,By.xpath("//a[@title='Collapse the FactBox pane']"));
                } catch (TimeoutException e){
                    Waits.waitUntilClickable(driver,By.xpath("//a[@title='Expand the FactBox pane']"));
                }
                ExtractorUtils.scrollTable(driver, "General Ledger Entries");
                List<WebElement> rows = driver.findElements(By.xpath(
                        "//table[@summary = 'General Ledger Entries']/tbody/tr"));
                this.progressContainer.getProgressBar().setMaximum(Math.max(1, rows.size() * header.size()));
                this.progressContainer.getStatus().setText("Extracting");
                int progress = 0;
                for (WebElement row : rows) {
                    if (isCancelled()) {
                        driver.quit();
                        return null;
                    }
                    List<WebElement> cells = row.findElements(By.xpath("./td[@aria-readonly]/*"));
                    List<Object> values = new ArrayList<>();
                    for (int i = 0; i < cells.size(); i++) {
                        if (isCancelled()) {
                            driver.quit();
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
                try {
                    if(accountNumbers.indexOf(accountNum)== ((int) Math.ceil((accountNumbers.size()+1.0)/2)-1)){
                        driver.quit();
                        driver = Driver.createDriver(downloadDir);
                        LoginProcess.loginGLEntry(driver, company);
                    }else {
                        Waits.waitUntilVisible(driver, By.xpath("//h1[@title='General Ledger Entries']"));
                    }
                }
                catch (TimeoutException e) {
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver, company);
                }
                this.progressContainer.getProgressBar().setValue(0);
            }
            this.progressContainer.getAccount().setText("");
            GLEntries entry = new TrialBalanceGLMapper(date, data, deptCodes, accountNums, this).map();
            this.progressContainer.getProgressBar().setValue(0);
            return entry;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                this.progressContainer.getStatus().setText("Cancelled");
                this.progressContainer.getProgressBar().setValue(0);
                driver.quit();
            }
            else {
                try {
                    this.get();
                    driver.quit();
                    this.getOverallProgress().setValue(this.getOverallProgress().getValue() + 1);
                    this.progressContainer.getCompany().setText("");
                    this.progressContainer.getPeriod().setText("");
                    this.progressContainer.getStatus().setText("");
                    this.progressContainer.getAccount().setText("");
                    this.progressContainer.getProgressBar().setValue(0);
                    if (glSelector.get() < datesGL.size() - 1) {
                        if (!(dateSelector.get() > datesGL.size() - 1)) {
                            GLFromDate taskFromDate = new GLFromDate(this.progressContainer,
                                                                     datesGL.get(dateSelector.getAndIncrement()),
                                                                     company,
                                                                     this.getOverallProgress(),
                                                                     downloadDir
                            );
                            addTask(taskFromDate);
                            glSelector.getAndIncrement();
                            service.execute(taskFromDate);
                        }
                    }
                }
                catch (ExecutionException e) {
                    driver.quit();
                    logger.fatal(e.getMessage(), e);
                }
                catch (InterruptedException e) {
                    driver.quit();
                    Thread.currentThread().interrupt();
                }
            }
            super.done();
        }
    }

    public class TBInitial extends Task<Void> {

        private final String     date;
        private final String     company;
        private final Path       downloadDir;
        private final ProgressGL progressContainer;
        private       Driver     driver;

        public TBInitial(ProgressGL progressContainer,
                         String date,
                         String company,
                         JProgressBar overallProgress,
                         Path downloadDir
        ) {
            super(progressContainer, overallProgress, date);
            this.date = date;
            this.company = company;
            this.progressContainer = progressContainer;
            this.downloadDir = downloadDir;
        }

        private void filter(String accountNum, String period)
        throws TimeoutException, NoSuchElementException, StaleElementReferenceException, InterruptedException {
            WebElement element
                    = driver.findElement(By.xpath(
                    "//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
            new Actions(driver).contextClick(element).perform();
            Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
            Waits.waitForLoad(driver);
            Waits.waitUntilVisible(driver,
                                   By.xpath(
                                           "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                           "ancestor::div[@class='ms-nav-band-container']//input")
            );
            ExtractorUtils.clearAndSetValue(driver,
                                            By.xpath(
                                                    "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                                    "ancestor::div[@class='ms-nav-band-container']//input"),
                                            period
            );
            Waits.waitForLoad(driver);
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
            progressContainer.getCompany().setText(company);
            progressContainer.getPeriod().setText(date);
            progressContainer.getStatus().setText("Logging In");
            String period = "''..C" + date;
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
                    driver.quit();
                    return null;
                }
                this.progressContainer.getStatus().setText("Filtering");
                this.progressContainer.getAccount().setText(accountNum);
                try{
                    filter(accountNum,period);
                }
                catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver, company);
                    filter(accountNum,period);
                }
                this.progressContainer.getProgressBar().setValue(0);
                this.progressContainer.getStatus().setText("Scrolling");
                try {
                    Waits.waitUntilClickable(driver,By.xpath("//a[@title='Collapse the FactBox pane']"));
                } catch (TimeoutException e){
                    Waits.waitUntilClickable(driver,By.xpath("//a[@title='Expand the FactBox pane']"));
                }
                ExtractorUtils.scrollTable(driver, "General Ledger Entries");
                List<WebElement> rows = driver.findElements(By.xpath(
                        "//table[@summary = 'General Ledger Entries']/tbody/tr"));
                this.progressContainer.getProgressBar().setMaximum(Math.max(1, rows.size() * header.size()));
                this.progressContainer.getStatus().setText("Extracting");
                int progress = 0;
                for (WebElement row : rows) {
                    if (isCancelled()) {
                        driver.quit();
                        return null;
                    }
                    List<WebElement> cells = row.findElements(By.xpath("./td[@aria-readonly]/*"));
                    List<Object> values = new ArrayList<>();
                    for (int i = 0; i < cells.size(); i++) {
                        if (isCancelled()) {
                            driver.quit();
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
                try {
                    if(accountNumbers.indexOf(accountNum)== ((int) Math.ceil((accountNumbers.size()+1.0)/2)-1)){
                        driver.quit();
                        driver = Driver.createDriver(downloadDir);
                        LoginProcess.loginGLEntry(driver, company);
                    }else {
                        Waits.waitUntilVisible(driver, By.xpath("//h1[@title='General Ledger Entries']"));
                    }
                }
                catch (TimeoutException e) {
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver, company);
                }
                this.progressContainer.getProgressBar().setValue(0);
            }
            this.progressContainer.getAccount().setText("");
            this.progressContainer.getProgressBar().setValue(0);
            if (!isCancelled()) {
                progressContainer.getStatus().setText("Mapping");
                Balances initialBalance = new TBMapper(date, data, deptCodes, accountNums, this).map();
                Path tbFolder = companyFolder.resolve("Trial Balances/");
                if (!tbFolder.toFile().exists()) {
                    try {
                        Files.createDirectories(tbFolder);
                    }
                    catch (IOException e) {
                        logger.fatal("Unable to create directory {}", tbFolder, e);
                        driver.quit();
                        System.exit(1);
                    }
                }
                Path file = tbFolder.resolve(company + "_" + date.replace("/", "_") + ".xlsx");
                progressContainer.getStatus().setText("Writing");
                new TBWriter(file, initialBalance, this).makeWorkbook();
                progressContainer.getStatus().setText("Mapping To Template");
                List<List<Object>> mapped = new TemplateMapper(this, initialBalance).map();
                Path templateFile = tbFolder.resolve(company +
                                                     "_Trial Balance Template_" +
                                                     date.replace("/", "_") +
                                                     ".xlsx");
                progressContainer.getStatus().setText("Writing");
                new TemplateWriter(this, templateFile, mapped).makeWorkbook();
                progressContainer.getStatus().setText("Waiting for next month");
                progressContainer.getProgressBar().setStringPainted(false);
                progressContainer.getProgressBar().setIndeterminate(true);
                try {
                    String[] split = date.split("C");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                    LocalDate current;
                    try {
                        current = LocalDate.from(formatter.parse(split[1]));
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        current = LocalDate.from(formatter.parse(split[0]));
                    }
                    LocalDate nextMonth = current.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                    if (getTasks().get(formatter.format(nextMonth)) instanceof GLFromDate) {
                        GLFromDate gl = (GLFromDate) getTasks().get(formatter.format(nextMonth));
                        service.setMaximumPoolSize(4);
                        service.setCorePoolSize(4);
                        GLEntries nextMonthEntry = gl.get();
                        SwingUtilities.invokeLater(() -> {
                            progressContainer.getProgressBar().setStringPainted(true);
                            progressContainer.getProgressBar().setIndeterminate(false);
                        });
                        TBFromDate tbFromDate = new TBFromDate(progressContainer,
                                                               formatter.format(nextMonth),
                                                               company,
                                                               nextMonthEntry,
                                                               initialBalance,
                                                               getOverallProgress(),
                                                               downloadDir
                        );
                        addTask(tbFromDate);
                        tbSelector.getAndIncrement();
                        service.execute(tbFromDate);
                    }
                    else {
                        ui.getCancel().doClick();
                    }
                }
                catch (ExecutionException e) {
                    driver.quit();
                    logger.fatal(e.getMessage(), e);
                    throw e;
                }
                catch (InterruptedException e) {
                    driver.quit();
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
            return null;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                driver.quit();
                progressContainer.getStatus().setText("Canceled");
                progressContainer.getProgressBar().setValue(0);
            }
            else {
                try {
                    this.get();
                    driver.quit();
                    this.getOverallProgress().setValue(this.getOverallProgress().getValue() + 1);
                    removeTask(this);
                }
                catch (ExecutionException e) {
                    driver.quit();
                    logger.fatal(e.getMessage(), e);
                    progressContainer.getStatus().setText("Canceled");
                    progressContainer.getProgressBar().setValue(0);
                    ui.getCancel().doClick();
                }
                catch (InterruptedException e) {
                    driver.quit();
                    Thread.currentThread().interrupt();
                    progressContainer.getStatus().setText("Canceled");
                    progressContainer.getProgressBar().setValue(0);
                }
            }
            super.done();
        }
    }

    public class TBFromDate extends Task<Void> {

        private final String     date;
        private final String     company;
        private final Path       downloadDir;
        private final ProgressGL progressContainer;
        private final GLEntries  entry;
        private final Balances   prevBalance;

        public TBFromDate(ProgressGL progressContainer,
                          String date,
                          String company,
                          GLEntries entry,
                          Balances prevBalance,
                          JProgressBar overallProgress,
                          Path downloadDir
        ) {
            super(progressContainer, overallProgress, date);
            this.date = date;
            this.company = company;
            this.progressContainer = progressContainer;
            this.downloadDir = downloadDir;
            this.entry = entry;
            this.prevBalance = prevBalance;
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
            Path companyFolder = downloadDir.resolve(company);
            if (!companyFolder.toFile().exists()) {
                try {
                    Files.createDirectories(companyFolder);
                }
                catch (IOException e) {
                    logger.fatal("Unable to create company folder {}", companyFolder, e);
                    System.exit(1);
                }
            }
            String[] split = date.split("C");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
            LocalDate current;
            try {
                current = LocalDate.from(formatter.parse(split[1]));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                current = LocalDate.from(formatter.parse(split[0]));
            }
            this.progressContainer.getPeriod().setText(formatter.format(current));
            List<String> accountNums = prevBalance.getAccountNums();
            List<String> depts = prevBalance.getDepts();
            Map<String, Map<String, BigDecimal>> prevDebit = prevBalance.getDebit();
            Map<String, Map<String, BigDecimal>> prevCredit = prevBalance.getCredit();
            Map<String, Map<String, BigDecimal>> debitChanges = entry.getDebit();
            Map<String, Map<String, BigDecimal>> creditChanges = entry.getCredit();
            Map<String, Map<String, BigDecimal>> debit = new HashMap<>();
            Map<String, Map<String, BigDecimal>> credit = new HashMap<>();
            for (String accountNum : accountNums) {
                if (isCancelled()) {
                    return null;
                }
                Map<String, BigDecimal> prevDebitAccount = prevDebit.get(accountNum);
                Map<String, BigDecimal> prevCreditAccount = prevCredit.get(accountNum);
                Map<String, BigDecimal> debitChangeAccount = debitChanges.get(accountNum);
                Map<String, BigDecimal> creditChangeAccount = creditChanges.get(accountNum);
                Map<String, BigDecimal> newDebitAccount = new HashMap<>();
                Map<String, BigDecimal> newCreditAccount = new HashMap<>();
                for (String dept : depts) {
                    if (isCancelled()) {
                        return null;
                    }
                    BigDecimal prevDebitValue = prevDebitAccount.get(dept);
                    BigDecimal prevCreditValue = prevCreditAccount.get(dept);
                    BigDecimal debitChangeValue = debitChangeAccount.get(dept);
                    BigDecimal creditChangeValue = creditChangeAccount.get(dept);
                    BigDecimal newDebitValue = prevDebitValue.add(debitChangeValue);
                    BigDecimal newCreditValue = prevCreditValue.add(creditChangeValue);
                    newDebitAccount.put(dept, newDebitValue);
                    newCreditAccount.put(dept, newCreditValue);
                }
                debit.put(accountNum, newDebitAccount);
                credit.put(accountNum, newCreditAccount);
            }
            if (!isCancelled()) {
                Balances balance = new Balances(formatter.format(current),
                                                prevBalance.getHeader(),
                                                accountNums,
                                                depts,
                                                debit,
                                                credit
                );
                Path tbFolder = companyFolder.resolve("Trial Balances/");
                if (!tbFolder.toFile().exists()) {
                    try {
                        Files.createDirectories(tbFolder);
                    }
                    catch (IOException e) {
                        logger.fatal("Unable to create directory {}", tbFolder, e);
                        System.exit(1);
                    }
                }
                Path file = tbFolder.resolve(company + "_" + formatter.format(current).replace("/", "_") + ".xlsx");
                progressContainer.getStatus().setText("Writing");
                new TBWriter(file, balance, this).makeWorkbook();
                progressContainer.getStatus().setText("Mapping To Template");
                List<List<Object>> mapped = new TemplateMapper(this, balance).map();
                Path templateFile = tbFolder.resolve(company +
                                                     "_Trial Balance Template_" +
                                                     formatter.format(current).replace("/", "_") +
                                                     ".xlsx");
                progressContainer.getStatus().setText("Writing");
                new TemplateWriter(this, templateFile, mapped).makeWorkbook();
                progressContainer.getStatus().setText("Waiting for next month");
                progressContainer.getProgressBar().setStringPainted(false);
                progressContainer.getProgressBar().setIndeterminate(true);
                try {
                    LocalDate nextMonth = current.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                    GLEntries nextMonthEntry = (GLEntries) getTasks().get(formatter.format(nextMonth)).get();
                    SwingUtilities.invokeLater(() -> {
                        progressContainer.getProgressBar().setStringPainted(true);
                        progressContainer.getProgressBar().setIndeterminate(false);
                    });
                    TBFromDate tbFromDate = new TBFromDate(progressContainer,
                                                           formatter.format(nextMonth),
                                                           company,
                                                           nextMonthEntry,
                                                           balance,
                                                           getOverallProgress(),
                                                           downloadDir
                    );
                    addTask(tbFromDate);
                    tbSelector.getAndIncrement();
                    service.execute(tbFromDate);
                }
                catch (ExecutionException e) {
                    logger.fatal(e.getMessage(), e);
                    throw e;
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
            return null;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                progressContainer.getStatus().setText("Canceled");
                progressContainer.getProgressBar().setValue(0);
            }
            else {
                try {
                    this.get();
                    this.getOverallProgress().setValue(this.getOverallProgress().getValue() + 1);
                    removeTask(this);
                    if (tbSelector.get() > datesGL.size()) {
                        Utils.shutdownExecutor(service, logger);
                    }
                }
                catch (ExecutionException e) {
                    logger.fatal(e.getMessage(), e);
                    progressContainer.getStatus().setText("Canceled");
                    progressContainer.getProgressBar().setValue(0);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            super.done();
        }
    }
}
