/*
 * Created by JFormDesigner on Wed Jun 30 15:21:02 CDT 2021
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
import org.balance.extractor.processes.gl.GLExtractAtDate;
import org.balance.extractor.utils.ExtractorUtils;
import org.jdesktop.swingx.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Nicholas Curl
 */
public class GLAtDate extends JPanel {
	private static final Logger logger = LogManager.getLogger(GLAtDate.class);
	private GLExtractAtDate extractor;
	public GLAtDate() {
		initComponents();
	}

	public JXTextField getFileLineInput() {
		return this.fileLineInput;
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
        return this.companySelector;
    }

    public JFileChooser getFileChooser() {
        return this.fileChooser;
    }

	public MonthPanel getMonthPanel1() {
		return this.monthPanel1;
	}
	public GLAtDate getInstance(){
		return this;
	}

	public ProgressGL getProgress1() {
		return this.progress1;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		var panel1 = new JXPanel();
		var label1 = new JXLabel();
		var panel2 = new JXPanel();
		var panel3 = new JXPanel();
		var fileSelect = new JXPanel();
		var outputDirectoryLabel = new JXLabel();
		this.fileLineInput = new JXTextField();
		this.browse = new JXButton();
		var startDateSelection = new JXPanel();
		var startDateLabel = new JLabel();
		var hSpacer1 = new Spacer();
		this.monthPanel1 = new MonthPanel();
		var extractLayout = new JXPanel();
		var companySelect = new JXLabel();
		this.companySelector = new FlatComboBox<>();
		var hSpacer3 = new Spacer();
		var panel4 = new JPanel();
		this.extract = new JXButton();
		this.cancel = new JXButton();
		var separator1 = new JSeparator();
		var panel5 = new JXPanel();
		var label2 = new JXLabel();
		var prog = new JXPanel();
		var overallContainer = new JXPanel();
		var label3 = new JXLabel();
		var separator2 = new JSeparator();
		this.overallProgressBar = new FlatProgressBar();
		this.progress1 = new ProgressGL();
		this.fileChooser = new JFileChooser();
		var browseAction = new Browse();
		var textFileLineAction = new TextFileLine();
		var extractAction = new Extract();
		var cancelAction = new Cancel();

		//======== this ========
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//======== panel1 ========
		{
			panel1.setLayout(new GridLayoutManager(1, 2, new Insets(6, 6, 6, 6), 12, 0));

			//---- label1 ----
			label1.setText("<html><p>G/L Entry Extractor<br>At Month</p></html>");
			label1.setFont(new Font("Segoe UI", Font.BOLD, 26));
			label1.setMaxLineSpan(1);
			label1.setPreferredSize(new Dimension(86, 81));
			label1.setMinimumSize(new Dimension(250, 81));
			label1.setHorizontalAlignment(SwingConstants.LEFT);
			label1.setMaximumSize(new Dimension(2147483647, 27));
			label1.setLineWrap(true);
			panel1.add(label1, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				null, null, null));

			//======== panel2 ========
			{
				panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 6, 0));

				//======== panel3 ========
				{
					panel3.setLayout(new VerticalLayout(6));

					//======== fileSelect ========
					{
						fileSelect.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));

						//---- outputDirectoryLabel ----
						outputDirectoryLabel.setText("Save Location");
						outputDirectoryLabel.setLabelFor(this.fileLineInput);
						fileSelect.add(outputDirectoryLabel, new GridConstraints(0, 0, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));

						//---- fileLineInput ----
						this.fileLineInput.setAction(textFileLineAction);
						this.fileLineInput.setEnabled(false);
						this.fileLineInput.setInputVerifier(new FileVerifier());
						fileSelect.add(this.fileLineInput, new GridConstraints(0, 1, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));

						//---- browse ----
						this.browse.setAction(browseAction);
						fileSelect.add(this.browse, new GridConstraints(0, 2, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
					}
					panel3.add(fileSelect);

					//======== startDateSelection ========
					{
						startDateSelection.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
						startDateSelection.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));

						//---- startDateLabel ----
						startDateLabel.setText("Select Month");
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
						startDateSelection.add(this.monthPanel1, new GridConstraints(0, 2, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
					}
					panel3.add(startDateSelection);

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
						this.companySelector.setModel(new DefaultComboBoxModel<>(new String[] {
							"4826 Verne Road LLC",
							"Beach House Investments LLC",
							"Cajun Affiliates LLC",
							"Mahaffey Events & Tents LLC",
							"Mahaffey Industrial Contractor",
							"Mahaffey Tent & Awning Co. Inc",
							"Mahaffey USA LLC"
						}));
						this.companySelector.setSelectedIndex(6);
						extractLayout.add(this.companySelector, new GridConstraints(0, 1, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
							null, null, null));
						extractLayout.add(hSpacer3, new GridConstraints(0, 2, 1, 1,
							GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
							GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
							GridConstraints.SIZEPOLICY_CAN_SHRINK,
							null, null, null));

						//======== panel4 ========
						{
							panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 3, 0));

							//---- extract ----
							this.extract.setAction(extractAction);
							panel4.add(this.extract, new GridConstraints(0, 0, 1, 1,
								GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
								GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
								GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
								null, null, null));

							//---- cancel ----
							this.cancel.setAction(cancelAction);
							this.cancel.setDefaultCapable(false);
							panel4.add(this.cancel, new GridConstraints(0, 1, 1, 1,
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
					panel3.add(extractLayout);
				}
				panel2.add(panel3, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
			}
			panel1.add(panel2, new GridConstraints(0, 1, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				null, null, null));
		}
		add(panel1);

		//---- separator1 ----
		separator1.setBorder(new EtchedBorder());
		add(separator1);

		//======== panel5 ========
		{
			panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 6, 6, 6), 0, 12));

			//---- label2 ----
			label2.setText("Progress");
			label2.setFont(new Font("Segoe UI", Font.BOLD, 20));
			label2.setHorizontalAlignment(SwingConstants.CENTER);
			label2.setMaxLineSpan(1);
			label2.setHorizontalTextPosition(SwingConstants.CENTER);
			label2.setTextAlignment(JXLabel.TextAlignment.CENTER);
			panel5.add(label2, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				null, null, null));

			//======== prog ========
			{
				prog.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 3, 6));

				//======== overallContainer ========
				{
					overallContainer.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 6, 0));

					//---- label3 ----
					label3.setText("Overall");
					overallContainer.add(label3, new GridConstraints(0, 0, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						null, null, null));

					//---- separator2 ----
					separator2.setOrientation(SwingConstants.VERTICAL);
					overallContainer.add(separator2, new GridConstraints(0, 1, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						null, null, null));

					//---- overallProgressBar ----
					this.overallProgressBar.setStringPainted(true);
					overallContainer.add(this.overallProgressBar, new GridConstraints(0, 2, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						null, null, null));
				}
				prog.add(overallContainer, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
				prog.add(this.progress1, new GridConstraints(1, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
			}
			panel5.add(prog, new GridConstraints(1, 0, 1, 1,
				GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				null, null, null));
		}
		add(panel5);

		//---- fileChooser ----
		this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.fileChooser.setCurrentDirectory(Paths.get("./").toFile());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JXTextField fileLineInput;
	private JXButton browse;
	private MonthPanel monthPanel1;
	private FlatComboBox<String> companySelector;
	private JXButton extract;
	private JXButton cancel;
	private FlatProgressBar overallProgressBar;
	private ProgressGL progress1;
	private JFileChooser fileChooser;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

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
				getFileLineInput().setText(getFileChooser().getSelectedFile().getAbsolutePath());
				getFileLineInput().setEnabled(true);
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

	public class Cancel extends AbstractAction {
		public Cancel() {
			// JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			putValue(NAME, "Cancel");
			setEnabled(false);
			// JFormDesigner - End of action initialization  //GEN-END:initComponents
		}

		public void actionPerformed(ActionEvent e) {
			extractor.getTasks().values().forEach(task -> task.cancel(true));
			getBrowse().setEnabled(true);
			getFileLineInput().setEnabled(true);
			getExtract().setEnabled(true);
			getMonthPanel1().setEnabled(true);
			getCompanySelector().setEnabled(true);
			getCancel().setEnabled(false);
		}
	}

	public class Extract extends AbstractAction {
		public Extract() {
			// JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner non-commercial license
			putValue(NAME, "Extract");
			// JFormDesigner - End of action initialization  //GEN-END:initComponents
		}

		public void actionPerformed(ActionEvent e) {
			getOverallProgressBar().setValue(0);
			if(getMonthPanel1().getDate()!=null) {
				if ((getFileChooser().getSelectedFile() != null && getFileChooser().getSelectedFile().exists()) ||
					(Strings.isNotBlank(getFileLineInput().getText()) &&
					 getFileLineInput().getInputVerifier().verify(getFileLineInput())
					)) {
					String dateString = ExtractorUtils.getMonthlyPeriod(getMonthPanel1().getDate());
					if(!dateString.isBlank()){
						getCancel().setEnabled(true);
						getBrowse().setEnabled(false);
						getFileLineInput().setEnabled(false);
						getExtract().setEnabled(false);
						getMonthPanel1().setEnabled(false);
						getCompanySelector().setEnabled(false);
						extractor = new GLExtractAtDate(getInstance(), dateString, getProgress1());
						extractor.extract();
					}
					else{
						JOptionPane.showMessageDialog(null,"Please select a month prior to " + getMonthPanel1().getMonth().getSelectedItem(),"Invalid Date", JOptionPane.ERROR_MESSAGE);
					}
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
}
