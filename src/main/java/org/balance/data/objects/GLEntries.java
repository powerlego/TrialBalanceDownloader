package org.balance.data.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Nicholas Curl
 */
public class GLEntries {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(GLEntries.class);

    private final Map<String, Map<String, BigDecimal>> debit;
    private final Map<String, Map<String, BigDecimal>> credit;
    private final String       period;
    private final List<String> deptCodes;

    public GLEntries(Map<String, Map<String, BigDecimal>> debit,
                     Map<String, Map<String, BigDecimal>> credit,
                     String period,
                     List<String> deptCodes
    ) {
        this.debit = debit;
        this.credit = credit;
        this.period = period;
        this.deptCodes = deptCodes;
    }

    public Map<String, Map<String, BigDecimal>> getCredit() {
        return credit;
    }

    public Map<String, Map<String, BigDecimal>> getDebit() {
        return debit;
    }

    public String getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return "GLEntries{" +
               "period='" + period + '\'' +
               '}';
    }

    public List<String> getDeptCodes() {
        return deptCodes;
    }
}
