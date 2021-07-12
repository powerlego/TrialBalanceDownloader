package org.balance.data.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Object to hold the data that is read
 * @author Nicholas Curl
 */
public class Data {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Data.class);

    /**
     * The data of the file
     */
    private final List<List<Object>> data;
    /**
     * The header row for the data
     */
    private final List<Object>       header;

    /**
     * The constructor for the Data object
     * @param header The header row
     * @param data The rest of the data
     */
    public Data(List<Object> header, List<List<Object>> data) {
        this.header = header;
        this.data = data;
    }

    /**
     * Gets the data
     * @return The data
     */
    public List<List<Object>> getData() {
        return data;
    }

    /**
     * Gets the header
     * @return The header
     */
    public List<Object> getHeader() {
        return header;
    }
}
