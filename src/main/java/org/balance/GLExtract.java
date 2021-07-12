package org.balance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.extractor.utils.ExtractorUtils;

import java.nio.file.Path;

/**
 * @author Nicholas Curl
 */
public class GLExtract {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(GLExtract.class);

    public static void main(String[] args) {
        Path downloadDir = ExtractorUtils.initialize();
        //GLExtractor.threadedExtractGLEntries(downloadDir, "Mahaffey USA LLC");
    }
}
