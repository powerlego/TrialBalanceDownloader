package org.balance.data.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Nicholas Curl
 */
public class Balances {

    /**
     * The instance of the logger
     */
    private static final Logger                               logger = LogManager.getLogger(Balances.class);
    private final        Map<String, Map<String, BigDecimal>> debit;
    private final        List<Object>                         header;
    private final        List<String>                         accountNums;
    private final        Map<String, Map<String, BigDecimal>> credit;
    private final        String                               period;
    private final List<String> depts;

    public Balances(String period,
                    List<Object> header,
                    List<String> accountNums,
                    List<String> depts,
                    Map<String, Map<String, BigDecimal>> debit,
                    Map<String, Map<String, BigDecimal>> credit
    ) {
        this.period = period;
        this.header = header;
        this.debit = debit;
        this.credit = credit;
        this.accountNums = accountNums;
        this.depts = depts;
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

    public List<Object> getHeader() {
        return header;
    }

    public List<String> getAccountNums() {
        return accountNums;
    }

    public List<String> getDepts() {
        return depts;
    }
}
