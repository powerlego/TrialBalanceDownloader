package org.balance.data.mapping.tb;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.data.mapping.Mapper;
import org.balance.data.objects.Balances;
import org.balance.extractor.processes.Extractor.Task;
import org.jdesktop.swingx.JXLabel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicholas Curl
 */
@Deprecated
public class TBInitialMapper extends Mapper<Balances> {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(TBInitialMapper.class);

    private final Task<?>            task;
    private final String period;
    private final List<String>       depts;
    private final List<String>       accountNums;
    private final List<List<Object>> debitTable;
    private final List<List<Object>> creditTable;

    @Deprecated
    public TBInitialMapper(Task<?> task,
                           String period,
                           List<String> depts,
                           List<String> accountNums,
                           List<List<Object>> debitTable,
                           List<List<Object>> creditTable
    ) {
        this.task = task;
        this.period = period;
        this.depts = depts;
        this.accountNums = accountNums;
        this.debitTable = debitTable;
        this.creditTable = creditTable;
    }

    @Deprecated
    @Override
    public Balances map() {
        Map<String, Map<String, BigDecimal>> debit = new HashMap<>();
        Map<String, Map<String, BigDecimal>> credit = new HashMap<>();
        FlatProgressBar progressBar = task.getProgressContainer().getProgressBar();
        JXLabel status = task.getProgressContainer().getStatus();
        int progress = 0;
        progressBar.setMaximum(depts.size()*accountNums.size());
        progressBar.setValue(0);
        status.setText("Mapping");
        for (int i = 1; i < accountNums.size()+1; i++) {
            if(task.isCancelled()){
                return null;
            }
            String accountNum = accountNums.get(i-1);
            debit.put(accountNum, new HashMap<>());
            credit.put(accountNum, new HashMap<>());
            List<Object> debitRow = debitTable.get(i);
            List<Object> creditRow = creditTable.get(i);
            debit.get(accountNum).put("Total", (BigDecimal) debitRow.get(2));
            credit.get(accountNum).put("Total", (BigDecimal) creditRow.get(2));
            for (int j = 0; j < depts.size(); j++) {
                if(task.isCancelled()){
                    return null;
                }
                String dept = depts.get(j);
                debit.get(accountNum).put(dept,(BigDecimal) debitRow.get(j+3));
                credit.get(accountNum).put(dept,(BigDecimal) creditRow.get(j+3));
                progressBar.setValue(progress++);
            }
        }
        List<Object> header = new ArrayList<>(debitTable.get(0));
        depts.add(0,"Total");
        header.remove(0);
        header.set(0, "Account");
        return new Balances(period,header,accountNums,depts,debit,credit);
    }
}
