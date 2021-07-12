package org.balance.extractor.gui.verification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * @author Nicholas Curl
 */
public class FileVerifier extends InputVerifier {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(FileVerifier.class);

    /**
     * Checks whether the JComponent's input is valid. This method should
     * have no side effects. It returns a boolean indicating the status
     * of the argument's input.
     *
     * @param input the JComponent to verify
     *
     * @return {@code true} when valid, {@code false} when invalid
     *
     * @see JComponent#setInputVerifier
     * @see JComponent#getInputVerifier
     */
    @Override
    public boolean verify(JComponent input) {
        if (input instanceof JTextField) {
            JTextField jxTextField = (JTextField) input;
            try {
                if(jxTextField.getText().isBlank()){
                    return false;
                }
                Paths.get(jxTextField.getText());
                return true;
            }
            catch (InvalidPathException e) {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public boolean shouldYieldFocus(JComponent source, JComponent target) {
        boolean ok = this.verify(source);
        if(ok){
            return true;
        }
        JOptionPane.showMessageDialog(null,"Please enter a valid directory","Invalid Directory", JOptionPane.ERROR_MESSAGE);
        source.setInputVerifier(this);
        return false;
    }
}
