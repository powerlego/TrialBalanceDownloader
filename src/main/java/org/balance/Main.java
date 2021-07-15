package org.balance;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.extractor.gui.ui.MainUI;
import org.jdesktop.swingx.plaf.basic.CalendarHeaderHandler;
import org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler;

import javax.swing.*;

/**
 * @author Nicholas Curl
 */
public class Main {

    /**
     * The instance of the logger
     */
    private static final Logger       logger    = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        UIManager.put(
                CalendarHeaderHandler.uiControllerID,
                "org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler");
        UIManager.put(
                SpinningCalendarHeaderHandler.ARROWS_SURROUND_MONTH,
                Boolean.TRUE);
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
    private static void createAndShowGUI() {
        FlatDarculaLaf.setup();
        JFrame frame = new MainUI();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
