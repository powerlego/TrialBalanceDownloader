/*
 * Created by JFormDesigner on Thu Jul 01 11:46:51 CDT 2021
 */

package org.balance.extractor.gui.ui.components;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicholas Curl
 */
public class ProgressGL extends Progress {
	public ProgressGL() {
		initComponents();
	}

	public JXLabel getAccount() {
		return account;
	}

	@Override
	void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		var xLabel1 = new JXLabel();
		company = new JXLabel();
		this.setCompany(company);
		var separator2 = new JSeparator();
		var xLabel3 = new JXLabel();
		period = new JXLabel();
		this.setPeriod(period);
		var separator1 = new JSeparator();
		var xLabel5 = new JXLabel();
		status = new JXLabel();
		this.setStatus(status);
		var separator4 = new JSeparator();
		var xLabel2 = new JXLabel();
		account = new JXLabel();
		var separator3 = new JSeparator();
		progressBar = new FlatProgressBar();
		this.setProgressBar(progressBar);

		//======== this ========
		setLayout(new GridLayoutManager(1, 13, new Insets(0, 0, 0, 0), 3, 0));

		//---- xLabel1 ----
		xLabel1.setText("Company:");
		add(xLabel1, new GridConstraints(0, 0, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));
		add(company, new GridConstraints(0, 1, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- separator2 ----
		separator2.setOrientation(SwingConstants.VERTICAL);
		add(separator2, new GridConstraints(0, 2, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- xLabel3 ----
		xLabel3.setText("Period:");
		add(xLabel3, new GridConstraints(0, 3, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));
		add(period, new GridConstraints(0, 4, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- separator1 ----
		separator1.setOrientation(SwingConstants.VERTICAL);
		add(separator1, new GridConstraints(0, 5, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			new Dimension(1, 1), null, null));

		//---- xLabel5 ----
		xLabel5.setText("Status:");
		add(xLabel5, new GridConstraints(0, 6, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));
		add(status, new GridConstraints(0, 7, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- separator4 ----
		separator4.setOrientation(SwingConstants.VERTICAL);
		add(separator4, new GridConstraints(0, 8, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- xLabel2 ----
		xLabel2.setText("Account:");
		add(xLabel2, new GridConstraints(0, 9, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));
		add(account, new GridConstraints(0, 10, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- separator3 ----
		separator3.setOrientation(SwingConstants.VERTICAL);
		add(separator3, new GridConstraints(0, 11, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
			null, null, null));

		//---- progressBar ----
		progressBar.setStringPainted(true);
		add(progressBar, new GridConstraints(0, 12, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JXLabel company;
	private JXLabel period;
	private JXLabel status;
	private JXLabel account;
	private FlatProgressBar progressBar;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
