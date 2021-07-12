/*
 * Created by JFormDesigner on Tue Jul 06 08:22:58 CDT 2021
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
public class ProgressTB extends Progress {
	public ProgressTB() {
		initComponents();
	}


	@Override
	void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		var xLabel1 = new JXLabel();
		company = new JXLabel();
		this.setCompany(company);
		var separator1 = new JSeparator();
		var xLabel3 = new JXLabel();
		period = new JXLabel();
		this.setPeriod(period);
		var separator2 = new JSeparator();
		var xLabel5 = new JXLabel();
		status = new JXLabel();
		this.setStatus(status);
		var separator3 = new JSeparator();
		progressBar = new FlatProgressBar();
		this.setProgressBar(progressBar);

		//======== this ========
		setLayout(new GridLayoutManager(1, 10, new Insets(0, 0, 0, 0), 6, 6));

		//---- xLabel1 ----
		xLabel1.setText("Comapny:");
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

		//---- separator1 ----
		separator1.setOrientation(SwingConstants.VERTICAL);
		add(separator1, new GridConstraints(0, 2, 1, 1,
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

		//---- separator2 ----
		separator2.setOrientation(SwingConstants.VERTICAL);
		add(separator2, new GridConstraints(0, 5, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

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

		//---- separator3 ----
		separator3.setOrientation(SwingConstants.VERTICAL);
		add(separator3, new GridConstraints(0, 8, 1, 1,
			GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
			null, null, null));

		//---- progressBar ----
		progressBar.setStringPainted(true);
		add(progressBar, new GridConstraints(0, 9, 1, 1,
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
	private FlatProgressBar progressBar;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
