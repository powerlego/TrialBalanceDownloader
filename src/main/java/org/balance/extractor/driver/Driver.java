package org.balance.extractor.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * @author Nicholas Curl
 */
public class Driver extends ChromeDriver {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Driver.class);


    private final Path downloadDir;

    public Driver(Path downloadDir) {
        super();
        this.downloadDir = downloadDir;
    }

    public Driver(ChromeDriverService service, Path downloadDir) {
        super(service);
        this.downloadDir = downloadDir;
    }

    @Deprecated
    public Driver(Capabilities capabilities, Path downloadDir) {
        super(capabilities);
        this.downloadDir = downloadDir;
    }

    public Driver(ChromeOptions options, Path downloadDir) {
        super(options);
        this.downloadDir = downloadDir;
    }

    public Driver(ChromeDriverService service, ChromeOptions options, Path downloadDir) {
        super(service, options);
        this.downloadDir = downloadDir;
    }

    @Deprecated
    public Driver(ChromeDriverService service, Capabilities capabilities, Path downloadDir) {
        super(service, capabilities);
        this.downloadDir = downloadDir;
    }

    public static Driver createDriver(Path downloadDir) {
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        ChromeDriverService service = new ChromeDriverService.Builder().withSilent(true).build();
        service.sendOutputTo(OutputStream.nullOutputStream());
        ChromeOptions chromeOptions = getOptions(downloadDir);
        Driver driver = new Driver(service, chromeOptions, downloadDir);
        driver.manage().window().maximize();
        return driver;
    }

    public static ChromeOptions getOptions(Path downloadDirPath) {
        String download_dir = downloadDirPath.toString();
        ChromeOptions chromeOptions = new ChromeOptions();
        JSONObject settings = new JSONObject(
                "{\n" +
                "   \"recentDestinations\": [\n" +
                "       {\n" +
                "           \"id\": \"Save as PDF\",\n" +
                "           \"origin\": \"local\",\n" +
                "           \"account\": \"\",\n" +
                "       }\n" +
                "   ],\n" +
                "   \"selectedDestinationId\": \"Save as PDF\",\n" +
                "   \"version\": 2\n" +
                "}");
        JSONObject prefs = new JSONObject(
                "{\n" +
                "   \"plugins.plugins_list\":\n" +
                "       [\n" +
                "           {\n" +
                "               \"enabled\": False,\n" +
                "               \"name\": \"Chrome PDF Viewer\"\n" +
                "          }\n" +
                "       ],\n" +
                "   \"download.extensions_to_open\": \"applications/pdf\"\n" +
                "}")
                .put("printing.print_preview_sticky_settings.appState", settings)
                .put("download.default_directory", download_dir);
        chromeOptions.setExperimentalOption("prefs", prefs);
        chromeOptions.setHeadless(true);
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        chromeOptions.addArguments("--window-size=1920,1080",
                                   "--disable-crash-reporter",
                                   "--no-sandbox",
                                   "--disable-extensions",
                                   "--disable-in-process-stack-traces",
                                   "--disable-logging",
                                   "--disable-dev-shm-usage",
                                   "--log-level=3"
        );
        return chromeOptions;
    }

    public Path getDownloadDir() {
        return downloadDir;
    }

}
