package org.balance.data.mapping.tb;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.data.mapping.Mapper;
import org.balance.data.objects.GLEntries;
import org.balance.extractor.processes.Extractor.Task;
import org.jdesktop.swingx.JXLabel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nicholas Curl
 */
public class TrialBalanceGLMapper extends Mapper<GLEntries> {

    /**
     * The instance of the logger
     */
    private static final Logger             logger = LogManager.getLogger(TrialBalanceGLMapper.class);
    private final        String             period;
    private final        List<List<Object>> data;
    private final        List<String>       deptCodes;
    private final        Task<?>            task;
    private final List<String> accountNums;


    public TrialBalanceGLMapper(String period,
                                List<List<Object>> data,
                                List<String> deptCodes,
                                List<String> accountNums,
                                Task<?> task
    ) {
        this.period = period;
        this.data = data;
        this.deptCodes = deptCodes;
        this.task = task;
        this.accountNums = accountNums;
    }

    @Override
    public GLEntries map() {
        int progress = 0;
        Map<String, Map<String, BigDecimal>> debit = new HashMap<>();
        Map<String, Map<String, BigDecimal>> credit = new HashMap<>();
        JXLabel status = task.getProgressContainer().getStatus();
        FlatProgressBar progressBar = task.getProgressContainer().getProgressBar();
        status.setText("Mapping");
        progressBar.setMaximum(data.size() * accountNums.size());
        for (String accountNum : accountNums) {
            debit.put(accountNum, new HashMap<>());
            credit.put(accountNum, new HashMap<>());
            for (String deptCode : deptCodes) {
                debit.get(accountNum).put(deptCode, new BigDecimal("0.00"));
                credit.get(accountNum).put(deptCode, new BigDecimal("0.00"));
            }
        }
        for (List<Object> row : data) {
            String accountNum = row.get(2) + " " + row.get(7);
            Pattern pattern = Pattern.compile("^[123]\\d*");
            Matcher matcher = pattern.matcher((String) row.get(2));
            String dept;
            if (matcher.find()) {
                if (((String) row.get(3)).isBlank()) {
                    dept = "0";
                }
                else {
                    dept = (String) row.get(3);
                }
            }
            else {
                if(((String) row.get(3)).isBlank()){
                    dept = "3";
                }
                else{
                    dept = (String) row.get(3);
                }
            }
            BigDecimal amount = (BigDecimal) row.get(4);
            if (amount.signum() < 0) {
                Map<String, BigDecimal> deptValue = credit.get(accountNum);
                BigDecimal prevAmount = deptValue.getOrDefault(dept, new BigDecimal("0.00"));
                deptValue.put(dept, prevAmount.add(amount));
            }
            else if (amount.signum() > 0) {
                Map<String, BigDecimal> deptValue = credit.get(accountNum);
                BigDecimal prevAmount = deptValue.getOrDefault(dept, new BigDecimal("0.00"));
                deptValue.put(dept, prevAmount.add(amount));
            }
            else {
                Map<String, BigDecimal> deptValueDebit = debit.get(accountNum);
                Map<String, BigDecimal> deptValueCredit = credit.get(accountNum);
                BigDecimal prevAmountDebit = deptValueDebit.getOrDefault(dept, new BigDecimal("0.00"));
                BigDecimal prevAmountCredit = deptValueCredit.getOrDefault(dept, new BigDecimal("0.00"));
                credit.get(accountNum).put(dept, prevAmountCredit.add(new BigDecimal("0.00")));
                debit.get(accountNum).put(dept, prevAmountDebit.add(new BigDecimal("0.00")));
            }
            progressBar.setValue(progress++);
        }
        progressBar.setValue(0);
        return new GLEntries(debit, credit, period, deptCodes);
    }
}
