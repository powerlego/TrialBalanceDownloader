package org.balance.data.writing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Nicholas Curl
 */
public abstract class Writer {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Writer.class);
    private final FileOutputStream fileOutputStream;
    private final File workbookFile;


    public Writer(File workbookFile){
        FileOutputStream fileOutputStream1;
        try {
            fileOutputStream1 = new FileOutputStream(workbookFile);

        }
        catch (FileNotFoundException e) {
            fileOutputStream1 = null;
            logger.fatal(e.getMessage(), e);
            System.exit(1);
        }
        this.workbookFile =workbookFile;
        this.fileOutputStream = fileOutputStream1;
    }

    protected File getWorkbookFile() {
        return workbookFile;
    }

    protected FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public abstract void makeWorkbook();
}
