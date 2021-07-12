package org.balance;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.balance.data.mapping.Mapper;
import org.balance.data.objects.NavData;
import org.balance.data.objects.NavTables;
import org.balance.data.utils.DataUtils;
import org.balance.extractor.driver.Driver;
import org.balance.extractor.processes.LoginProcess;
import org.balance.extractor.processes.tb.TrialBalanceExtract;
import org.balance.extractor.utils.ExtractorUtils;
import org.balance.utils.Utils;
import org.balance.utils.concurrent.CustomExecutors;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Nicholas Curl
 */
public class Main {

    /**
     * The instance of the logger
     */
    private static final Logger       logger    = LogManager.getLogger(Main.class);
    private static final List<String> companies = new ArrayList<>(Arrays.asList("4826 Verne Road LLC",
                                                                                "Beach House Investments LLC",
                                                                                "Cajun Affiliates LLC",
                                                                                "Mahaffey Events & Tents LLC",
                                                                                "Mahaffey Industrial Contractor",
                                                                                "Mahaffey Tent & Awning Co. Inc",
                                                                                "Mahaffey USA LLC"
    ));

    public static void main(String[] args)
    throws URISyntaxException, IOException, InvalidFormatException, ParseException {
        Path downloadDir = ExtractorUtils.initialize();
        Driver driver = Driver.createDriver(downloadDir);
        LoginProcess.login(driver, Utils.encodeCompany(companies.get(6)));
        NavData data = TrialBalanceExtract.extract(driver, companies.get(6));
        Path companyFolder = data.getCompanyPath();
        Path trialBalanceFolder = companyFolder.resolve("Trial Balances");
        try {
            Files.createDirectories(trialBalanceFolder);
        }
        catch (IOException e) {
            logger.fatal("Unable to create trial balance folder {}", trialBalanceFolder, e);
            System.exit(1);
        }
        ExecutorService service = CustomExecutors.newFixedThreadPool(data.getData().size());
        ProgressBar progressBar = new ProgressBarBuilder().setTaskName("Mapping Trial Balances")
                                                          .setMaxRenderedLength(120)
                                                          .setUpdateIntervalMillis(1)
                                                          .setInitialMax(data.getData().size())
                                                          .setStyle(ProgressBarStyle.ASCII)
                                                          .build();
        for(NavTables tables : data.getData()){
            service.submit(() -> {
                List<List<Object>> map = Mapper.map(tables);
                File workbookFile = trialBalanceFolder.resolve(companyFolder.toFile().getName() +
                                                               "_Trial Balance Template_" +
                                                               tables.getDateString() +
                                                               ".xlsx").toFile();
                DataUtils.writeWorkbook(map, workbookFile);
                progressBar.step();
                return null;
            });
        }
        Utils.shutdownExecutor(service, logger);
        progressBar.close();
        driver.quit();
    }
}
