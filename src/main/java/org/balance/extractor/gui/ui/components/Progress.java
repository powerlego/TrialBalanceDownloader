package org.balance.extractor.gui.ui.components;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;

/**
 * @author Nicholas Curl
 */
public abstract class Progress extends JPanel {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Progress.class);
    private JXLabel         company;
    private JXLabel         period;
    private JXLabel         status;
    private FlatProgressBar progressBar;

    public JXLabel getCompany() {
        return company;
    }

    protected void setCompany(JXLabel company) {
        this.company = company;
    }

    public JXLabel getPeriod() {
        return period;
    }

    protected void setPeriod(JXLabel period) {
        this.period = period;
    }

    public JXLabel getStatus() {
        return status;
    }

    protected void setStatus(JXLabel status) {
        this.status = status;
    }

    public FlatProgressBar getProgressBar() {
        return progressBar;
    }

    protected void setProgressBar(FlatProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    abstract void initComponents();

}
