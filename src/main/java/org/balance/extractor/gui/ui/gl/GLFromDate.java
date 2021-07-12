/*
 * Created by JFormDesigner on Wed Jun 30 15:57:55 CDT 2021
 */

package org.balance.extractor.gui.ui.gl;

import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.formdev.flatlaf.extras.components.FlatProgressBar;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.balance.extractor.gui.ui.components.MonthPanel;
import org.balance.extractor.gui.ui.components.ProgressGL;
import org.balance.extractor.gui.verification.FileVerifier;
import org.balance.extractor.processes.gl.GLExtractFromDate;
import org.balance.utils.Utils;
import org.jdesktop.swingx.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Nicholas Curl
 */
public class GLFromDate extends JPanel {
    private static final Logger      logger = LogManager.getLogger(GLFromDate.class);
    private              GLExtractFromDate extractor;

    public GLFromDate getInstance(){
        return this;
    }

    public GLFromDate() {
        initComponents();
    }

    public JXTextField getFileLineInputFromDate() {
        return this.fileLineInputFromDate;
    }

    public JXButton getBrowse() {
        return this.browse;
    }

    public JXButton getExtract() {
        return this.extract;
    }

    public JXButton getCancel() {
        return this.cancel;
    }

    public FlatProgressBar getOverallProgressBar() {
        return this.overallProgressBar;
    }

	public FlatComboBox<String> getCompanySelector() {
		return companySelector;
	}

	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	public MonthPanel getMonthPanel1() {
		return monthPanel1;
	}

	public ProgressGL getProgress1() {
		return progress1;
	}

	public ProgressGL getProgress2() {
		return progress2;
	}

	public ProgressGL getProgress3() {
		return progress3;
	}

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		var panel2 = new JXPanel();
		var label1 = new JXLabel();
		var panel1 = new JXPanel();
		var panel5 = new JXPanel();
		var fileSelect = new JXPanel();
		var outputDirectoryLabel = new JXLabel();
		fileLineInputFromDate = new JXTextField();
		browse = new JXButton();
		var startDateSelection = new JXPanel();
		var startDateLabel = new JLabel();
		var hSpacer1 = new Spacer();
		monthPanel1 = new MonthPanel();
		var extractLayout = new JXPanel();
		var companySelect = new JXLabel();
		companySelector = new FlatComboBox<>();
		var hSpacer2 = new Spacer();
		var panel4 = new JPanel();
		extract = new JXButton();
		cancel = new JXButton();
		var separator1 = new JSeparator();
		var panel3 = new JXPanel();
		var xLabel6 = new JXLabel();
		var prog1 = new JXPanel();
		var xPanel1 = new JXPanel();
		var xLabel11 = new JXLabel();
		var separator11 = new JSeparator();
		overallProgressBar = new FlatProgressBar();
		progress1 = new ProgressGL();
		progress2 = new ProgressGL();
		progress3 = new ProgressGL();
		fileChooser = new JFileChooser();
		var fileLine = new TextFileLine();
		var browseAction = new Browse();
		var extractAction = new Extract();
		var cancelAction = new Cancel();

		//======== this ========
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//======== panel2 ========
		{
			panel2.setLayout(new GridLayoutManager(1, 2, new Insets(6, 6, 6, 6), 12, 0));

			//---- label1 ----
			label1.setText("<html><p>G/L Entry Extractor<br>From Month</p></html>");
			label1.setFont(new Font("Segoe UI", Font.BOLD, 26));
			label1.setMaxLineSpan(1);
			label1.setPreferredSize(new Dimension(86, 81));
			label1.setMinimumSize(new Dimension(250, 81));
			label1.setHorizontalAlignment(SwingConstants.LEFT);
			label1.setMaximumSize(new Dimension(2147483647, 27));
			label1.setLineWrap(true);
			panel2.add(label1, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				null, null, null));

			//======== panel1 ========
			{
				panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 6, 0));

				//======== panel5 ========
				{
					panel5.setLayout(new VerticalLayout(6));

					//======== fileSelect ========
					{
						fileSelect.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));

						//---- outputDirectoryLabel ----
						outputDirectoryLabel.setText("Save Location");
						outputDirectoryLabel.setLabelFor(fileLineInputFromDate);
						fileSelect.add(outputDirectoryLabel, new GridConstraints(0, 0, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));

						//---- fileLineInputFromDate ----
						fileLineInputFromDate.setAction(fileLine);
						fileLineInputFromDate.setEnabled(false);
						fileLineInputFromDate.setInputVerifier(new FileVerifier());
						fileSelect.add(fileLineInputFromDate, new GridConstraints(0, 1, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));

						//---- browse ----
						browse.setAction(browseAction);
						fileSelect.add(browse, new GridConstraints(0, 2, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
					}
					panel5.add(fileSelect);

					//======== startDateSelection ========
					{
						startDateSelection.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
						startDateSelection.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));

						//---- startDateLabel ----
						startDateLabel.setText("Start Month");
						startDateSelection.add(startDateLabel, new GridConstraints(0, 0, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
						startDateSelection.add(hSpacer1, new GridConstraints(0, 1, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK,
							null, null, null));
						startDateSelection.add(monthPanel1, new GridConstraints(0, 2, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
					}
					panel5.add(startDateSelection);

					//======== extractLayout ========
					{
						extractLayout.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 6, 0));

						//---- companySelect ----
						companySelect.setText("Select Company");
						extractLayout.add(companySelect, new GridConstraints(0, 0, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));

						//---- companySelector ----
						companySelector.setModel(new DefaultComboBoxModel<>(new String[] {
							"4826 Verne Road LLC",
							"Beach House Investments LLC",
							"Cajun Affiliates LLC",
							"Mahaffey Events & Tents LLC",
							"Mahaffey Industrial Contractor",
							"Mahaffey Tent & Awning Co. Inc",
							"Mahaffey USA LLC"
						}));
						companySelector.setSelectedIndex(6);
						extractLayout.add(companySelector, new GridConstraints(0, 1, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
						extractLayout.add(hSpacer2, new GridConstraints(0, 2, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK,
							null, null, null));

						//======== panel4 ========
						{
							panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 3, 0));

							//---- extract ----
							extract.setAction(extractAction);
							panel4.add(extract, new GridConstraints(0, 0, 1, 1,
								GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
								GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
								GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
								null, null, null));

							//---- cancel ----
							cancel.setAction(cancelAction);
							cancel.setDefaultCapable(false);
							panel4.add(cancel, new GridConstraints(0, 1, 1, 1,
								GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
								GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
								GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
								null, null, null));
						}
						extractLayout.add(panel4, new GridConstraints(0, 3, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
					}
					panel5.add(extractLayout);
				}
				panel1.add(panel5, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
			}
			panel2.add(panel1, new GridConstraints(0, 1, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				null, null, null));
		}
		add(panel2);

		//---- separator1 ----
		separator1.setBorder(new EtchedBorder());
		add(separator1);

		//======== panel3 ========
		{
			panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 6, 6, 6), 0, 12));

			//---- xLabel6 ----
			xLabel6.setText("Progress");
			xLabel6.setFont(new Font("Segoe UI", Font.BOLD, 20));
			xLabel6.setHorizontalAlignment(SwingConstants.CENTER);
			xLabel6.setMaxLineSpan(1);
			xLabel6.setTextAlignment(JXLabel.TextAlignment.CENTER);
			panel3.add(xLabel6, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				null, null, null));

			//======== prog1 ========
			{
				prog1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), 3, 6));

				//======== xPanel1 ========
				{
					xPanel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));

					//---- xLabel11 ----
					xLabel11.setText("Overall");
					xPanel1.add(xLabel11, new GridConstraints(0, 0, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						null, null, null));

					//---- separator11 ----
					separator11.setOrientation(SwingConstants.VERTICAL);
					xPanel1.add(separator11, new GridConstraints(0, 1, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						null, null, null));

					//---- overallProgressBar ----
					overallProgressBar.setStringPainted(true);
					xPanel1.add(overallProgressBar, new GridConstraints(0, 2, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						null, null, null));
				}
				prog1.add(xPanel1, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
				prog1.add(progress1, new GridConstraints(1, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
				prog1.add(progress2, new GridConstraints(2, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
				prog1.add(progress3, new GridConstraints(3, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
			}
			panel3.add(prog1, new GridConstraints(1, 0, 1, 1,
				GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				null, null, null));
		}
		add(panel3);

		//---- fileChooser ----
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(Paths.get("./").toFile());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JXTextField fileLineInputFromDate;
	private JXButton browse;
	private MonthPanel monthPanel1;
	private FlatComboBox<String> companySelector;
	private JXButton extract;
	private JXButton cancel;
	private FlatProgressBar overallProgressBar;
	private ProgressGL progress1;
	private ProgressGL progress2;
	private ProgressGL progress3;
	private JFileChooser fileChooser;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class Cancel extends AbstractAction {
        private Cancel() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			putValue(NAME, "Cancel");
			setEnabled(false);
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
			extractor.getTasks().values().forEach(task -> task.cancel(true));
            getBrowse().setEnabled(true);
            getFileLineInputFromDate().setEnabled(true);
            getExtract().setEnabled(true);
            getMonthPanel1().setEnabled(true);
            getCompanySelector().setEnabled(true);
            getCancel().setEnabled(false);
        }
    }

    private class Extract extends AbstractAction {
        private Extract() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			putValue(NAME, "Extract");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            getOverallProgressBar().setValue(0);
            if(getMonthPanel1().getDate()!=null) {
                if ((getFileChooser().getSelectedFile() != null && getFileChooser().getSelectedFile().exists()) ||
                    (Strings.isNotBlank(getFileLineInputFromDate().getText()) &&
                     getFileLineInputFromDate().getInputVerifier().verify(getFileLineInputFromDate())
                    )) {
                    getCancel().setEnabled(true);
                    getBrowse().setEnabled(false);
                    getFileLineInputFromDate().setEnabled(false);
                    getExtract().setEnabled(false);
                    getMonthPanel1().setEnabled(false);
                    getCompanySelector().setEnabled(false);
                    java.util.List<String> dates;
                    java.util.List<ProgressGL> progressList = new ArrayList<>() {{
                        add(getProgress1());
                        add(getProgress2());
                        add(getProgress3());
                    }};
                    if (getMonthPanel1().getDate() != null) {
                        dates = new ArrayList<>(Utils.generateMonthlyDates(getMonthPanel1().getDate()));
                    }
                    else {
                        dates = new ArrayList<>();
                        logger.fatal("Unable to generate dates");
                        System.exit(1);
                    }
                    extractor = new GLExtractFromDate(getInstance(), dates, progressList);
                    extractor.extract();
                }
                else{
                    JOptionPane.showMessageDialog(null,"Please enter a valid directory","Invalid Directory", JOptionPane.ERROR_MESSAGE);
                }
            }
            else{
                JOptionPane.showMessageDialog(null,"Please enter a valid date","Invalid Date", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class Browse extends AbstractAction {
        private Browse() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			putValue(NAME, "Browse");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            int returnVal = getFileChooser().showOpenDialog(getInstance());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                getFileLineInputFromDate().setText(getFileChooser().getSelectedFile().getAbsolutePath());
                getFileLineInputFromDate().setEnabled(true);
                SwingUtilities.getWindowAncestor(getInstance()).pack();
            }
        }
    }

    private class TextFileLine extends AbstractAction {
        private TextFileLine() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			putValue(NAME, "TextFileLine");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JXTextField) {
                JXTextField source = (JXTextField) e.getSource();
                verifyAndCreate(source);
            }
            else if (e.getSource() instanceof JTextField) {
                JTextField source = (JTextField) e.getSource();
                verifyAndCreate(source);
            }
            SwingUtilities.getWindowAncestor(getInstance()).pack();
        }

        private void verifyAndCreate(JTextField source) {
            if(source.getInputVerifier().shouldYieldFocus(source,null)) {
                File selectedFile = Paths.get(source.getText()).toFile();
                source.setText(selectedFile.getAbsolutePath());
                if(!selectedFile.exists()){
                    try{
                        Files.createDirectories(selectedFile.toPath());
                    } catch (IOException exception){
                        logger.fatal("Unable to create directory", exception);
                        System.exit(1);
                    }
                }
                getFileChooser().setSelectedFile(selectedFile);
            }
        }
    }
}
