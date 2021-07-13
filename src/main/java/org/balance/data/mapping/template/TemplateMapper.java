package org.balance.data.mapping.template;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.balance.data.mapping.Mapper;
import org.balance.data.objects.Balances;
import org.balance.data.utils.DataUtils;
import org.balance.extractor.processes.Extractor.Task;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nicholas Curl
 */
public class TemplateMapper extends Mapper<List<List<Object>>> {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(TemplateMapper.class);

    private final Balances          balances;
    private final LocalDate         period;
    private final Task<?>           task;
    private final DateTimeFormatter postingPeriod;
    private final DateTimeFormatter transactionDate;


    public TemplateMapper(Task<?> task, Balances balances) {
        this.task = task;
        this.balances = balances;
        this.period = LocalDate.from(DateTimeFormatter.ofPattern("MM/dd/yy").parse(balances.getPeriod()));
        this.postingPeriod = DateTimeFormatter.ofPattern("MMMM yyyy");
        this.transactionDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    @Override
    public List<List<Object>> map() {
        List<List<Object>> table = new ArrayList<>();
        List<Object> header;
        JProgressBar progressBar = task.getProgressContainer().getProgressBar();
        JXLabel status = task.getProgressContainer().getStatus();
        try {
            header = DataUtils.getHeader(DataUtils.getResource(this.getClass(),
                                                               "Trial Balance Template_MFG.xlsx"
            ));
        }
        catch (URISyntaxException | IOException | InvalidFormatException e) {
            header = new ArrayList<>();
            logger.fatal("Unable to get header", e);
            System.exit(1);
        }
        table.add(header);
        Map<String, Map<String, BigDecimal>> debit = balances.getDebit();
        Map<String, Map<String, BigDecimal>> credit = balances.getCredit();
        List<String> depts = balances.getDepts();
        List<String> accountNums = balances.getAccountNums();
        int rowNum = 0;
        status.setText("Mapping");
        progressBar.setMaximum(2*(accountNums.size()*depts.size()));
        for (String accountNum : accountNums) {
            for (String dept : depts) {
                String tranID = String.format("TB%02d%d_%d", period.getMonthValue(), period.getYear(), rowNum);
                List<Object> debitRow = mapRow(header.size(),tranID,debit.get(accountNum).get(dept),dept,accountNum);
                progressBar.setValue(rowNum++);
                tranID = String.format("TB%02d%d_%d", period.getMonthValue(), period.getYear(), rowNum);
                List<Object> creditRow = mapRow(header.size(),tranID,credit.get(accountNum).get(dept),dept,accountNum);
                progressBar.setValue(rowNum++);
                if(!debitRow.isEmpty()) {
                    table.add(debitRow);
                }
                else{
                    rowNum--;
                }
                if(!creditRow.isEmpty()) {
                    table.add(creditRow);
                }
                else{
                    rowNum--;
                }
            }
        }
        return table;
    }


    private List<Object> mapRow(int headerSize, String tranID, BigDecimal value, String dept, String accountNum) {
        List<Object> rowMap = new ArrayList<>();
        for (int k = 0; k < headerSize; k++) {
            switch (k) {
                case 0: //tranid
                    rowMap.add(k, tranID);
                    break;
                case 1: //subsidiary
                    rowMap.add(k, "Mahaffey USA");
                    break;
                case 2: //currency
                    rowMap.add(k, "USA");
                    break;
                case 3: //exchangeRate
                    rowMap.add(k, 1);
                    break;
                case 4: //postingPeriod
                    rowMap.add(k, postingPeriod.format(period));
                    break;
                case 5: //tranDate
                    rowMap.add(k, transactionDate.format(period));
                    break;
                case 6: //account number
                    rowMap.add(k, accountNum);
                    break;
                case 7: //credit and debit amounts
                    if (value.signum() == 1) {
                        rowMap.add(k, value.doubleValue());
                        rowMap.add(k + 1, 0.0);
                    }
                    else if (value.signum() == -1) {
                        rowMap.add(k, 0.0);
                        rowMap.add(k + 1, value.doubleValue());
                    }
                    else {
                        return new ArrayList<>();
                    }
                    break;
                case 8: //breaks on credit
                    break;
                case 10: //department
                    rowMap.add(getDepartmentMappings().get(dept));
                    break;
                case 12: //location
                    rowMap.add(k, "Mahaffey - Delp St");
                    break;
                default:
                    rowMap.add(k, null);
                    break;
            }
        }
        return rowMap;
    }

}
