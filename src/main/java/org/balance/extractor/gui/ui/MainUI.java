/*
 * Created by JFormDesigner on Tue Jun 29 08:26:48 CDT 2021
 */

package org.balance.extractor.gui.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.extractor.gui.ui.gl.GLAtDate;
import org.balance.extractor.gui.ui.gl.GLFromDate;
import org.balance.extractor.gui.ui.tb.TrialBalance;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicholas Curl
 */
public class MainUI extends JXFrame {
    private static final Logger logger = LogManager.getLogger(MainUI.class);
    public MainUI() {
        initComponents();
    }

    public MainUI getInstance(){
        return this;
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		var tabbedPane1 = new JTabbedPane();
		var tbMain = new TBMain();
		var glMain = new GLMain();

		//======== this ========
		setTitle("Trial Balance Extractor");
		setMinimumSize(new Dimension(800, 10));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		var contentPane = getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		//======== tabbedPane1 ========
		{
			tabbedPane1.setMinimumSize(new Dimension(800, 10));
			tabbedPane1.addTab("Trial Balance Extract", tbMain);
			tabbedPane1.addTab("G/L Extract", glMain);
		}
		contentPane.add(tabbedPane1);
		setSize(835, 550);
		setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public static class GLMain extends JXPanel {
        private GLMain() {
            initComponents();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			var tabbedPane2 = new JTabbedPane();
			var gLFromDate1 = new GLFromDate();
			var gLAtDate1 = new GLAtDate();

			//======== this ========
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			//======== tabbedPane2 ========
			{
				tabbedPane2.addTab("Run From Month", gLFromDate1);
				tabbedPane2.addTab("Run At Month", gLAtDate1);
			}
			add(tabbedPane2);
            // JFormDesigner - End of component initialization  //GEN-END:initComponents
        }

        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
		// Generated using JFormDesigner non-commercial license
        // JFormDesigner - End of variables declaration  //GEN-END:variables
    }

    public static class TBMain extends JPanel {
        private TBMain() {
            initComponents();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			var trialBalance1 = new TrialBalance();

			//======== this ========
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(trialBalance1);
            // JFormDesigner - End of component initialization  //GEN-END:initComponents
        }

        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
		// Generated using JFormDesigner non-commercial license
        // JFormDesigner - End of variables declaration  //GEN-END:variables
    }
}
