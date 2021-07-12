package org.balance.data.mapping;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.balance.data.objects.Data;
import org.balance.data.objects.NavTables;
import org.balance.data.utils.DataUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps the NAV Trial Balance File into Trial Balance Template
 *
 * @author Nicholas Curl
 */
public abstract class Mapper<T> {

    /**
     * The instance of the logger
     */
    private static final Logger                       logger             = LogManager.getLogger(Mapper.class);
    /**
     * The map of department mappings<br>
     * Is immutable
     */
    private static final ImmutableMap<String, String> departmentMappings = new ImmutableMap.Builder<String, String>() {{
        put("0", "Balance Sheet  - 001");
        put("1", "Admin - 500 : Finance - 510");
        put("2", "Other Ops -800 : Local Ops - 820");
        put("3", "Operations - 100 : Unabsorbed Labor/Admin - 160");
        put("4", "Other Ops - 800 : Fort Polk - 810");
        put("5", "Operations Support - 200 : Equipment Admin - 240");
        put("6", "Operations Support - 200 : Fabric - 220");
        put("7", "Operations Support - 200 : Fabric - 220");
        put("8", "Sales & Training - 300 : Sales - 310");
        put("9", "Other Ops -800 : Classic Asset - 830");
        put("10", "Operations Support - 200 : Houston Depot - 250");
        put("12", "Admin - 500 : Technology - 530");
        put("22", "Owner Ops - 900 : Owners - 910");
        put("47", "Sales & Training  - 300 : Training - 320");
        put("58", "Admin - 500 : HR - 520");
        put("99", "Operations - 100 : Unabsorbed Labor/Admin - 160");
    }}.build();

    protected ImmutableMap<String, String> getDepartmentMappings() {
        return departmentMappings;
    }

    public abstract T map();

    @Deprecated
    public static List<List<Object>> map(NavTables tables)
    throws ParseException, URISyntaxException, IOException, InvalidFormatException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yy");
        Date period = inputFormat.parse(tables.getDateString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(period);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        List<List<Object>> table = new ArrayList<>();
        List<Object> header = DataUtils.getHeader(DataUtils.getResource(Mapper.class,
                                                                        "Trial Balance Template_MFG.xlsx"
        ));
        table.add(header);
        int rowNum = 0;
        for (int i = 0; i < tables.getDebitTable().size(); i++) {
            List<Object> debitRow = tables.getDebitTable().get(i);
            List<Object> creditRow = tables.getCreditTable().get(i);
            //balance:
            for (int j = 3; j < tables.getDebitTable().get(0).size(); j++) {
                String tranID = String.format("TB%02d%d_%d", month, year, rowNum);
                List<Object> debitMapRow = mapRow(tables, debitRow, j, header.size(), tranID, period);
                if (debitMapRow.size() > 0) {
                    table.add(debitMapRow);
                    rowNum++;
                }
                tranID = String.format("TB%02d%d_%d", month, year, rowNum);
                List<Object> creditMapRow = mapRow(tables, creditRow, j, header.size(), tranID, period);
                if (creditMapRow.size() > 0) {
                    table.add(creditMapRow);
                    rowNum++;
                }
            }
        }
        return table;
    }

    /**
     * Maps the downloaded trial balance from NAV
     *
     * @param file The NAV trial balance file
     *
     * @return The mapped trial balance
     *
     * @throws URISyntaxException     if the resource file is not present or a incorrect URI
     * @throws IOException            if file is not able to be read
     * @throws InvalidFormatException if the file is not an .xlsx file
     * @throws ParseException         if the string is not parsable to a date
     */
    @Deprecated
    public static List<List<Object>> map(File file)
    throws URISyntaxException, IOException, InvalidFormatException, ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yy");
        SimpleDateFormat postingPeriod = new SimpleDateFormat("MMMM yyyy");
        SimpleDateFormat transactionDate = new SimpleDateFormat("MM/dd/yyyy");
        Pattern pattern = Pattern.compile("(\\d+-\\d+-\\d+)");
        Matcher m = pattern.matcher(file.getName());
        String dateString;
        if (m.find()) {
            dateString = m.group(1);
        }
        else {
            dateString = "";
            logger.fatal("Unable to get date for file {}", file.getName());
            System.exit(1);
        }
        Date period = inputFormat.parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(period);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        String tranID = String.format("TB%02d%d", month, year);
        List<List<Object>> table = new ArrayList<>();
        List<Object> header = DataUtils.getHeader(DataUtils.getResource(Mapper.class,
                                                                        "Trial Balance Template_MFG.xlsx"
        ));
        table.add(header);
        Data data = DataUtils.readData(file);
        for (int i = 0; i < data.getData().size(); i++) {
            List<Object> row = data.getData().get(i);
            //balance:
            for (int j = 3; j < data.getHeader().size(); j++) {
                List<Object> mapRow = new ArrayList<>();
                for (int k = 0; k < header.size(); k++) {
                    switch (k) {
                        case 0: //tranid
                            mapRow.add(k, tranID);
                            break;
                        case 1: //subsidiary
                            mapRow.add(k, "Mahaffey USA");
                            break;
                        case 2: //currency
                            mapRow.add(k, "USA");
                            break;
                        case 3: //exchangeRate
                            mapRow.add(k, 1);
                            break;
                        case 4: //postingPeriod
                            mapRow.add(k, postingPeriod.format(period));
                            break;
                        case 5: //tranDate
                            mapRow.add(k, transactionDate.format(period));
                            break;
                        case 6: //account number
                            mapRow.add(k, row.get(0) + " " + row.get(1));
                            break;
                        case 7: //credit and debit amounts
                            if (row.get(j) instanceof Double) {
                                double value = (double) row.get(j);
                                if (value > 0) {
                                    mapRow.add(k, value);
                                    mapRow.add(k + 1, 0.0);
                                }
                                else if (value < 0) {
                                    mapRow.add(k, 0.0);
                                    mapRow.add(k + 1, value);
                                }
                                else {
                                    mapRow.add(k, 0.0);
                                    mapRow.add(k + 1, 0.0);
                                }
                            }
                            else {
                                mapRow.add(k, 0.0);
                                mapRow.add(k + 1, 0.0);
                            }
                            break;
                        case 8: //breaks on credit
                            break;
                        case 10: //department
                            mapRow.add(departmentMappings.get(data.getHeader().get(j).toString()));
                            break;
                        case 12: //location
                            mapRow.add(k, "Mahaffey - Delp St");
                            break;
                        default:
                            mapRow.add(k, null);
                            break;
                    }
                }
                table.add(mapRow);
            }
        }
        return table;
    }

    @Deprecated
    public static Map<String, List<List<Object>>> mapGL(List<String> header,
                                                        List<List<Object>> data,
                                                        List<String> deptCodes
    ) {
        HashMap<String, List<List<Object>>> map = new HashMap<>();
        List<Object> mapHeader = initializeHeader(header);
        for (String deptCode : deptCodes) {
            map.put(deptCode, new ArrayList<>() {{
                add(mapHeader);
            }});
        }
        map.put("", new ArrayList<>() {{
            add(mapHeader);
        }});
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyy");
        for (List<Object> datum : data) {
            List<Object> row = new ArrayList<>();
            for (int i = 0; i < mapHeader.size(); i++) {
                switch (i) {
                    case 0:
                        if (datum.get(0) instanceof Date) {
                            Date date = (Date) datum.get(0);
                            row.add(i, format.format(date));
                        }
                        else {
                            row.add(i, datum.get(0));
                        }
                        break;
                    case 1:
                        row.add(i, datum.get(1));
                        break;
                    case 2:
                        String accountName = datum.get(2) + " " + datum.get(7);
                        row.add(i, accountName);
                        break;
                    case 3:
                        row.add(i, datum.get(4));
                        break;
                    case 4:
                        row.add(i, datum.get(8));
                        break;
                    case 5:
                        row.add(i, datum.get(9));
                        break;
                    case 6:
                        row.add(i, datum.get(10));
                        break;
                    case 7:
                        row.add(i, datum.get(11));
                        break;
                    default:
                        row.add(i, "");
                        break;
                }
            }
            Pattern pattern = Pattern.compile("^[123]\\d*");
            Matcher matcher = pattern.matcher((String) datum.get(2));
            if (matcher.find()) {
                if (((String) datum.get(3)).isBlank()) {
                    map.get("0").add(row);
                }
                else {
                    map.get((String) datum.get(3)).add(row);
                }
            }
            else {
                map.get((String) datum.get(3)).add(row);
            }
        }
        return map;
    }


    @Deprecated
    private static List<Object> mapRow(NavTables tables,
                                       List<Object> row,
                                       int j,
                                       int headerSize,
                                       String tranID,
                                       Date period
    ) {
        SimpleDateFormat postingPeriod = new SimpleDateFormat("MMMM yyyy");
        SimpleDateFormat transactionDate = new SimpleDateFormat("MM/dd/yyyy");
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
                    rowMap.add(k, row.get(0) + " " + row.get(1));
                    break;
                case 7: //credit and debit amounts
                    if (row.get(j) instanceof BigDecimal) {
                        BigDecimal bigDecimal = (BigDecimal) row.get(j);
                        if (bigDecimal.signum() == 1) {
                            rowMap.add(k, bigDecimal.doubleValue());
                            rowMap.add(k + 1, 0.0);
                        }
                        else if (bigDecimal.signum() == -1) {
                            rowMap.add(k, 0.0);
                            rowMap.add(k + 1, bigDecimal.doubleValue());
                        }
                        else {
                            return new ArrayList<>();
                        }
                    }
                    else {
                        return new ArrayList<>();
                    }
                    break;
                case 8: //breaks on credit
                    break;
                case 10: //department
                    rowMap.add(departmentMappings.get(tables.getDebitTable().get(0).get(j).toString()));
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

    @Deprecated
    @NotNull
    private static List<Object> initializeHeader(List<String> header) {
        return new ArrayList<>() {{
            add(header.get(0));
            add(header.get(1));
            add("G/L Account");
            add(header.get(4));
            add(header.get(8));
            add(header.get(9));
            add(header.get(10));
            add(header.get(11));
        }};
    }
}
