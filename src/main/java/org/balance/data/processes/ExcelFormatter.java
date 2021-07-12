package org.balance.data.processes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.apache.xmlbeans.XmlBoolean;
import org.balance.data.utils.DataUtils;
import org.balance.extractor.processes.Extractor.Task;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nicholas Curl
 */
@Deprecated
public class ExcelFormatter {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(ExcelFormatter.class);

    @Deprecated
    public static void formatWorkbook(Path workbookFile) {
        formatWorkbook(workbookFile.toFile());
    }

    @Deprecated
    public static void formatWorkbook(File workbookFile) {
        try {
            OPCPackage pkg = OPCPackage.open(workbookFile);
            XSSFWorkbook workbook = new XSSFWorkbook(pkg);
            XSSFSheet sheet = workbook.getSheetAt(0);
            CellReference startingReference = new CellReference(sheet.getRow(0)
                                                                     .getCell(sheet.getRow(0).getFirstCellNum()));
            CellReference endReference = new CellReference(sheet.getRow(sheet.getLastRowNum())
                                                                .getCell(sheet.getRow(sheet.getLastRowNum())
                                                                              .getLastCellNum() - 1));
            AreaReference reference = new AreaReference(startingReference, endReference, SpreadsheetVersion.EXCEL2007);
            XSSFTable table = sheet.createTable(reference);
            table.getCTTable().addNewTableStyleInfo();
            XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
            for (int i = 0; i < table.getColumns().size(); i++) {
                XSSFTableColumn column = table.getColumns().get(i);
                column.setName(sheet.getRow(0).getCell(i).getStringCellValue());
            }
            table.updateHeaders();
            style.setName("TableStyleMedium2");
            style.setShowColumnStripes(false);
            style.setShowRowStripes(true);
            style.setFirstColumn(false);
            style.setLastColumn(false);
            table.getCTTable().setTotalsRowCount(1);
            table.getCTTable()
                 .getTableColumns()
                 .getTableColumnList()
                 .get(0)
                 .setTotalsRowLabel("Total");
            table.getCTTable()
                 .getTableColumns()
                 .getTableColumnList()
                 .get(2)
                 .setTotalsRowFunction(STTotalsRowFunction.SUM);
            CTAutoFilter autoFilter = table.getCTTable().addNewAutoFilter();
            autoFilter.setRef(reference.formatAsString());
            String formula = "SUBTOTAL(109," + table.getName() + "[" + table.getColumns().get(2).getName() + "])";
            XSSFRow xssfRow = sheet.createRow(table.getEndRowIndex() + 1);
            for (int i = 0; i < DataUtils.getLastColumn(sheet); i++) {
                xssfRow.createCell(i);
            }
            endReference = new CellReference(sheet.getRow(sheet.getLastRowNum())
                                                  .getCell(sheet.getRow(sheet.getLastRowNum())
                                                                .getLastCellNum() - 1));
            reference = new AreaReference(startingReference, endReference, SpreadsheetVersion.EXCEL2007);
            table.setCellReferences(reference);
            sheet.getRow(sheet.getLastRowNum()).getCell(0).setCellValue("Total");
            XSSFCell xssfCell = sheet.getRow(sheet.getLastRowNum()).getCell(2);
            xssfCell.setCellFormula(formula);
            workbook.getCreationHelper().createFormulaEvaluator().evaluate(xssfCell);
            if (xssfCell.getNumericCellValue() != 0) {
                logger.warn("Total does not balance to 0 for workbook {}", workbookFile.toString());
            }
            XSSFCellStyle cellStyle = sheet.getRow(table.getStartRowIndex() + 1)
                                           .getCell(table.getEndColIndex())
                                           .getCellStyle()
                                           .copy();
            XSSFDataFormat format = workbook.getCreationHelper().createDataFormat();
            short formatIndex = format.getFormat("\"$\"#,##0.00");
            cellStyle.setDataFormat(formatIndex);
            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                for (int j = 2; j < DataUtils.getLastColumn(sheet); j++) {
                    XSSFCell cell = sheet.getRow(i).getCell(j);
                    if (cell != null) {
                        cell.setCellStyle(cellStyle);
                    }
                }
            }
            for (int i = 0; i < DataUtils.getLastColumn(sheet); i++) {
                sheet.autoSizeColumn(i);
            }
            if (workbook.getNumberOfSheets() > 1) {
                workbook.removeSheetAt(1);
            }
            DataUtils.saveChanges(pkg, workbook);
        }
        catch (IOException | InvalidFormatException e) {
            logger.fatal("Unable to format workbook", e);
            System.exit(1);
        }
    }

    @Deprecated
    public static void makeGLWorkbook(Path workbookFile, Map<String, List<List<Object>>> map, Task<?> task) {
        makeGLWorkbook(workbookFile.toFile(), map, task);
    }

    @Deprecated
    public static void makeGLWorkbook(File workbookFile, Map<String, List<List<Object>>> map, Task<?> task) {
        int progress = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCellStyle bold = workbook.createCellStyle();
        ConcurrentHashMap<String, List<List<Object>>> concurrentHashMap = new ConcurrentHashMap<>(map);
        XSSFFont font = workbook.findFont(true,
                                          IndexedColors.AUTOMATIC.getIndex(),
                                          XSSFFont.DEFAULT_FONT_SIZE,
                                          XSSFFont.DEFAULT_FONT_NAME,
                                          false,
                                          false,
                                          XSSFFont.SS_NONE,
                                          XSSFFont.U_NONE
        );
        bold.setFont(font);
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFDataFormat format = workbook.getCreationHelper().createDataFormat();
        short formatIndex = format.getFormat("\"$\"#,##0.00");
        style.setDataFormat(formatIndex);
        task.getProgressContainer().getProgressBar().setMaximum(concurrentHashMap.keySet().size());
        task.getProgressContainer().getProgressBar().setValue(0);
        task.getProgressContainer().getStatus().setText("Writing");
        for (String dept : concurrentHashMap.keySet()) {
            if (task.isCancelled()) {
                try {
                    workbook.close();
                }
                catch (IOException e) {
                    logger.fatal("Unable to close workbook", e);
                    System.exit(1);
                }
                task.getProgressContainer().getStatus().setText("Cancelled");
                return;
            }
            List<List<Object>> dataTable = concurrentHashMap.get(dept);
            String sheetName;
            if (dept.isBlank()) {
                sheetName = "Unknown Department";
            }
            else {
                sheetName = "Dept " + dept;
            }
            List<Object> header = dataTable.get(0);
            XSSFSheet sheet = workbook.createSheet(sheetName);
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < header.size(); i++) {
                if (task.isCancelled()) {
                    try {
                        workbook.close();
                    }
                    catch (IOException e) {
                        logger.fatal("Unable to close workbook", e);
                        System.exit(1);
                    }
                    task.getProgressContainer().getStatus().setText("Cancelled");
                    return;
                }
                Object object = header.get(i);
                XSSFCell cell = headerRow.createCell(i);
                DataUtils.setCellValue(cell, object);
                cell.setCellStyle(bold);
            }
            for (int i = 1; i < dataTable.size(); i++) {
                if (task.isCancelled()) {
                    try {
                        workbook.close();
                    }
                    catch (IOException e) {
                        logger.fatal("Unable to close workbook", e);
                        System.exit(1);
                    }
                    task.getProgressContainer().getStatus().setText("Cancelled");
                    return;
                }
                XSSFRow row = sheet.createRow(i);
                List<Object> dataRow = dataTable.get(i);
                for (int j = 0; j < dataRow.size(); j++) {
                    if (task.isCancelled()) {
                        try {
                            workbook.close();
                        }
                        catch (IOException e) {
                            logger.fatal("Unable to close workbook", e);
                            System.exit(1);
                        }
                        task.getProgressContainer().getStatus().setText("Cancelled");
                        return;
                    }
                    XSSFCell cell = row.createCell(j);
                    DataUtils.setCellValue(cell, dataRow.get(j));
                    if (dataRow.get(j) instanceof Double || dataRow.get(j) instanceof BigDecimal) {
                        cell.setCellStyle(style);
                    }
                }
            }
            if (sheet.getLastRowNum() == sheet.getFirstRowNum()) {
                XSSFRow row = sheet.createRow(1);
                for (int i = 0; i < header.size(); i++) {
                    if (task.isCancelled()) {
                        try {
                            workbook.close();
                        }
                        catch (IOException e) {
                            logger.fatal("Unable to close workbook", e);
                            System.exit(1);
                        }
                        task.getProgressContainer().getStatus().setText("Cancelled");
                        return;
                    }
                    XSSFCell cell = row.createCell(i);
                    cell.setBlank();
                }
            }
            CellReference startReference = new CellReference(0, 0);
            CellReference endReference = new CellReference(sheet.getLastRowNum(),
                                                           sheet.getRow(sheet.getLastRowNum()).getLastCellNum() - 1
            );
            AreaReference reference = new AreaReference(startReference, endReference, SpreadsheetVersion.EXCEL2007);
            XSSFTable xssfTable = sheet.createTable(reference);
            xssfTable.getCTTable().addNewTableStyleInfo();
            XSSFTableStyleInfo tableStyle = (XSSFTableStyleInfo) xssfTable.getStyle();
            tableStyle.setName("TableStyleMedium2");
            tableStyle.setShowColumnStripes(false);
            tableStyle.setShowRowStripes(true);
            tableStyle.setFirstColumn(false);
            tableStyle.setLastColumn(false);
            CTAutoFilter autoFilter = xssfTable.getCTTable().addNewAutoFilter();
            autoFilter.setRef(reference.formatAsString());
            for (int i = 0; i < xssfTable.getEndColIndex() + 1; i++) {
                if (task.isCancelled()) {
                    try {
                        workbook.close();
                    }
                    catch (IOException e) {
                        logger.fatal("Unable to close workbook", e);
                        System.exit(1);
                    }
                    task.getProgressContainer().getStatus().setText("Cancelled");
                    return;
                }
                sheet.autoSizeColumn(i);
            }
            task.getProgressContainer().getProgressBar().setValue(progress++);
        }
        if (!task.isCancelled()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(workbookFile);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();
            }
            catch (IOException e) {
                logger.fatal("Unable to write file", e);
                System.exit(1);
            }
        }
        else {
            try {
                workbook.close();
            }
            catch (IOException e) {
                logger.fatal("Unable to close workbook", e);
                System.exit(1);
            }
            task.getProgressContainer().getStatus().setText("Cancelled");
        }
    }

    @Deprecated
    public static void makeGLWorkbook(Path workbookFile, Map<String, List<List<Object>>> map) {
        makeGLWorkbook(workbookFile.toFile(), map);
    }

    @Deprecated
    public static void makeGLWorkbook(File workbookFile, Map<String, List<List<Object>>> map) {
        ConcurrentHashMap<String, List<List<Object>>> concurrentHashMap = new ConcurrentHashMap<>(map);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCellStyle bold = workbook.createCellStyle();
        XSSFFont font = workbook.findFont(true,
                                          IndexedColors.AUTOMATIC.getIndex(),
                                          XSSFFont.DEFAULT_FONT_SIZE,
                                          XSSFFont.DEFAULT_FONT_NAME,
                                          false,
                                          false,
                                          XSSFFont.SS_NONE,
                                          XSSFFont.U_NONE
        );
        bold.setFont(font);
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFDataFormat format = workbook.getCreationHelper().createDataFormat();
        short formatIndex = format.getFormat("\"$\"#,##0.00");
        style.setDataFormat(formatIndex);
        for (String dept : concurrentHashMap.keySet()) {
            List<List<Object>> dataTable = concurrentHashMap.get(dept);
            String sheetName;
            if (dept.isBlank()) {
                sheetName = "Unknown Department";
            }
            else {
                sheetName = "Dept " + dept;
            }
            List<Object> header = dataTable.get(0);
            XSSFSheet sheet = workbook.createSheet(sheetName);
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < header.size(); i++) {
                Object object = header.get(i);
                XSSFCell cell = headerRow.createCell(i);
                DataUtils.setCellValue(cell, object);
                cell.setCellStyle(bold);
            }
            for (int i = 1; i < dataTable.size(); i++) {
                XSSFRow row = sheet.createRow(i);
                List<Object> dataRow = dataTable.get(i);
                for (int j = 0; j < dataRow.size(); j++) {
                    XSSFCell cell = row.createCell(j);
                    DataUtils.setCellValue(cell, dataRow.get(j));
                    if (dataRow.get(j) instanceof Double || dataRow.get(j) instanceof BigDecimal) {
                        cell.setCellStyle(style);
                    }
                }
            }
            if (sheet.getLastRowNum() == sheet.getFirstRowNum()) {
                XSSFRow row = sheet.createRow(1);
                for (int i = 0; i < header.size(); i++) {
                    XSSFCell cell = row.createCell(i);
                    cell.setBlank();
                }
            }
            CellReference startReference = new CellReference(0, 0);
            CellReference endReference = new CellReference(sheet.getLastRowNum(),
                                                           sheet.getRow(sheet.getLastRowNum()).getLastCellNum() - 1
            );
            AreaReference reference = new AreaReference(startReference, endReference, SpreadsheetVersion.EXCEL2007);
            XSSFTable xssfTable = sheet.createTable(reference);
            xssfTable.getCTTable().addNewTableStyleInfo();
            XSSFTableStyleInfo tableStyle = (XSSFTableStyleInfo) xssfTable.getStyle();
            tableStyle.setName("TableStyleMedium2");
            tableStyle.setShowColumnStripes(false);
            tableStyle.setShowRowStripes(true);
            tableStyle.setFirstColumn(false);
            tableStyle.setLastColumn(false);
            CTAutoFilter autoFilter = xssfTable.getCTTable().addNewAutoFilter();
            autoFilter.setRef(reference.formatAsString());
            for (int i = 0; i < xssfTable.getEndColIndex() + 1; i++) {
                sheet.autoSizeColumn(i);
            }
            XSSFPivotTable pivotTable = sheet.createPivotTable(xssfTable, new CellReference("J1"));
            pivotTable.addRowLabel(2);
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM,3);
        }
        XSSFSheet sheet = workbook.createSheet("Calculation");
        XSSFRow headerRow = sheet.createRow(0);
        List<String> calculationHeader = new ArrayList<>(List.of("Account",
                                                                 "Total",
                                                                 "0",
                                                                 "1",
                                                                 "10",
                                                                 "12",
                                                                 "2",
                                                                 "3",
                                                                 "4",
                                                                 "47",
                                                                 "5",
                                                                 "58",
                                                                 "6",
                                                                 "7",
                                                                 "8",
                                                                 "9",
                                                                 "99"));
        List<String> accounts = new ArrayList<>(List.of("10010 Petty Cash",
                                                        "10020 Suntrust Account",
                                                        "10025 Suntrust Investment Sweep",
                                                        "10030 FTB",
                                                        "15000 Accounts Receivable",
                                                        "15002 Accounts Receivable POR",
                                                        "15005 Accounts Receivable POR Houston Depot",
                                                        "15050 Undeposited Funds",
                                                        "15100 Accounts Receivable - Other",
                                                        "15200 Travel Advances",
                                                        "15250 Travel Advance owed/paid",
                                                        "15300 Prepaid Insurance",
                                                        "15400 Prepaid Income Tax",
                                                        "15500 Prepaid Other",
                                                        "15550 PPD Commission",
                                                        "15640 Inventory for Resale",
                                                        "17000 Deposits on New Capital Assets",
                                                        "17100 Trucks",
                                                        "17200 Trailers",
                                                        "17400 Machinery and Equipment",
                                                        "17420 Machinery & Equipment -Ntl",
                                                        "17430 Machinery & Equipment - Houston Depot",
                                                        "17900 Net Suite",
                                                        "18000 Rental Equipment",
                                                        "18020 National Structures - FA",
                                                        "18030 National Floor - FA",
                                                        "18040 Ntl HVAC/Power Distribution FA",
                                                        "18045 National Doors",
                                                        "18046 National Lights",
                                                        "18047 National Tables & Chairs",
                                                        "18048 Pipe & Drape",
                                                        "18049 Cots",
                                                        "18080 National Fabric",
                                                        "18500 Office Equipment",
                                                        "18600 Leasehold Improvements",
                                                        "18800 Amortization",
                                                        "18900 Accumulated Depreciation",
                                                        "18950 Accumulated Amortization",
                                                        "20000 Accounts Payable",
                                                        "20050 First Horizon Cards",
                                                        "20055 Intercompany Clearing",
                                                        "20080 Due to/from GCTR LLC",
                                                        "20081 Due To/From MET LLC",
                                                        "20082 Due To/From MTC LLC",
                                                        "20085 Due To/From Verne",
                                                        "20100 Customer Deposits",
                                                        "20150 Deferred Revenue",
                                                        "20200 Accrued Accounting Fees",
                                                        "20300 Accrued Income Tax",
                                                        "20400 Accrued Interest Expense",
                                                        "20500 Accrued Property Tax",
                                                        "20550 Accrued Takedown Expense",
                                                        "20560 Year End Reversal of Job Cost",
                                                        "20600 Other Accounts Payable",
                                                        "20700 Sales Tax Payable",
                                                        "20800 Accrued Employee Bonus",
                                                        "20850 Mahaffey Cares - EE",
                                                        "20900 Accrued 401K Profit Sharing",
                                                        "20950 Accrued Health Insurance Costs",
                                                        "21000 Accrued Payroll",
                                                        "22020 William J. Pretsch",
                                                        "22030 George A. Smith",
                                                        "23053 PPP Loan via First Horizon",
                                                        "23801 FTB Loan - 48 Mths 8/17 - 8/21",
                                                        "24100 Line of Credit",
                                                        "24110 Suntrust Bank",
                                                        "24130 FTB - LOC",
                                                        "30000 Opening Balance Equity",
                                                        "31000 Common Stock",
                                                        "31500 Distributions",
                                                        "32000 Retained Earnings",
                                                        "33100 WJP Equity",
                                                        "33200 GAS Equity",
                                                        "33250 KJS Equity",
                                                        "40000 Pole Tent Rental",
                                                        "40100 Frame Tent Rental",
                                                        "40300 Clearspan Structures",
                                                        "40310 Super Series",
                                                        "40320 Mega Series",
                                                        "40330 Blast Structure",
                                                        "40340 Rapid Response Inflatable",
                                                        "40350 Shelter Arcum Tents",
                                                        "40700 MTS Rental",
                                                        "40800 Boomerang",
                                                        "44500 Flooring",
                                                        "44700 HVAC/Power Dist",
                                                        "44800 Glass/Hardsides",
                                                        "44900 Doors",
                                                        "45000 Lights",
                                                        "45100 Tables/Chairs",
                                                        "45200 Furnishings",
                                                        "46500 Tent Accessories",
                                                        "47000 Water Services",
                                                        "47100 Transportation/Delivery",
                                                        "47200 Hotel/Per Diem",
                                                        "47300 Sales/Service Labor",
                                                        "47400 Equipment",
                                                        "48000 Damage Waiver",
                                                        "48100 Washing/Repairing",
                                                        "48400 Retention Billing",
                                                        "48500 New Structure Sale",
                                                        "48600 Used Structure Sale",
                                                        "48900 Discounts",
                                                        "49000 Other Revenue",
                                                        "49500 Inter-Company Revenue",
                                                        "49501 Inter-Company Rent",
                                                        "50010 Frame - COGS",
                                                        "50020 Fabric - COGS",
                                                        "50030 Flooring - COGS",
                                                        "50040 HVAC/Power Distribution - COGS",
                                                        "50050 Hardsides - COGS",
                                                        "50060 Doors - COGS",
                                                        "50070 Lighting - COGS",
                                                        "50080 Other - COGS",
                                                        "50100 Job Supplies",
                                                        "50110 Tools",
                                                        "50113 Nuts, bolts, bits",
                                                        "50120 Supplies",
                                                        "50140 Carpet - Job Specific",
                                                        "50150 Damage - Job Specific",
                                                        "50160 Referral Fees",
                                                        "50170 Other Job Supplies",
                                                        "50180 Site Prep Work",
                                                        "50190 Job Training/Drug Testing",
                                                        "50200 Rental Expenditures",
                                                        "50210 Frame - Rental Expenditures",
                                                        "50215 Boomerang Parts & Pieces",
                                                        "50220 Fabric - Rental Expenditures",
                                                        "50230 Hardsides - Rental Expenditures",
                                                        "50240 Wood - Rental Expenditures",
                                                        "50245 HVAC Units",
                                                        "50246 Lights",
                                                        "50250 Doors - Rental Expenditures",
                                                        "50255 Chemicals - Tent Cleaner",
                                                        "50260 Supplies",
                                                        "50270 Linens - Rental Expenditures",
                                                        "50275 Chairs & Tables",
                                                        "50280 Glassware",
                                                        "50286 Chemicals - Dishwasher",
                                                        "50287 Chemicals - Linens",
                                                        "50288 Chemicals - Fabric",
                                                        "50299 Other Rental Expenditures",
                                                        "50300 Subcontracted Expenses",
                                                        "50310 Frame/Fabric",
                                                        "50320 HVAC/Power Distribution Sub",
                                                        "50325 Generators",
                                                        "50326 Light Sets",
                                                        "50330 Flooring - Subcontracted",
                                                        "50340 Water Services - Subcontracted",
                                                        "50341 Reefers",
                                                        "50345 Fuel Services",
                                                        "50346 Furnishings - Subcontracted",
                                                        "50347 Linens - Subcontracted",
                                                        "50348 Glassware - Sucontracted",
                                                        "50349 Labor - Installation",
                                                        "50350 Other Subcontracted Expenses",
                                                        "50360 Subcontracted Income",
                                                        "50400 Transportation",
                                                        "50410 Contract Carrier",
                                                        "50420 Freight",
                                                        "50430 Courier - Overnight Shipping",
                                                        "50440 Accrued Trans for Takedown",
                                                        "50500 Equipment Rental",
                                                        "50510 Forklift",
                                                        "50520 Crane",
                                                        "50530 Other Equipment Rental",
                                                        "50540 Accrued Equipment Rental",
                                                        "51000 Travel Expense",
                                                        "51010 Lodging - Field",
                                                        "51020 Per Diem - Field",
                                                        "51030 Airfare - Field",
                                                        "51040 Rental Car - Field",
                                                        "51050 Other Travel Expense",
                                                        "51060 Training Travel",
                                                        "51070 Accrued Travel for Takedown",
                                                        "52000 Fuel",
                                                        "52010 Diesel",
                                                        "52020 Gasoline",
                                                        "52030 Propane",
                                                        "52040 Forklift Fuel",
                                                        "53000 Truck Rental",
                                                        "53010 Tractor Rental",
                                                        "53020 Trailer Rental",
                                                        "53030 Box Truck Rental",
                                                        "53040 Pick Up Truck Rental",
                                                        "54000 Leased Equipment",
                                                        "54005 Pickup Lease",
                                                        "54010 Tractor Lease",
                                                        "54015 Box Truck Lease",
                                                        "54020 Trailer Lease",
                                                        "54030 Van/Car Lease",
                                                        "54040 Forklift Lease",
                                                        "55000 Repair and Maintenance",
                                                        "55010 Pickup Repair and Maintenance",
                                                        "55015 Box Truck Repair & Maintenance",
                                                        "55020 Tractor Repair and Maintenance",
                                                        "55030 Trailer Repair and Maintenance",
                                                        "55040 Forklift Repair and Maintenance",
                                                        "55050 Machinery Repair and Maintenanc",
                                                        "55060 A/C Unit Repair and Maintenance",
                                                        "55070 Generator Repair & Maintenance",
                                                        "55080 Other Repairs and Maintenance",
                                                        "56000 Depreciation - Vehicles",
                                                        "56010 Trucks - Depreciation",
                                                        "56020 Trailers - Depreciation",
                                                        "56030 Depreciation - Machinery",
                                                        "56040 Machinery & Equipment Houston Depot Depreciation",
                                                        "57000 Depreciation Rental Equipment",
                                                        "57020 National Structures Depr",
                                                        "57022 Fabric - Depreciation",
                                                        "57030 National Floor Depr",
                                                        "57040 Ntl HVAC/Power Distribution Dpr",
                                                        "57042 Depreciation - Doors",
                                                        "57046 Lights - Depreciation",
                                                        "57047 Tables & Chairs Depreciation",
                                                        "57048 Pipe & Drape Depreciation",
                                                        "57050 National Water Services Dpr",
                                                        "58000 Amortization Expense",
                                                        "59001 Inter-Company Hotel/Travel/Per Diem",
                                                        "59002 Inter-Company Equipment",
                                                        "59003 Inter-Company Equip Transportation",
                                                        "59004 Inter-Company Structure & HVAC Labor",
                                                        "59005 Inter-Company Materials",
                                                        "59006 Inter-Company Rent",
                                                        "59007 Inter-Company Other Repairs & Maintenance",
                                                        "60000 Labor",
                                                        "60005 HVAC Tech",
                                                        "60010 Field",
                                                        "60020 Manufacturing",
                                                        "60030 Hardware",
                                                        "60033 Truck Loading",
                                                        "60035 Fabric",
                                                        "60040 Mileage",
                                                        "60045 Training Labor",
                                                        "60050 Cleaning",
                                                        "60060 Cell Phone Reimbursement",
                                                        "60070 Accrued Takedown Labor",
                                                        "60200 Salaries",
                                                        "60210 Management",
                                                        "60213 Warehouse Admin",
                                                        "60220 Sales",
                                                        "60225 Commission",
                                                        "60230 Clerical",
                                                        "60400 Bonus",
                                                        "60410 Salaried",
                                                        "60420 Hourly",
                                                        "60500 Temporary Labor",
                                                        "60510 Field",
                                                        "60520 Leased Employees",
                                                        "60525 Linen",
                                                        "60526 Warehouse",
                                                        "60527 Fabric Repair",
                                                        "60530 Contractors",
                                                        "60540 Other Temporary Labor",
                                                        "60545 Capitalized Labor",
                                                        "60600 Payroll Taxes",
                                                        "60700 Worker's Compensation",
                                                        "60800 Medical",
                                                        "60810 Insurance",
                                                        "60820 Drug Testing/Physicals",
                                                        "60830 Injuries",
                                                        "60900 401(k) Contribution",
                                                        "60910 Match",
                                                        "60920 Profit Sharing Accrual",
                                                        "60990 Education",
                                                        "60995 Employee Training - Mfs Qtr.",
                                                        "61000 Employee Relations",
                                                        "61010 Uniforms & Boots",
                                                        "61020 Meals",
                                                        "61030 Special Event/Party",
                                                        "61040 Safety Bonus Program",
                                                        "61050 Employee Relations - Others",
                                                        "61200 Employee Recruitment",
                                                        "61210 Employment Advertising",
                                                        "61220 Background Checks",
                                                        "61230 Moving Expenses",
                                                        "61240 New Hire Training",
                                                        "62000 Advertising",
                                                        "62010 Print",
                                                        "62020 Public Relations",
                                                        "62030 Awards",
                                                        "62040 Design",
                                                        "62050 Marketing Sponsorships",
                                                        "62060 Social Media",
                                                        "62100 Internet PPC & SEO",
                                                        "62200 Marketing",
                                                        "62210 Website Maintenance",
                                                        "62220 Trade Shows",
                                                        "62230 Travel",
                                                        "62240 Media",
                                                        "62260 Software & Technology",
                                                        "62270 Digital/Inbound Marketing",
                                                        "62290 Other - Marketing",
                                                        "62300 Processing Fees",
                                                        "62310 Bank Fees",
                                                        "62320 Credit Card Fees",
                                                        "62330 Payroll Fees",
                                                        "62400 Building Repair and Maintenance",
                                                        "62500 Computer System Service",
                                                        "62600 Courier",
                                                        "62800 Customer Meals",
                                                        "62900 Depreciation - Office",
                                                        "62910 Leasehold Improvements Dpr",
                                                        "62920 Office Equipment Dpr",
                                                        "63000 Dues and Subscriptions",
                                                        "63200 Entertainment",
                                                        "63400 Insurance Expense",
                                                        "63410 Commercial Package",
                                                        "63420 Umbrella",
                                                        "63430 General Liability",
                                                        "63440 Equipment Floater",
                                                        "63450 Commercial Auto",
                                                        "64000 Leased Office Equipment",
                                                        "64100 Office Supplies",
                                                        "64300 Permits and Licenses",
                                                        "64400 Telecommunication",
                                                        "64410 Cell Phones",
                                                        "64420 Data and Voice T-1",
                                                        "64440 GPS Tracking",
                                                        "64500 Postage",
                                                        "64600 Professional Services",
                                                        "64610 Accounting",
                                                        "64620 Engineering Services",
                                                        "64630 Legal",
                                                        "64640 Other Professional Services",
                                                        "65000 Rent",
                                                        "65100 Taxes",
                                                        "65110 Property",
                                                        "65120 Business",
                                                        "65130 Income Tax",
                                                        "65140 Truck Registration",
                                                        "65150 Entity Registration",
                                                        "65200 Office Travel Expense",
                                                        "65210 Lodging - Office",
                                                        "65220 Per Diem - Office",
                                                        "65230 Airfare - Office",
                                                        "65240 Rental Car - Office",
                                                        "65250 Other - Office Travel",
                                                        "65260 Terry Logan",
                                                        "65261 Kevin Ponder",
                                                        "65263 Mark Huels",
                                                        "65264 Mark Lewis",
                                                        "65265 Mitch Holt",
                                                        "65267 Bill Sublette",
                                                        "65268 Matt Hixson",
                                                        "65271 David Rech",
                                                        "65272 Camille Curry",
                                                        "65273 Erin Fulweber",
                                                        "65274 Joe Berger",
                                                        "65275 Brian Szkaradnik",
                                                        "65276 Victoria Mariencheck",
                                                        "65277 Trip Gintz",
                                                        "65278 George Smith",
                                                        "65280 Gary Moe",
                                                        "65281 Andre Pinto",
                                                        "65286 Chris Ruch",
                                                        "65287 Greg West",
                                                        "65288 Stefani Schwen",
                                                        "65289 Chris Hill",
                                                        "65290 Chris Ladley",
                                                        "65291 Edwin Santana",
                                                        "65292 Lori Dominy",
                                                        "65293 Paige Whitton",
                                                        "65294 Blake Myers",
                                                        "65300 Utilities",
                                                        "66000 Payroll Expenses",
                                                        "66500 Source Wave Temp Labor",
                                                        "70000 Other Income",
                                                        "70100 Bad Debt Recovery",
                                                        "70200 Gain/Loss Sale of Fixed Asset",
                                                        "70400 Interest Income",
                                                        "70500 Vendor Rebates",
                                                        "80000 Other Expense",
                                                        "80100 Bad Debt Expense",
                                                        "80300 Directors Fees",
                                                        "80400 Profit Sharing",
                                                        "80500 Guarantee Fee",
                                                        "81000 Interest Expense",
                                                        "81010 Scheduled Loans Interest",
                                                        "81020 Line of Credit Interest",
                                                        "81030 Executive Loans Interest",
                                                        "89000 Overhead Allocation"));
        for (int i = 0; i < calculationHeader.size(); i++) {
            String value = calculationHeader.get(i);
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(value);
            cell.setCellStyle(bold);
        }
        for (int i = 0; i< accounts.size(); i++){
            XSSFRow row = sheet.createRow(i+1);
            for(int j = 0; j<calculationHeader.size(); j++){
                XSSFCell cell = row.createCell(j);
                if(j>0){
                    cell.setBlank();
                    cell.setCellStyle(style);
                }
                else{
                    cell.setCellValue(accounts.get(i));
                }
            }
        }
        CellReference startReference = new CellReference(0, 0);
        CellReference endReference = new CellReference(sheet.getLastRowNum(),
                                                       sheet.getRow(sheet.getLastRowNum()).getLastCellNum() - 1
        );
        AreaReference reference = new AreaReference(startReference, endReference, SpreadsheetVersion.EXCEL2007);
        XSSFTable xssfTable = sheet.createTable(reference);
        xssfTable.getCTTable().addNewTableStyleInfo();
        XSSFTableStyleInfo tableStyle = (XSSFTableStyleInfo) xssfTable.getStyle();
        tableStyle.setName("TableStyleMedium2");
        tableStyle.setShowColumnStripes(false);
        tableStyle.setShowRowStripes(true);
        tableStyle.setFirstColumn(false);
        tableStyle.setLastColumn(false);
        CTAutoFilter autoFilter = xssfTable.getCTTable().addNewAutoFilter();
        autoFilter.setRef(reference.formatAsString());
        List<CTTableColumn> tableColumnList = xssfTable.getCTTable().getTableColumns().getTableColumnList();
        for (int i = 1; i < tableColumnList.size(); i++) {
            CTTableColumn column = tableColumnList.get(i);
            CTTableFormula formula = column.addNewCalculatedColumnFormula();
            formula.setArray(true);
            if (i == 1) {
                formula.setStringValue("SUM(" + xssfTable.getName() + "[[#This Row],[0]:[99]])");
            }
            else {
                formula.setStringValue("IFERROR(INDEX(INDIRECT(\"'Dept \"&" +
                                       xssfTable.getName() +
                                       "[[#Headers],[0]]&\"'!J:K\"),MATCH(" +
                                       xssfTable.getName() +
                                       "[[#This Row],[Account]:[Account]],INDIRECT(\"'Dept \"&" +
                                       xssfTable.getName() +
                                       "[[#Headers],[0]]&\"'!J:J\"),0),2),0)");
            }
        }
        XmlBoolean tru = XmlBoolean.Factory.newInstance();
        tru.setStringValue("1");
        XmlBoolean fal = XmlBoolean.Factory.newInstance();
        fal.setStringValue("0");
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            XSSFRow row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                XSSFCell cell = row.getCell(j);
                if (j > 0) {
                    if (j == 1) {
                        CTCellFormula formula = cell.getCTCell().addNewF();

                        formula.xsetCa(tru);
                        formula.setStringValue("SUM(" + xssfTable.getName() + "[[#This Row],[0]:[99]])");
                    }
                    else {
                        CTCellFormula formula = cell.getCTCell().addNewF();
                        formula.xsetCa(tru);
                        formula.setRef(cell.getReference());
                        cell.setCellFormula("IFERROR(INDEX(INDIRECT(\"'Dept \"&" +
                                            xssfTable.getName() +
                                            "[[#Headers],["+xssfTable.getColumns().get(j).getName()+"]]&\"'!J:K\"),MATCH(" +
                                            xssfTable.getName() +
                                            "[[#This Row],[Account]:[Account]],INDIRECT(\"'Dept \"&" +
                                            xssfTable.getName() +
                                            "[[#Headers],["+xssfTable.getColumns().get(j).getName()+"]]&\"'!J:J\"),0),2),0)");
                    }
                }
            }
        }
        for (int i = 0; i < xssfTable.getEndColIndex() + 1; i++) {
            sheet.autoSizeColumn(i);
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(workbookFile);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }
        catch (IOException e) {
            logger.fatal("Unable to write file", e);
            System.exit(1);
        }
    }

    @Deprecated
    public static void makeWorkbook(Path workbookFile, List<List<Object>> debitTable, List<List<Object>> creditTable) {
        makeWorkbook(workbookFile.toFile(), debitTable, creditTable);
    }

    @Deprecated
    public static void makeWorkbook(File workbookFile, List<List<Object>> debitTable, List<List<Object>> creditTable) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");
        XSSFRow headerRow = sheet.createRow(0);
        List<Object> header = debitTable.get(0);
        XSSFCellStyle bold = workbook.createCellStyle();
        XSSFFont font = workbook.findFont(true,
                                          IndexedColors.AUTOMATIC.getIndex(),
                                          XSSFFont.DEFAULT_FONT_SIZE,
                                          XSSFFont.DEFAULT_FONT_NAME,
                                          false,
                                          false,
                                          XSSFFont.SS_NONE,
                                          XSSFFont.U_NONE
        );
        bold.setFont(font);
        for (int i = 0; i < header.size(); i++) {
            Object object = header.get(i);
            XSSFCell cell = headerRow.createCell(i);
            DataUtils.setCellValue(cell, object);
            cell.setCellStyle(bold);
        }
        for (int i = 1; i < debitTable.size(); i++) {
            XSSFRow row = sheet.createRow(i);
            List<Object> debitRow = debitTable.get(i);
            List<Object> creditRow = creditTable.get(i);
            for (int j = 0; j < debitRow.size(); j++) {
                Object debitObject = debitRow.get(j);
                Object creditObject = creditRow.get(j);
                Object object = debitObject;
                XSSFCell cell = row.createCell(j);
                if (debitObject instanceof BigDecimal && creditObject instanceof BigDecimal) {
                    BigDecimal debitBigDecimal = (BigDecimal) debitObject;
                    BigDecimal creditBigDecimal = (BigDecimal) creditObject;
                    BigDecimal amountBigDecimal = debitBigDecimal.add(creditBigDecimal);
                    object = amountBigDecimal.doubleValue();
                }
                DataUtils.setCellValue(cell, object);
            }
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(workbookFile);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        }
        catch (IOException e) {
            logger.fatal("Unable to write file", e);
            System.exit(1);
        }
        formatWorkbook(workbookFile);
    }
}
