package org.balance.utils;

import com.google.common.net.UrlEscapers;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Nicholas Curl
 */
public class Utils {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Utils.class);

    public static ProgressBar createProgressBar(String taskName, int initialMax) {
        return new ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII)
                                       .setMaxRenderedLength(120)
                                       .setUpdateIntervalMillis(500)
                                       .setTaskName(taskName)
                                       .setInitialMax(initialMax)
                                       .build();
    }

    public static String encodeCompany(String company) {
        return UrlEscapers.urlFragmentEscaper().escape(company).replace("&", "%26");
    }

    public static void shutdownExecutor(ExecutorService executorService, Logger logger) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                logger.warn("Termination Timeout");
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static <T> List<T> trimList(List<T> list) {
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static LocalDate dateToLocalDate(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localDateToDate(LocalDate date){
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static List<String> generateMonthlyDates(Date startDate){
        LocalDate localStartDate = dateToLocalDate(startDate);
        return generateMonthlyDates(localStartDate);
    }

    public static List<String> generateMonthlyDates(LocalDate startDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        List<String> dates = new ArrayList<>();
        startDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate now = LocalDate.now();
        while (startDate.isBefore(now) && !startDate.isEqual(now)) {
            dates.add(startDate.with(TemporalAdjusters.firstDayOfMonth()).format(formatter) +
                      "..C" +
                      startDate.with(TemporalAdjusters.lastDayOfMonth()).format(formatter));
            startDate = startDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
        return dates;
    }
}
