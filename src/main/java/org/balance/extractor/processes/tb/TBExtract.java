package org.balance.extractor.processes.tb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.balance.data.mapping.tb.TBMapper;
import org.balance.data.mapping.tb.TrialBalanceGLMapper;
import org.balance.data.objects.Balances;
import org.balance.data.objects.GLEntries;
import org.balance.data.writing.tb.TBWriter;
import org.balance.extractor.driver.Driver;
import org.balance.extractor.gui.ui.components.ProgressGL;
import org.balance.extractor.gui.ui.tb.TrialBalance;
import org.balance.extractor.processes.Extractor;
import org.balance.extractor.processes.LoginProcess;
import org.balance.extractor.utils.ExtractorUtils;
import org.balance.extractor.utils.Waits;
import org.balance.utils.Utils;
import org.balance.utils.concurrent.CustomExecutors;
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

    private final TrialBalance     ui;
    private final List<String>     datesGL;
    private final AtomicInteger    dateSelector;
    private final AtomicInteger    glSelector;
    private final AtomicInteger tbSelector;
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
                WebElement element
                        = driver.findElement(By.xpath("//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
                new Actions(driver).contextClick(element).perform();
                Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
                Waits.waitForLoad(driver);
                try {
                    Waits.waitUntilVisible(driver,
                                           By.xpath(
                                                   "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                                   "ancestor::div[@class='ms-nav-band-container']//input")
                    );
                } catch (TimeoutException e){
                    new Actions(driver).sendKeys(Keys.ESCAPE).perform();
                    element
                            = driver.findElement(By.xpath("//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
                    new Actions(driver).contextClick(element).perform();
                    Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
                    Waits.waitForLoad(driver);
                    Waits.waitUntilVisible(driver,
                                           By.xpath(
                                                   "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                                   "ancestor::div[@class='ms-nav-band-container']//input")
                    );
                }
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
                this.progressContainer.getStatus().setText("Scrolling");
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
                try{
                    Waits.waitUntilVisible(driver, By.xpath("//h1[@title='General Ledger Entries']"));
                } catch (TimeoutException e){
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver,company);
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
            driver.quit();
            if (isCancelled()) {
                this.progressContainer.getStatus().setText("Cancelled");
                enableUI();
            }
            else {
                try {
                    this.get();
                }
                catch (ExecutionException e) {
                    driver.quit();
                    logger.fatal(e.getMessage(), e);
                }
                catch (InterruptedException e) {
                    driver.quit();
                    Thread.currentThread().interrupt();
                }
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
            /*Path companyFolder = driver.getDownloadDir().resolve(company);
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
            LoginProcess.login(driver, company);
            List<List<Object>> debitTable = getBalances("Debit");
            if(isCancelled()){
                driver.quit();
                return null;
            }
            List<List<Object>> creditTable = getBalances("Credit");
            if(isCancelled()){
                driver.quit();
                return null;
            }
            driver.quit();
            List<String> accountNums = new ArrayList<>();
            List<String> depts = new ArrayList<>();
            for (int i = 1; i < debitTable.size(); i++) {
                if (isCancelled()) {
                    driver.quit();
                    return null;
                }
                List<Object> objects = debitTable.get(i);
                accountNums.add(objects.get(0) + " " + objects.get(1));
            }
            for (int i = 3; i < debitTable.get(0).size(); i++) {
                if (isCancelled()) {
                    driver.quit();
                    return null;
                }
                depts.add((String) debitTable.get(0).get(i));
            }*/
            String period = "''..C"+date;
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
                WebElement element
                        = driver.findElement(By.xpath("//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
                new Actions(driver).contextClick(element).perform();
                Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
                Waits.waitForLoad(driver);
                try {
                    Waits.waitUntilVisible(driver,
                                           By.xpath(
                                                   "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                                   "ancestor::div[@class='ms-nav-band-container']//input")
                    );
                } catch (TimeoutException e){
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver, company);
                    element
                            = driver.findElement(By.xpath("//th[@abbr='Posting Date']//a[@title='Open Menu']/ancestor::th"));
                    new Actions(driver).contextClick(element).perform();
                    Waits.waitUntilClickable(driver, By.xpath("//a[@title='Filter...']"));
                    Waits.waitForLoad(driver);
                    Waits.waitUntilVisible(driver,
                                           By.xpath(
                                                   "//p[text()='Only show lines where \"Posting Date\" is']/" +
                                                   "ancestor::div[@class='ms-nav-band-container']//input")
                    );
                }
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
                this.progressContainer.getStatus().setText("Scrolling");
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
                try{
                    Waits.waitUntilVisible(driver, By.xpath("//h1[@title='General Ledger Entries']"));
                } catch (TimeoutException e){
                    driver.quit();
                    driver = Driver.createDriver(downloadDir);
                    LoginProcess.loginGLEntry(driver,company);
                }
                this.progressContainer.getProgressBar().setValue(0);
            }
            this.progressContainer.getAccount().setText("");
            //GLEntries entry = new TrialBalanceGLMapper(date, data, deptCodes, accountNums, this).map();
            this.progressContainer.getProgressBar().setValue(0);
            if (!isCancelled()) {
                progressContainer.getStatus().setText("Mapping");
                Balances initialBalance = new TBMapper(date,data,deptCodes,accountNums,this).map();
                //Balances initialBalance = new TBInitialMapper(this,date, depts, accountNums, debitTable,creditTable).map();
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
                Path file = tbFolder.resolve(company + "_" + date.replace("/", "_") + ".xlsx");
                progressContainer.getStatus().setText("Writing");
                new TBWriter(file, initialBalance, this).makeWorkbook();
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
                        progressContainer.getProgressBar().setStringPainted(true);
                        progressContainer.getProgressBar().setIndeterminate(false);
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
                }
                catch (InterruptedException e) {
                    driver.quit();
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                driver.quit();
                enableUI();
                progressContainer.getStatus().setText("Canceled");
                progressContainer.getProgressBar().setValue(0);
            }
            else {
                try {
                    this.get();
                }
                catch (ExecutionException e) {
                    driver.quit();
                    logger.fatal(e.getMessage(), e);
                }
                catch (InterruptedException e) {
                    driver.quit();
                    Thread.currentThread().interrupt();
                }
                this.getOverallProgress().setValue(this.getOverallProgress().getValue() + 1);
                removeTask(this);
            }
            super.done();
        }

        private List<List<Object>> getBalances(String showAmount) {
            int progress = 0;
            ui.getProgressTB1().getStatus().setText("Filtering " + showAmount);
            String newDateString = "''..C" + date;
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
            try {
                Waits.waitUntilVisible(driver, By.xpath("//table[@summary='G/L Balance by Dim. Matrix']"));
            } catch (TimeoutException e){
                Waits.waitUntilClickable(driver, By.xpath("//span[contains(text(),'Show')]/.."));
                Waits.waitUntilVisible(driver, By.xpath("//table[@summary='G/L Balance by Dim. Matrix']"));
            }
            //endregion
            //region Show Every Row
            ui.getProgressTB1().getStatus().setText("Scrolling " + showAmount);
            do {
                if (isCancelled()) {
                    driver.quit();
                    return new ArrayList<>();
                }
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
                if (isCancelled()) {
                    driver.quit();
                    return new ArrayList<>();
                }
                String value = headerElement.getAttribute("abbr");
                header.add(value);
            }
            table.add(header);
            List<WebElement> rows
                    = driver.findElements(By.xpath("//table[@summary='G/L Balance by Dim. Matrix']/tbody/tr"));
            ui.getProgressTB1().getProgressBar().setMaximum(rows.size());
            ui.getProgressTB1().getStatus().setText("Extracting " + showAmount);
            for (WebElement row : rows) {
                if (isCancelled()) {
                    driver.quit();
                    return new ArrayList<>();
                }
                List<WebElement> cells = row.findElements(By.xpath(
                        "./td[not(@class='ms-nav-hidden') and @aria-readonly]"));
                List<Object> tableRow = new ArrayList<>();
                for (int i = 0; i < cells.size(); i++) {
                    if (isCancelled()) {
                        driver.quit();
                        return new ArrayList<>();
                    }
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
                ui.getProgressTB1().getProgressBar().setValue(progress++);
            }
            //endregion
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            ui.getProgressTB1().getProgressBar().setValue(0);
            return table.stream()
                        .filter(objects -> Strings.isNotBlank((String) objects.get(0)))
                        .collect(Collectors.toList());
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
            //depts.remove(0);
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
                Path tbFolder = downloadDir.resolve("Trial Balances/");
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
                progressContainer.getStatus().setText("Waiting for next month");
                progressContainer.getProgressBar().setStringPainted(false);
                progressContainer.getProgressBar().setIndeterminate(true);
                try {
                    LocalDate nextMonth = current.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                    GLEntries nextMonthEntry = (GLEntries) getTasks().get(formatter.format(nextMonth)).get();
                    progressContainer.getProgressBar().setStringPainted(true);
                    progressContainer.getProgressBar().setIndeterminate(false);
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
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                enableUI();
                progressContainer.getStatus().setText("Canceled");
                progressContainer.getProgressBar().setValue(0);
            }
            else {
                try {
                    this.get();
                }
                catch (ExecutionException e) {
                    logger.fatal(e.getMessage(), e);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                this.getOverallProgress().setValue(this.getOverallProgress().getValue() + 1);
                removeTask(this);
                if(tbSelector.get()>datesGL.size()){
                    Utils.shutdownExecutor(service,logger);
                }
            }
            super.done();
        }
    }
}
