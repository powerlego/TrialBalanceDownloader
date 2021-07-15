package org.balance.data.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicholas Curl
 */
@Deprecated
public class NavTables {

    /**
     * The instance of the logger
     */
    private static final Logger             logger = LogManager.getLogger(NavTables.class);
    private final        List<List<Object>> debitTable;
    private final        List<List<Object>> creditTable;
    private final String dateString;
    public static NavTables EMPTY = new NavTables();
    @Deprecated
    public NavTables(List<List<Object>> debitTable, List<List<Object>> creditTable, String dateString) {
        this.debitTable = debitTable;
        this.creditTable = creditTable;
        this.dateString = dateString;
    }
    @Deprecated
    public NavTables(){
        this.debitTable = new ArrayList<>();
        this.creditTable = new ArrayList<>();
        this.dateString = "";
    }

    public List<List<Object>> getCreditTable() {
        return creditTable;
    }

    public String getDateString() {
        return dateString;
    }

    public List<List<Object>> getDebitTable() {
        return debitTable;
    }
}
