package org.balance;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.balance.extractor.gui.ui.MainUI;
import org.jdesktop.swingx.plaf.basic.CalendarHeaderHandler;
import org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * @author Nicholas Curl
 */
public class Test {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Test.class);

    public static void main(String[] args)
    throws ParseException, URISyntaxException, IOException, InvalidFormatException {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        /*ExecutorService service = CustomExecutors.newFixedThreadPool(2);
        service.submit(()->{
            Driver driver = Driver.createDriver(Paths.get("./gl_testing"));
            LoginProcess.loginGLEntry(driver, "Mahaffey USA LLC");
            TrialBalanceExtract.extractGLEntries(driver, "Mahaffey USA LLC", "11/01/17..C11/30/17");
            driver.quit();
        });
        service.submit(()->{
            Driver driver = Driver.createDriver(Paths.get("./gl_testing"));
            LoginProcess.login(driver,"Mahaffey USA LLC");
            TrialBalanceExtract.extractAtDate(driver, "Mahaffey USA LLC", "10/31/17");
            driver.quit();
            driver = Driver.createDriver(Paths.get("./gl_testing"));
            LoginProcess.login(driver,"Mahaffey USA LLC");
            TrialBalanceExtract.extractAtDate(driver, "Mahaffey USA LLC", "11/30/17");
            driver.quit();
        });
        Utils.shutdownExecutor(service, logger);*/
        UIManager.put(
                CalendarHeaderHandler.uiControllerID,
                "org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler");
        UIManager.put(
                SpinningCalendarHeaderHandler.ARROWS_SURROUND_MONTH,
                Boolean.TRUE);
        SwingUtilities.invokeLater(Test::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        FlatDarculaLaf.setup();
        /*try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.fatal(e.getMessage(), e);
        }*/
        JFrame frame = new MainUI();
        /*JFrame frame = new JFrame();
        MonthPanel panel =new MonthPanel();
        panel.setEnabled(false);
        frame.add(panel);*/
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
