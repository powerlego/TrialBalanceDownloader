package org.balance.data.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Nicholas Curl
 */
@Deprecated
public class NavData {

    /**
     * The instance of the logger
     */
    private static final Logger          logger = LogManager.getLogger(NavData.class);
    private final        Path            companyPath;
    private final        List<NavTables> data;

    @Deprecated
    public NavData(Path companyPath, List<NavTables> data) {
        this.companyPath = companyPath;
        this.data = data;
    }

    public Path getCompanyPath() {
        return companyPath;
    }

    public List<NavTables> getData() {
        return data;
    }
}
