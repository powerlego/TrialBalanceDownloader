/*
 * Created by JFormDesigner on Thu Jul 01 09:52:53 CDT 2021
 */

package org.balance.extractor.gui.ui.components;

import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.utils.Utils;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Nicholas Curl
 */
public class MonthPanel extends JPanel {

    private static final Logger logger = LogManager.getLogger(MonthPanel.class);
    private              Date   date;
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private FlatComboBox<String> month;
    private FlatComboBox<String> year;

    public MonthPanel() {
        initComponents();
		setDate(LocalDate.now());
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        month.setSelectedIndex(calendar.get(Calendar.MONTH));
        year.getModel().setSelectedItem(String.valueOf(calendar.get(Calendar.YEAR)));
        this.date = date;
    }

    public void setDate(LocalDate date) {
        month.setSelectedIndex(date.getMonth().getValue() - 1);
        year.getModel().setSelectedItem(String.valueOf(date.getYear()));
        this.date = Utils.localDateToDate(date);
    }

    public FlatComboBox<String> getMonth() {
        return month;
    }

    public FlatComboBox<String> getYear() {
        return year;
    }

    private void monthActionPerformed(ActionEvent e) {
        parseDateAction(e);
    }

    private void yearActionPerformed(ActionEvent e) {
        parseDateAction(e);
    }

    private void parseDateAction(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("comboBoxChanged")) {
            parseDate();
        }
    }

    private void parseDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
        String month = (String) getMonth().getSelectedItem();
        String year = (String) getYear().getSelectedItem();
        String dateString = month + " " + year;
        try {
            this.date = simpleDateFormat.parse(dateString);
        }
        catch (ParseException exception) {
            logger.fatal("Unable to parse date", exception);
            System.exit(1);
        }
    }

    private void thisPropertyChange(PropertyChangeEvent e) {
        getMonth().setEnabled((boolean) e.getNewValue());
        getYear().setEnabled((boolean) e.getNewValue());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        var xLabel1 = new JXLabel();
        month = new FlatComboBox<>();
        var xLabel2 = new JXLabel();
        year = new FlatComboBox<>();

        //======== this ========
        addPropertyChangeListener("enabled", e -> thisPropertyChange(e));
        setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 6, 0));

        //---- xLabel1 ----
        xLabel1.setText("Month");
        add(xLabel1, new GridConstraints(0, 0, 1, 1,
                                         GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                         null, null, null
        ));

        //---- month ----
        month.setModel(new DefaultComboBoxModel<>(new String[]{
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        }));
        month.addActionListener(e -> monthActionPerformed(e));
        add(month, new GridConstraints(0, 1, 1, 1,
                                       GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       null, null, null
        ));

        //---- xLabel2 ----
        xLabel2.setText("Year");
        add(xLabel2, new GridConstraints(0, 2, 1, 1,
                                         GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                         null, null, null
        ));

        //---- year ----
        year.setModel(new DefaultComboBoxModel<>(new String[]{
                "2021",
                "2020",
                "2019",
                "2018",
                "2017",
                "2016",
                "2015",
                "2014",
                "2013",
                "2012",
                "2011",
                "2010",
                "2009",
                "2008",
                "2007",
                "2006",
                "2005",
                "2004",
                "2003",
                "2002",
                "2001",
                "2000",
                "1999",
                "1998",
                "1997",
                "1996",
                "1995",
                "1994",
                "1993",
                "1992",
                "1991",
                "1990",
                "1989",
                "1988",
                "1987",
                "1986",
                "1985",
                "1984",
                "1983",
                "1982",
                "1981",
                "1980",
                "1979",
                "1978",
                "1977",
                "1976",
                "1975",
                "1974",
                "1973",
                "1972",
                "1971",
                "1970"
        }));
        year.addActionListener(e -> yearActionPerformed(e));
        add(year, new GridConstraints(0, 3, 1, 1,
                                      GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                      null, null, null
        ));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
